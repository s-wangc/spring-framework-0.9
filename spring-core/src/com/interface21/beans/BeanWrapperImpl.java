/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify. However, please
 * acknowledge the source and include the above URL in each
 * class using or derived from this code.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package com.interface21.beans;

import java.beans.IndexedPropertyDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interface21.beans.propertyeditors.ClassEditor;
import com.interface21.beans.propertyeditors.LocaleEditor;
import com.interface21.beans.propertyeditors.PropertiesEditor;
import com.interface21.beans.propertyeditors.PropertyValuesEditor;
import com.interface21.beans.propertyeditors.StringArrayPropertyEditor;

/**
 * BeanWrapper接口的默认实现, 应该足以满足所有正常使用. 缓存内省结果以提高效率.
 *
 * <p>注意: 此类永远不会尝试按名称加载类, 因为这会在具有多个部署模块的J2EE应用
 * 程序中造成类加载问题. 例如, 如果类在WAR中使用, 但是由EJB类加载器加载, 并且
 * 要加载的类在WAR中, 则按名称加载类在某些应用程序服务器中将不起作用.
 * (这个类将使用EJB类加载器, 它看不到所需的类.)我们不会尝试通过在运行时获取类加载器
 * 来解决此类问题, 因为这违反了EJB编程限制.
 *
 * <p>注意: 关于com.interface21.beans.propertyeditors中的属性编辑器.
 * 还明确地注册默认的JRE, 以关心那些不使用线程context类加载器进行编辑器搜索路径的JRE.
 * 应用程序可以在使用BeanWrapperImpl实例之前使用标准PropertyEditorManager注册自定义
 * 编辑器, 也可以调用实例的registerCustomEditor方法为特定实例注册编辑器.
 *
 * @author Rod Johnson
 * @version $Revision: 1.10 $
 * @see #registerCustomEditor
 * @see java.beans.PropertyEditorManager
 * @since 15 April 2001
 */
public class BeanWrapperImpl implements BeanWrapper {

	/**
	 * 默认情况下是否应该启用JavaBeans事件传播?
	 */
	public static final boolean DEFAULT_EVENT_PROPAGATION_ENABLED = false;

	/**
	 * 我们将创建很多这样的对象, 因此我们不希望每次都有一个新的logger.
	 */
	private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);

	/**
	 * 存放默认编辑器
	 */
	private static final Map defaultEditors = new HashMap();

	static {
		// 安装默认属性编辑器
		try {
			// 这个不能应用<ClassName>编辑器命名模式
			PropertyEditorManager.registerEditor(String[].class, StringArrayPropertyEditor.class);
			// 在我们的标准包中注册所有编辑器
			PropertyEditorManager.setEditorSearchPath(new String[]{
					"sun.beans.editors",
					"com.interface21.beans.propertyeditors"
			});
		} catch (SecurityException ex) {
			// e.g. in applets -> log and proceed
			logger.warn("Cannot register property editors with PropertyEditorManager", ex);
		}

		// 在此类中注册默认编辑器, 用于上述抛出SecurityException的受限环境,
		// 以及不使用线程context类加载器进行属性编辑器查找的JDK.
		defaultEditors.put(String[].class, new StringArrayPropertyEditor());
		defaultEditors.put(PropertyValues.class, new PropertyValuesEditor());
		defaultEditors.put(Properties.class, new PropertiesEditor());
		defaultEditors.put(Class.class, new ClassEditor());
		defaultEditors.put(Locale.class, new LocaleEditor());
	}


	//---------------------------------------------------------------------
	// 实例数据
	//---------------------------------------------------------------------

	/**
	 * 被包装的对象
	 */
	private Object object;

	/**
	 * 缓存这个对象的内省结果, 以防止每次遇到JavaBeans内省的开销.
	 */
	private CachedIntrospectionResults cachedIntrospectionResults;

	/**
	 * 用于传播事件的标准java.beans辅助器对象
	 */
	private VetoableChangeSupport vetoableChangeSupport;

	/**
	 * 用于传播事件的标准java.beans辅助器对象
	 */
	private PropertyChangeSupport propertyChangeSupport;

	/**
	 * 我们应该将事件传播给listeners吗?
	 */
	private boolean eventPropagationEnabled = DEFAULT_EVENT_PROPAGATION_ENABLED;

	/* 使用缓存的嵌套BeanWrappers进行映射 */
	private Map nestedBeanWrappers;

	/**
	 * 使用自定义PropertyEditor实例映射
	 */
	private Map customEditors;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * 使用默认事件传播创建新的BeanWrapper(disabled)
	 *
	 * @param object 被这个BeanWrapper包装的对象.
	 * @throws BeansException 如果对象不能被BeanWrapper包装
	 */
	public BeanWrapperImpl(Object object) throws BeansException {
		this(object, DEFAULT_EVENT_PROPAGATION_ENABLED);
	}

	/**
	 * 创建新的BeanWrapperImpl, 允许指定是否启用事件传播.
	 *
	 * @param object                  被这个BeanWrapper包装的对象.
	 * @param eventPropagationEnabled 是否应启用事件传播
	 * @throws BeansException 如果对象不能被BeaWrapper包装
	 */
	public BeanWrapperImpl(Object object, boolean eventPropagationEnabled) throws BeansException {
		this.eventPropagationEnabled = eventPropagationEnabled;
		setObject(object);
	}

	/**
	 * 创建新的BeanWrapperImpl, 包装指定类的新实例
	 *
	 * @param clazz 要实例化和包装的类
	 * @throws BeansException 如果该类不能被BeanWrapper包装
	 */
	public BeanWrapperImpl(Class clazz) throws BeansException {
		this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(clazz);
		setObject(BeanUtils.instantiateClass(clazz));
	}

	/**
	 * 给定缓存的内省结果和给定对象, 创建新的BeanWrapperImpl. 仅供内部使用.
	 *
	 * @param cachedIntrospectionResults 缓存的内省结果, 用于提高操作此类对象的效率.
	 * @param obj                        object to wrap
	 * @throws BeansException 如果无法构造包装器
	 */
	private BeanWrapperImpl(CachedIntrospectionResults cachedIntrospectionResults, Object obj) throws BeansException {
		this.cachedIntrospectionResults = cachedIntrospectionResults;
		setObject(obj);
	}

	/**
	 * 为了提高效率, 包括了此方法. 如果实现缓存了有关改类的所有必要信息,
	 * 则实例化一个新的包装器来复制缓存的信息的可能要比再次使用内省快得<b>多</b>.
	 * 包装的实例是独立的, 新的BeanWrapper也是如此:
	 * 仅复制缓存的内省信息.
	 *
	 * @param obj 要由此BeanWrapper包装的新对象, 替换当前的目标对象.
	 * @return 新对象的BeanWrapper, 基于此对象可用的缓存信息
	 * @throws BeansException 如果目标无法更改.
	 */
	public BeanWrapper newWrapper(Object obj) throws BeansException {
		if (!this.cachedIntrospectionResults.getBeanClass().equals(obj.getClass()))
			throw new FatalBeanException("Cannot create new wrapper for object of class "
					+ obj.getClass().getName() + " using cached information for class "
					+ cachedIntrospectionResults.getBeanClass(), null);
		return new BeanWrapperImpl(this.cachedIntrospectionResults, obj);
	}

	/**
	 * 切换目标对象的实现方法, 仅当新对象的类与替换对象的类不同时才替换
	 * 缓存的内省结果
	 *
	 * @param object new target
	 * @throws BeansException 如果对象无法更改
	 */
	private void setObject(Object object) throws BeansException {
		if (object == null)
			throw new FatalBeanException("Cannot set BeanWrapperImpl target to a null object", null);
		this.object = object;
		if (cachedIntrospectionResults == null
				|| !cachedIntrospectionResults.getBeanClass().equals(object.getClass())) {
			cachedIntrospectionResults = CachedIntrospectionResults.forClass(object.getClass());
		}
		setEventPropagationEnabled(this.eventPropagationEnabled);
		// assert: cachedIntrospectionResults != null
	}


	//---------------------------------------------------------------------
	// Implementation of BeanWrapper
	//---------------------------------------------------------------------

	public void setWrappedInstance(Object object) throws BeansException {
		setObject(object);
	}

	public void newWrappedInstance() throws BeansException {
		this.object = BeanUtils.instantiateClass(getWrappedClass());
		vetoableChangeSupport = new VetoableChangeSupport(object);
	}

	public Class getWrappedClass() {
		return object.getClass();
	}

	public Object getWrappedInstance() {
		return object;
	}


	public void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor) {
		if (propertyPath != null) {
			BeanWrapperImpl bw = getBeanWrapperForNestedProperty(propertyPath);
			bw.doRegisterCustomEditor(requiredType, getFinalPath(propertyPath), propertyEditor);
		} else {
			doRegisterCustomEditor(requiredType, propertyPath, propertyEditor);
		}
	}

	public void doRegisterCustomEditor(Class requiredType, String propertyName, PropertyEditor propertyEditor) {
		if (this.customEditors == null) {
			this.customEditors = new HashMap();
		}
		if (propertyName != null) {
			// consistency check
			PropertyDescriptor descriptor = getPropertyDescriptor(propertyName);
			if (requiredType != null && !descriptor.getPropertyType().isAssignableFrom(requiredType)) {
				throw new IllegalArgumentException("Types do not match: required=" + requiredType.getName() +
						", found=" + descriptor.getPropertyType());
			}
			this.customEditors.put(propertyName, propertyEditor);
		} else {
			if (requiredType == null) {
				throw new IllegalArgumentException("No propertyName and no requiredType specified");
			}
			this.customEditors.put(requiredType, propertyEditor);
		}
	}

	public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
		if (propertyPath != null) {
			BeanWrapperImpl bw = getBeanWrapperForNestedProperty(propertyPath);
			return bw.doFindCustomEditor(requiredType, getFinalPath(propertyPath));
		} else {
			return doFindCustomEditor(requiredType, propertyPath);
		}
	}

	public PropertyEditor doFindCustomEditor(Class requiredType, String propertyName) {
		if (this.customEditors == null) {
			return null;
		}
		if (propertyName != null) {
			// 首先检查特定于属性的编辑器
			PropertyDescriptor descriptor = getPropertyDescriptor(propertyName);
			PropertyEditor editor = (PropertyEditor) this.customEditors.get(propertyName);
			if (editor != null) {
				// consistency check
				if (requiredType != null) {
					if (!descriptor.getPropertyType().isAssignableFrom(requiredType)) {
						throw new IllegalArgumentException("Types do not match: required=" + requiredType.getName() +
								", found=" + descriptor.getPropertyType());
					}
				}
				return editor;
			} else {
				if (requiredType == null) {
					// try property type
					requiredType = descriptor.getPropertyType();
				}
			}
		}
		// 无特定于属性的编辑器 -> 检查特定于类型的编辑器
		return (PropertyEditor) this.customEditors.get(requiredType);
	}


	/**
	 * 将值转换为所需类型(如果有必要, 可以从字符串中转换), 以创建PropertyChangeEvent.
	 * 从String到任何类型的转换使用PropertyEditor类的setAsTest()方法. 请注意,
	 * 必须为此类注册PropertyEditor才能使其生效. 这是一个标准的Java Beans API.
	 * 本类会自动注册许多属性编辑器.
	 *
	 * @param target       target bean
	 * @param propertyName name of the property
	 * @param oldValue     之前的值(如果可用). 可能为空.
	 * @param newValue     proposed change value.
	 * @param requiredType 我们必须转换为的类型
	 * @return 一个PropertyChangeEvent, 包含新值的转换类型.
	 * @throws BeansException 如果有内部错误
	 */
	private PropertyChangeEvent createPropertyChangeEventWithTypeConversionIfNecessary(
			Object target, String propertyName,
			Object oldValue, Object newValue,
			Class requiredType) throws BeansException {
		return new PropertyChangeEvent(target, propertyName, oldValue, doTypeConversionIfNecessary(target, propertyName, oldValue, newValue, requiredType));
	}

	/**
	 * 将值转换为所需类型(如果需要, 可以从字符串中转换)从String到任何类型的转换使用
	 * PropertyEditor类的setAsTest()方法. 请注意, 必须为此类注册PropertyEditor
	 * 才能使其工作. 这是一个标准的Java Beans API.
	 * 本类会自动注册许多属性编辑器.
	 *
	 * @param target       target bean
	 * @param propertyName name of the property
	 * @param oldValue     之前的值, 如果有的话. 可能为空.
	 * @param newValue     proposed change value.
	 * @param requiredType 我们必须转换为的类型
	 * @return 新值, 可能是类型转换的结果.
	 * @throws BeansException 如果有内部错误
	 */
	public Object doTypeConversionIfNecessary(
			Object target, String propertyName,
			Object oldValue, Object newValue,
			Class requiredType) throws BeansException {
		// 仅当值不为null时才需要强制转换
		if (newValue != null) {
			// 我们可能需要更改此类型的newValue自定义编辑器的值?
			PropertyEditor pe = findCustomEditor(requiredType, propertyName);
			if ((pe != null || !requiredType.isAssignableFrom(newValue.getClass())) && (newValue instanceof String)) {
				if (logger.isDebugEnabled())
					logger.debug("Convert: String to " + requiredType);
				if (pe == null) {
					// 没有自定义编辑器 -> 检查BeanWrapper的默认编辑器
					pe = (PropertyEditor) defaultEditors.get(requiredType);
					if (pe == null) {
						// 没有BeanWrapper默认编辑器 -> 检查标准编辑器
						pe = PropertyEditorManager.findEditor(requiredType);
					}
				}
				if (logger.isDebugEnabled())
					logger.debug("Using property editor [" + pe + "]");
				if (pe != null) {
					try {
						pe.setAsText((String) newValue);
						newValue = pe.getValue();
					} catch (IllegalArgumentException ex) {
						throw new TypeMismatchException(
								new PropertyChangeEvent(target, propertyName, oldValue, newValue), requiredType, ex);
					}
				}
			}
		}
		return newValue;
	}

	/**
	 * @see BeanWrapper#setPropertyValue(String, Object)
	 */
	public void setPropertyValue(String propertyName, Object value) throws PropertyVetoException, BeansException {
		setPropertyValue(new PropertyValue(propertyName, value));
	}

	/**
	 * 该属性是否嵌套? 也就是说, 它是否包含嵌套属性分隔符(通常是.)
	 *
	 * @param path property path
	 * @return 表示是否是嵌套属性的boolean值
	 */
	private boolean isNestedProperty(String path) {
		return path.indexOf(NESTED_PROPERTY_SEPARATOR) != -1;
	}

	/**
	 * 获取路径的最后一个组件. 如果没有嵌套, 也可以工作.
	 *
	 * @param nestedPath 我们知道的属性路径是嵌套的
	 * @return 路径的最后一个组件(目标bean上的属性)
	 */
	private String getFinalPath(String nestedPath) {
		return nestedPath.substring(nestedPath.lastIndexOf(NESTED_PROPERTY_SEPARATOR) + 1);
	}

	/**
	 * 递归导航以返回嵌套路径的BeanWrapper.
	 *
	 * @param path 属性路径, 可以嵌套
	 * @return 目标bean的BeanWrapper
	 */
	private BeanWrapperImpl getBeanWrapperForNestedProperty(String path) {
		// 属性分隔符所在的索引
		int pos = path.indexOf(NESTED_PROPERTY_SEPARATOR);
		// 递归处理嵌套属性
		if (pos > -1) {
			// 嵌套属性的属性名
			String nestedProperty = path.substring(0, pos);
			// 嵌套属性的嵌套路径(去掉了属性名的路径)
			String nestedPath = path.substring(pos + 1);
			logger.debug("Navigating to property path '" + nestedPath + "' of nested property '" + nestedProperty + "'");
			BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
			return nestedBw.getBeanWrapperForNestedProperty(nestedPath);
		} else {
			return this;
		}
	}

	/**
	 * 检索给定嵌套属性的BeanWrapper.
	 * 如果在缓存中找不到, 则创建一个新的.
	 * <p>注意: 缓存嵌套的BeanWrappers现在是必要的, 以便为嵌套属性保留已注册的自定义编辑器.
	 *
	 * @param nestedProperty 用于创建BeanWrapper的属性
	 * @return BeanWrapper实例, 可以是缓存的, 也可以是新创建的
	 */
	private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty) {
		if (this.nestedBeanWrappers == null) {
			this.nestedBeanWrappers = new HashMap();
		}
		// 获取bean属性的值
		Object propertyValue = getPropertyValue(nestedProperty);
		if (propertyValue == null) {
			throw new NullValueInNestedPathException(getWrappedClass(), nestedProperty);
		}
		// 查找缓存的子BeanWrapper, 如果找不到则新建一个
		BeanWrapperImpl nestedBw = (BeanWrapperImpl) this.nestedBeanWrappers.get(propertyValue);
		if (nestedBw == null) {
			logger.debug("Creating new nested BeanWrapper for property '" + nestedProperty + "'");
			nestedBw = new BeanWrapperImpl(propertyValue, false);
			// 继承所有特定于类型的PropertyEditors
			if (this.customEditors != null) {
				for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext(); ) {
					Object key = it.next();
					if (key instanceof Class) {
						Class requiredType = (Class) key;
						PropertyEditor propertyEditor = (PropertyEditor) this.customEditors.get(key);
						nestedBw.registerCustomEditor(requiredType, null, propertyEditor);
					}
				}
			}
			this.nestedBeanWrappers.put(propertyValue, nestedBw);
		} else {
			logger.debug("Using cached nested BeanWrapper for property '" + nestedProperty + "'");
		}
		return nestedBw;
	}

	/**
	 * 设置单个字段.
	 * 所有其他的setters都要经历这个.
	 *
	 * @param pv 用于更新的属性值
	 * @throws PropertyVetoException 如果listener抛出JavaBeans API 否决
	 * @throws BeansException        如果存在低级致命错误
	 */
	public void setPropertyValue(PropertyValue pv) throws PropertyVetoException, BeansException {

		// 该属性是否嵌套(属性是否有嵌套属性的分隔符)
		if (isNestedProperty(pv.getName())) {
			try {
				BeanWrapper nestedBw = getBeanWrapperForNestedProperty(pv.getName());
				nestedBw.setPropertyValue(new PropertyValue(getFinalPath(pv.getName()), pv.getValue()));
				return;
			} catch (NullValueInNestedPathException ex) {
				// 让这个通过
				throw ex;
			} catch (FatalBeanException ex) {
				// 嵌套路径中出错
				throw new NotWritablePropertyException(pv.getName(), getWrappedClass());
			}
		}

		// 该属性是否可写
		if (!isWritableProperty(pv.getName())) {
			throw new NotWritablePropertyException(pv.getName(), getWrappedClass());
		}

		// 获取属性修饰器
		PropertyDescriptor pd = getPropertyDescriptor(pv.getName());
		Method writeMethod = pd.getWriteMethod();
		Method readMethod = pd.getReadMethod();
		Object oldValue = null;    // 如果它不是可读属性, 可以保留为null
		PropertyChangeEvent propertyChangeEvent = null;

		try {
			if (readMethod != null && eventPropagationEnabled) {
				// 只能在可读属性的情况下找到现有值
				try {
					oldValue = readMethod.invoke(object, new Object[]{});
				} catch (Exception ex) {
					// getter抛出了一个异常, 因此我们无法检索旧值.
					// 我们此时并不真正对任何异常感兴趣, 所以我们只记录问题并将oldValue保留为null
					logger.warn("Failed to invoke getter '" + readMethod.getName()
									+ "' to get old property value before property change: getter probably threw an exception",
							ex);
				}
			}

			// 旧值可能仍为null
			propertyChangeEvent = createPropertyChangeEventWithTypeConversionIfNecessary(
					object, pv.getName(), oldValue, pv.getValue(), pd.getPropertyType());

			// 可能抛出PropertyVetoException: 如果发生这种情况, PropertyChangeSupport
			// 类会触发一个reversion事件, 我们将跳出这个方法, 这意味着实际上从未进行过更改
			if (eventPropagationEnabled) {
				vetoableChangeSupport.fireVetoableChange(propertyChangeEvent);
			}

			if (pd.getPropertyType().isPrimitive() && (pv.getValue() == null || "".equals(pv.getValue()))) {
				throw new IllegalArgumentException("Invalid value [" + pv.getValue() + "] for property [" + pd.getName() + "] of primitive type [" + pd.getPropertyType() + "]");
			}

			// 做出改变
			if (logger.isDebugEnabled())
				logger.debug("About to invoke write method ["
						+ writeMethod + "] on object of class '" + object.getClass().getName() + "'");
			writeMethod.invoke(object, new Object[]{propertyChangeEvent.getNewValue()});
			if (logger.isDebugEnabled())
				logger.debug("Invoked write method [" + writeMethod + "] ok");

			// 如果我们到了这里, 我们已经改变了属性, 可以广播了
			if (eventPropagationEnabled)
				propertyChangeSupport.firePropertyChange(propertyChangeEvent);
		} catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof PropertyVetoException)
				throw (PropertyVetoException) ex.getTargetException();
			if (ex.getTargetException() instanceof ClassCastException)
				throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
			throw new MethodInvocationException(ex.getTargetException(), propertyChangeEvent);
		} catch (IllegalAccessException ex) {
			throw new FatalBeanException("illegal attempt to set property [" + pv + "] threw exception", ex);
		} catch (IllegalArgumentException ex) {
			throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
		}
	}


	/**
	 * 从Map进行批量更新.
	 * PropertyValues的批量更新功能更强大: 此方法是为方便起见而提供的.
	 *
	 * @param map 包含要设置的属性的map, 作为name-value对.
	 *            Map可能包含嵌套属性.
	 * @throws BeansException 如果有一个致命的, 低级别的异常
	 */
	public void setPropertyValues(Map map) throws BeansException {
		setPropertyValues(new MutablePropertyValues(map));
	}


	/**
	 * 给被包装的bean设置属性值
	 *
	 * @see BeanWrapper#setPropertyValues(PropertyValues)
	 */
	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false, null);
	}


	/**
	 * 给被包装的bean设置属性值
	 *
	 * @see BeanWrapper#setPropertyValues(PropertyValues, boolean, PropertyValuesValidator)
	 */
	public void setPropertyValues(PropertyValues propertyValues,
								  boolean ignoreUnknown, PropertyValuesValidator pvsValidator) throws BeansException {
		// 仅在需要时创建
		PropertyVetoExceptionsException propertyVetoExceptionsException = new PropertyVetoExceptionsException(this);

		if (pvsValidator != null) {
			try {
				pvsValidator.validatePropertyValues(propertyValues);
			} catch (InvalidPropertyValuesException ipvex) {
				propertyVetoExceptionsException.addMissingFields(ipvex);
			}
		}

		PropertyValue[] pvs = propertyValues.getPropertyValues();
		for (int i = 0; i < pvs.length; i++) {
			try {
				// 如果出现严重故障(例如没有匹配字段), 则此方法可能抛出ReflectionException,
				// 此处不会捕获该异常. 我们可以尝试只处理不那么严重的异常.
				setPropertyValue(pvs[i]);
			}
			// 致命的ReflectionExceptions将被重新抛出
			catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown)
					throw ex;
				// 否则, 只需忽略它并继续...
			} catch (PropertyVetoException ex) {
				propertyVetoExceptionsException.addPropertyVetoException(ex);
			} catch (TypeMismatchException ex) {
				propertyVetoExceptionsException.addTypeMismatchException(ex);
			} catch (MethodInvocationException ex) {
				propertyVetoExceptionsException.addMethodInvocationException(ex);
			}
		}   // for each property

		// 如果遇到个别异常, 则抛出复合异常
		if (propertyVetoExceptionsException.getExceptionCount() > 0) {
			throw propertyVetoExceptionsException;
		}
	}


	/**
	 * @see BeanWrapper#getPropertyValue(String)
	 */
	public Object getPropertyValue(String propertyName) throws BeansException {
		if (isNestedProperty(propertyName)) {
			BeanWrapper nestedBw = getBeanWrapperForNestedProperty(propertyName);
			logger.debug("Final path in nested property value '" + propertyName + "' is '"
					+ getFinalPath(propertyName) + "'");
			return nestedBw.getPropertyValue(getFinalPath(propertyName));
		}

		PropertyDescriptor pd = getPropertyDescriptor(propertyName);
		Method readMethod = pd.getReadMethod();
		if (readMethod == null) {
			throw new FatalBeanException("Cannot get scalar property [" + propertyName + "]: not readable", null);
		}
		if (logger.isDebugEnabled())
			logger.debug("About to invoke read method ["
					+ readMethod + "] on object of class '" + object.getClass().getName() + "'");
		try {
			return readMethod.invoke(object, null);
		} catch (InvocationTargetException ex) {
			throw new FatalBeanException("Getter for property [" + propertyName + "] threw exception", ex);
		} catch (IllegalAccessException ex) {
			throw new FatalBeanException("Illegal attempt to get property [" + propertyName + "] threw exception", ex);
		}
	}

	/**
	 * 获取索引属性的值
	 *
	 * @param propertyName 要获取其值的属性的名称
	 * @param index        属性的从0开始的索引
	 * @return 属性的值
	 * @throws BeansException 如果有致命异常
	 */
	public Object getIndexedPropertyValue(String propertyName, int index) throws BeansException {
		PropertyDescriptor pd = getPropertyDescriptor(propertyName);
		if (!(pd instanceof IndexedPropertyDescriptor))
			throw new FatalBeanException("Cannot get indexed property value for [" + propertyName
					+ "]: this property is not an indexed property", null);
		Method m = ((IndexedPropertyDescriptor) pd).getIndexedReadMethod();
		if (m == null)
			throw new FatalBeanException("Cannot get indexed property [" + propertyName
					+ "]: not readable", null);
		try {
			return m.invoke(object, new Object[]{new Integer(index)});
		} catch (InvocationTargetException ex) {
			throw new FatalBeanException("getter for indexed property [" + propertyName + "] threw exception", ex);
		} catch (IllegalAccessException ex) {
			throw new FatalBeanException("illegal attempt to get indexed property ["
					+ propertyName + "] threw exception", ex);
		}
	}

	/**
	 * getProperties方法.
	 *
	 * @return PropertyDescriptor[] 包装目标的属性描述符
	 * @throws BeansException 如果无法获得属性描述符
	 */
	public PropertyDescriptor[] getProperties() throws BeansException {
		return cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
	}


	/**
	 * @see BeanWrapper#getPropertyDescriptor(String)
	 */
	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		return cachedIntrospectionResults.getPropertyDescriptor(propertyName);
	}

	/**
	 * @see BeanWrapper#isReadableProperty(String)
	 */
	public boolean isReadableProperty(String propertyName) {
		try {
			return getPropertyDescriptor(propertyName).getReadMethod() != null;
		} catch (BeansException ex) {
			// 不存在, 所以不可读
			return false;
		}
	}

	/**
	 * @see BeanWrapper#isWritableProperty(String)
	 */
	public boolean isWritableProperty(String propertyName) {
		try {
			return getPropertyDescriptor(propertyName).getWriteMethod() != null;
		} catch (BeansException ex) {
			// 不存在, 所以不可写
			return false;
		}
	}

	/**
	 * Invoke a method
	 *
	 * @see BeanWrapper#invoke(String, Object[])
	 */
	public Object invoke(String methodName, Object[] args) throws BeansException {
		try {
			MethodDescriptor md = this.cachedIntrospectionResults.getMethodDescriptor(methodName);
			if (logger.isDebugEnabled())
				logger.debug("About to invoke method [" + methodName + "]");
			Object returnVal = md.getMethod().invoke(this.object, args);
			if (logger.isDebugEnabled())
				logger.debug("Successfully invoked method [" + methodName + "]");
			return returnVal;
		} catch (InvocationTargetException ex) {
			//if (ex.getTargetException() instanceof ClassCastException)
			//	throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
			throw new MethodInvocationException(ex.getTargetException(), methodName);
		} catch (IllegalAccessException ex) {
			throw new FatalBeanException("Illegal attempt to invoke method [" + methodName + "] threw exception", ex);
		} catch (IllegalArgumentException ex) {
			throw new FatalBeanException("Illegal argument to method [" + methodName + "] threw exception", ex);
		}
	}

	/**
	 * @see BeanWrapper#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		return cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
	}

	//---------------------------------------------------------------------
	// Bean event support
	//---------------------------------------------------------------------

	/**
	 * @see BeanWrapper#addVetoableChangeListener(VetoableChangeListener)
	 */
	public void addVetoableChangeListener(VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.addVetoableChangeListener(l);
	}

	/**
	 * @see BeanWrapper#removeVetoableChangeListener(VetoableChangeListener)
	 */
	public void removeVetoableChangeListener(VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.removeVetoableChangeListener(l);
	}

	/**
	 * @see BeanWrapper#addVetoableChangeListener(String, VetoableChangeListener)
	 */
	public void addVetoableChangeListener(String propertyName, VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.addVetoableChangeListener(propertyName, l);
	}

	/**
	 * @see BeanWrapper#removeVetoableChangeListener(String, VetoableChangeListener)
	 */
	public void removeVetoableChangeListener(String propertyName, VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.removeVetoableChangeListener(propertyName, l);
	}

	/**
	 * @see BeanWrapper#addPropertyChangeListener(PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.addPropertyChangeListener(l);
	}

	/**
	 * @see BeanWrapper#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.removePropertyChangeListener(l);
	}

	/**
	 * @see BeanWrapper#addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.addPropertyChangeListener(propertyName, l);
	}

	/**
	 * @see BeanWrapper#removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.removePropertyChangeListener(propertyName, l);
	}

	/**
	 * @see BeanWrapper#isEventPropagationEnabled()
	 */
	public boolean isEventPropagationEnabled() {
		return eventPropagationEnabled;
	}

	/**
	 * 禁用事件传播可提高性能
	 *
	 * @param flag 是否应该启用事件传播.
	 */
	public void setEventPropagationEnabled(boolean flag) {
		this.eventPropagationEnabled = flag;
		// 如果尚未初始化, 则延迟初始化对事件的支持
		if (eventPropagationEnabled && (vetoableChangeSupport == null || propertyChangeSupport == null)) {
			vetoableChangeSupport = new VetoableChangeSupport(object);
			propertyChangeSupport = new PropertyChangeSupport(object);
		}
	}


	//---------------------------------------------------------------------
	// Diagnostics
	//---------------------------------------------------------------------

	/**
	 * 这种方法代价很昂贵! 仅用于诊断和调试原因, 不用于生产
	 *
	 * @return 描述此对象状态的字符串
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("BeanWrapperImpl: eventPropagationEnabled=" + eventPropagationEnabled
					+ " wrapping [" + getWrappedInstance().getClass() + "]; ");
			PropertyDescriptor pds[] = getPropertyDescriptors();
			if (pds != null) {
				for (int i = 0; i < pds.length; i++) {
					Object val = getPropertyValue(pds[i].getName());
					String valStr = (val != null) ? val.toString() : "null";
					sb.append(pds[i].getName() + "={" + valStr + "}");
				}
			}
		} catch (Exception ex) {
			sb.append("exception encountered: " + ex);
		}
		return sb.toString();
	}

}
