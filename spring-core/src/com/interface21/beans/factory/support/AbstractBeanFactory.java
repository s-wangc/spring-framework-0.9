/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.beans.factory.support;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.BeansException;
import com.interface21.beans.FatalBeanException;
import com.interface21.beans.MutablePropertyValues;
import com.interface21.beans.PropertyValue;
import com.interface21.beans.PropertyValues;
import com.interface21.beans.factory.BeanDefinitionStoreException;
import com.interface21.beans.factory.BeanFactory;
import com.interface21.beans.factory.BeanIsNotAFactoryException;
import com.interface21.beans.factory.BeanNotOfRequiredTypeException;
import com.interface21.beans.factory.FactoryBean;
import com.interface21.beans.factory.InitializingBean;
import com.interface21.beans.factory.Lifecycle;
import com.interface21.beans.factory.NoSuchBeanDefinitionException;

/**
 * 使BeanFactory的实现变得非常简单的抽象超类.
 * 此类使用<b>模板方法</b>设计模式.
 * 子类只能实现
 * <code>
 * getBeanDefinition(name)
 * </code>
 * 方法.
 * 此类处理运行时Bean引用的解析, FactoryBean解除引用和集合属性管理.
 *
 * @author Rod Johnson
 * @version $Id: AbstractBeanFactory.java,v 1.19 2003/06/07 15:58:34 johnsonr Exp $
 * @since 15 April 2001
 */
public abstract class AbstractBeanFactory implements BeanFactory {

	/**
	 * 用于取消引用FactoryBean并将其与工厂<i>created</i>的bean区分开来.
	 * 例如, 如果名为<code>myEjb</code>的bean是工厂, 则获取<code>&myEjb</code>
	 * 将返回工厂, 而不是返回工厂的实例.
	 */
	public static final String FACTORY_BEAN_PREFIX = "&";


	//---------------------------------------------------------------------
	// 实例数据
	//---------------------------------------------------------------------

	/**
	 * 父bean工厂, 用于bean继承支持
	 */
	private BeanFactory parentBeanFactory;

	/**
	 * 单例实例的缓存. bean name --> bean instanced
	 */
	private Map sharedInstanceCache = new HashMap();

	/**
	 * 可用于子类的Logger
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 默认父bean的名称
	 */
	protected String defaultParentBean;

	/**
	 * 从别名映射到规范bean名称
	 */
	private Map aliasMap = new HashMap();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * 创建一个新的AbstractBeanFactory
	 */
	public AbstractBeanFactory() {
	}

	/**
	 * 使用给定父级创建新的AbstractBeanFactory.
	 *
	 * @param parentBeanFactory 父bean工厂, 如果没有则为null
	 * @see #getBean
	 */
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}

	/**
	 * 返回父bean工厂, 如果没有, 则返回null.
	 */
	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// BeanFactory接口的实现
	//---------------------------------------------------------------------

	/**
	 * 此类中的所有其他方法都会调用此方法, 尽管bean可以在通过此方法实例化后进行缓存.
	 *
	 * @param name              bean的名称. 在BeanFactory中必须是唯一的
	 * @param newlyCreatedBeans 如果由另一个bean的创建触发, 则使用新创建的bean(名称, 实例)
	 *                          进行缓存, 否则为null(解析循环引用所必需的)
	 * @return 这个bean的一个新实例
	 */
	private Object createBean(String name, Map newlyCreatedBeans) throws BeansException {
		if (newlyCreatedBeans == null) {
			newlyCreatedBeans = new HashMap();
		}
		Object bean = getBeanWrapperForNewInstance(name, newlyCreatedBeans).getWrappedInstance();
		callLifecycleMethodsIfNecessary(bean, name);
		return bean;
	}

	/**
	 * 返回bean名称, 必要时删除工厂deference前缀, 并将别名解析为规范名称.
	 */
	private String transformedBeanName(String name) {
		if (name.startsWith(FACTORY_BEAN_PREFIX)) {
			name = name.substring(FACTORY_BEAN_PREFIX.length());
		}
		// Handle aliasing
		String canonicalName = (String) this.aliasMap.get(name);
		return canonicalName != null ? canonicalName : name;
	}

	/**
	 * 返回此名称是否为工厂dereference引用(以工厂dereference前缀开头)
	 */
	private boolean isFactoryDereference(String name) {
		return name.startsWith(FACTORY_BEAN_PREFIX);
	}

	/**
	 * 获取此bean名称的单例实例. 请注意, 不应该经常调用此方法: 调用者应保留实例.
	 * 因此, 整个方法在这里是同步的.
	 * TODO: 可能没有任何需要同步, 至少在我们预先实例化单例时是这样.
	 *
	 * @param pname             可能包含工厂dereference引用前缀的名称
	 * @param newlyCreatedBeans 如果由另一个bean的创建触发, 则使用新创建的bean(name, instance)
	 *                          进行缓存, 否则为null(解析循环引用所必需的)
	 */
	private final synchronized Object getSharedInstance(String pname, Map newlyCreatedBeans) throws BeansException {
		// 如果有的话, 除去dereference前缀
		String name = transformedBeanName(pname);

		Object beanInstance = this.sharedInstanceCache.get(name);
		// 如果不能从缓存中获取bean
		if (beanInstance == null) {
			logger.info("Cached shared instance of Singleton bean '" + name + "'");
			if (newlyCreatedBeans == null) {
				newlyCreatedBeans = new HashMap();
			}
			beanInstance = createBean(name, newlyCreatedBeans);
			this.sharedInstanceCache.put(name, beanInstance);
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Returning cached instance of Singleton bean '" + name + "'");
		}

		// 如果bean不是工厂, 不要让调用代码尝试取消对bean工厂的引用
		if (isFactoryDereference(pname) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(name, beanInstance);
		}

		// 现在我们有了beanInstance, 它可能是普通的bean或FactoryBean.
		// 如果它是一个FactoryBean, 我们就用它来创建一个bean实例,
		// 除非调用者真的想要一个对工厂的引用.实际上想要引用工厂.
		if (beanInstance instanceof FactoryBean) {
			if (!isFactoryDereference(pname)) {
				// 从工厂配置并返回新的bean实例
				FactoryBean factory = (FactoryBean) beanInstance;
				logger.debug("Bean with name '" + name + "' is a factory bean");
				beanInstance = factory.getObject();

				// 设置传递属性
				if (factory.getPropertyValues() != null) {
					logger.debug("Applying pass-through properties to bean with name '" + name + "'");
					new BeanWrapperImpl(beanInstance).setPropertyValues(factory.getPropertyValues());
				}
				// 初始化实际上由工厂决定
				//invokeInitializerIfNecessary(beanInstance);
			} else {
				// 用户想要工厂本身
				logger.debug("Calling code asked for BeanFactory instance for name '" + name + "'");
			}
		}    // 如果我们正在处理工厂bean

		return beanInstance;
	}

	/**
	 * 返回具有给定名称的bean, 如果未找到检查父bean工厂.
	 *
	 * @param name 要检索的bean的名称
	 */
	public final Object getBean(String name) {
		return getBeanInternal(name, null);
	}

	/**
	 * 返回具有给定名称的bean, 如果未找到则检查父bean工厂.
	 *
	 * @param name              要检索的bean的名称
	 * @param newlyCreatedBeans 如果由另一个bean的创建触发, 则使用新创建的bean(name, instance)
	 *                          进行缓存, 否则为null(解析循环引用所必需的)
	 */
	private Object getBeanInternal(String name, Map newlyCreatedBeans) {
		if (name == null)
			throw new NoSuchBeanDefinitionException(null);
		if (newlyCreatedBeans != null && newlyCreatedBeans.containsKey(name)) {
			return newlyCreatedBeans.get(name);
		}
		try {
			// 获取标准bean定义
			AbstractBeanDefinition bd = getBeanDefinition(transformedBeanName(name));
			return bd.isSingleton() ? getSharedInstance(name, newlyCreatedBeans) : createBean(name, newlyCreatedBeans);
		} catch (NoSuchBeanDefinitionException ex) {
			// not found -> check parent
			if (this.parentBeanFactory != null)
				return this.parentBeanFactory.getBean(name);
			throw ex;
		}
	}

	/**
	 * 返回给定bean的共享实例. 类似于getBeaninstance(name, requiredType).
	 *
	 * @param name         要返回的实例的名称
	 * @param requiredType 必须匹配的类型
	 * @return 给定bean的共享实例
	 * @throws BeanNotOfRequiredTypeException 如果bean不是所需类型的
	 * @throws NoSuchBeanDefinitionException  如果没有这样的bean定义
	 */
	public final Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBean(name);
		Class clazz = bean.getClass();
		if (!requiredType.isAssignableFrom(clazz))
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);
		return bean;
	}

	/**
	 * @see BeanFactory#isSingleton(String)
	 */
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		try {
			return getBeanDefinition(name).isSingleton();
		} catch (NoSuchBeanDefinitionException ex) {
			// not found -> check parent
			if (this.parentBeanFactory != null)
				return this.parentBeanFactory.isSingleton(name);
			throw ex;
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * 此类中的所有bean实例化都由此方法执行.
	 * 为此bean的新实例返回一个BeanWrapper对象.
	 * 首先查找给定bean名称的BeanDefinition.
	 * 使用递归来支持实例"inheritance".
	 */
	private BeanWrapper getBeanWrapperForNewInstance(String name, Map newlyCreatedBeans) throws BeansException {
		logger.debug("getBeanWrapperForNewInstance (" + name + ")");
		AbstractBeanDefinition bd = getBeanDefinition(name);
		logger.debug("getBeanWrapperForNewInstance definition is: " + bd);
		BeanWrapper instanceWrapper = null;
		// 如果是根bean定义
		if (bd instanceof RootBeanDefinition) {
			RootBeanDefinition rbd = (RootBeanDefinition) bd;
			instanceWrapper = rbd.getBeanWrapperForNewInstance();
		}
		// 如果是子bean定义
		else if (bd instanceof ChildBeanDefinition) {
			ChildBeanDefinition ibd = (ChildBeanDefinition) bd;
			instanceWrapper = getBeanWrapperForNewInstance(ibd.getParentName(), newlyCreatedBeans);
		}
		// 设置我们的属性值
		if (instanceWrapper == null)
			throw new FatalBeanException("Internal error for definition [" + name + "]: type of definition unknown (" + bd + ")", null);
		// 缓存新实例以便能够解析循环引用
		newlyCreatedBeans.put(name, instanceWrapper.getWrappedInstance());
		// 获取bean定义中的property子节点解析出来的属性值
		PropertyValues pvs = bd.getPropertyValues();
		applyPropertyValues(instanceWrapper, pvs, name, newlyCreatedBeans);
		return instanceWrapper;
	}

	/**
	 * 应用给定的属性值, 解析对此Bean工厂中其他bean的任何运行时引用.
	 * 必须使用深层复制, 因此我们不会永久修改此属性.
	 *
	 * @param bw                BeanWrapper包装目标对象
	 * @param pvs               new property values
	 * @param name              传递baen名称以获得更好的异常信息
	 * @param newlyCreatedBeans 如果由另一个bean的创建触发, 则使用新创建的bean(name, instance)
	 *                          进行缓存, 否则为null(解析循环引用所必需的)
	 */
	private void applyPropertyValues(BeanWrapper bw, PropertyValues pvs, String name, Map newlyCreatedBeans) throws BeansException {
		if (pvs == null)
			return;

		MutablePropertyValues deepCopy = new MutablePropertyValues(pvs);
		// 获取property子节点中的所有属性
		PropertyValue[] pvals = deepCopy.getPropertyValues();

		// 将mutable中(从bean定义中解析出来的property子节点)能替换的都替换掉
		for (int i = 0; i < pvals.length; i++) {
			PropertyValue pv = new PropertyValue(pvals[i].getName(), resolveValueIfNecessary(bw, newlyCreatedBeans, pvals[i]));
			// 更新deepCopy中的数据
			deepCopy.setPropertyValueAt(pv, i);
		}

		// 设置我们的(可能是massaged)deepCopy
		try {
			bw.setPropertyValues(deepCopy);
		} catch (FatalBeanException ex) {
			// 通过显示上下文来改进消息
			throw new FatalBeanException("Error setting property on bean [" + name + "]", ex);
		}
	}

	/**
	 * 给定一个PropertyValue, 返回一个值, 必要时解析对工厂中其他bean的任何引用.
	 * 值可能是:
	 * <li>普通对象或null, 在这种情况下它是独立的.
	 * <li>RuntimeBeanReference, 必须解析它.
	 * <li>A ManagedList. 这是一个特殊的集合, 可能包含需要解析的RuntimeBeanReferences.
	 * <li>一个ManagedMap. 在这种情况下, 该值可能是必须解析的引用.
	 * 如果值是简单对象, 但属性采用Collection类型, 则必须将该值放在List中.
	 */
	private Object resolveValueIfNecessary(BeanWrapper bw, Map newlyCreatedBeans, PropertyValue pv)
			throws BeansException {
		Object val;

		// 现在, 我们必须检查每个PropertyValue, 看看它是否需要解析对另一个bean的运行时引用.
		// 如果是这样, 我们将尝试实例化bean并设置引用
		if (pv.getValue() != null && (pv.getValue() instanceof RuntimeBeanReference)) {
			RuntimeBeanReference ref = (RuntimeBeanReference) pv.getValue();
			val = resolveReference(pv.getName(), ref, newlyCreatedBeans);
		} else if (pv.getValue() != null && (pv.getValue() instanceof ManagedList)) {
			// 从managed list转换. 这是一个可能包含运行时bean引用的特殊容器.
			// 可能需要解析引用
			ManagedList l = (ManagedList) pv.getValue();
			for (int j = 0; j < l.size(); j++) {
				if (l.get(j) instanceof RuntimeBeanReference) {
					l.set(j, resolveReference(pv.getName(), (RuntimeBeanReference) l.get(j), newlyCreatedBeans));
				}
			}
			val = l;
		} else if (pv.getValue() != null && (pv.getValue() instanceof ManagedMap)) {
			// 从managed map转换. 这是一个特殊容器, 可以包含运行时bean引用作为值.
			// 可能需要解析引用.
			ManagedMap mm = (ManagedMap) pv.getValue();
			Iterator keys = mm.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Object value = mm.get(key);
				if (value instanceof RuntimeBeanReference) {
					mm.put(key, resolveReference(pv.getName(), (RuntimeBeanReference) value, newlyCreatedBeans));
				}
			}    // 遍历managed map中的每个key
			val = mm;
		} else {
			// 这是一个普通的属性. 复制它.
			val = pv.getValue();
		}

		// 如果是数组类型, 我们可能需要一个集合类型. 我们先从ManagedList开始.
		// 我们可能还需要从字符串转换数组元素.
		// 考虑重构成BeanWrapperImpl吗?
		if (val != null && val instanceof ManagedList && bw.getPropertyDescriptor(pv.getName()).getPropertyType().isArray()) {
			// 这是一个数组
			Class arrayClass = bw.getPropertyDescriptor(pv.getName()).getPropertyType();
			Class componentType = arrayClass.getComponentType();
			List l = (List) val;

			try {
				Object[] arr = (Object[]) Array.newInstance(componentType, l.size());
				for (int i = 0; i < l.size(); i++) {
					// TODO hack: BWI cast
					Object newval = ((BeanWrapperImpl) bw).doTypeConversionIfNecessary(bw.getWrappedInstance(), pv.getName(), null, l.get(i), componentType);
					arr[i] = newval;
				}
				val = arr;
			} catch (ArrayStoreException ex) {
				throw new BeanDefinitionStoreException("Cannot convert array element from String to " + componentType, ex);
			}
		}

		return val;
	}

	/**
	 * 解析对工厂中另一个bean的引用
	 */
	private Object resolveReference(String name, RuntimeBeanReference ref, Map newlyCreatedBeans) {
		try {
			// 尝试解析bean引用
			logger.debug("Resolving reference from bean [" + name + "] to bean [" + ref.getBeanName() + "]");
			Object bean = getBeanInternal(ref.getBeanName(), newlyCreatedBeans);
			// 创建一个包含bean引用的新PropertyValue对象
			return bean;
		} catch (BeansException ex) {
			throw new FatalBeanException("Can't resolve reference to bean [" + ref.getBeanName() + "] while setting properties on bean [" + name + "]", ex);
		}
	}

	/**
	 * 给bean一个机会, 现在它的所有属性都设置好了, 并且有机会知道它拥有的bean工厂
	 * (这个对象). 这意味着检查bean是否实现了InitializingBean and/of Lifecycle,
	 * 如果实现了, 则调用必要的回调.
	 *
	 * @param bean 我们可能需要初始化新的bean实例
	 * @param name 工厂中的bean. 用于调试输出.
	 */
	private void callLifecycleMethodsIfNecessary(Object bean, String name) throws BeansException {
		if (bean instanceof InitializingBean) {
			logger.debug("configureBeanInstance calling afterPropertiesSet on bean with name '" + name + "'");
			try {
				((InitializingBean) bean).afterPropertiesSet();
			} catch (BeansException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new FatalBeanException("afterPropertiesSet on with name '" + name + "' threw an exception", ex);
			}
		}

		if (bean instanceof Lifecycle) {
			logger.debug("configureBeanInstance calling setBeanFactory() on Lifecycle bean with name '" + name + "'");
			try {
				((Lifecycle) bean).setBeanFactory(this);
			} catch (BeansException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new FatalBeanException("Lifecycle method on bean with name '" + name + "' threw an exception", ex);
			}
		}
	}

	/**
	 * 子类使用的便捷方法.
	 * 解析class, 即使遍历父级, 如果子级定义也是如此.
	 *
	 * @param bd 我们要检查的BeanDefinition. 此BeanDefinition实际上可能
	 *           不包含该类 - 此方法可能需要导航其祖先以查找该类
	 * @return 这个bean的Class
	 */
	protected final Class getBeanClass(AbstractBeanDefinition bd) {
		if (bd instanceof RootBeanDefinition)
			return ((RootBeanDefinition) bd).getBeanClass();
		else if (bd instanceof ChildBeanDefinition) {
			ChildBeanDefinition cbd = (ChildBeanDefinition) bd;
			try {
				return getBeanClass(getBeanDefinition(cbd.getParentName()));
			} catch (NoSuchBeanDefinitionException ex) {
				throw new FatalBeanException("Shouldn't happen: BeanDefinition store corrupted: cannot resolve parent " + cbd.getParentName());
			}
		}
		throw new FatalBeanException("Shouldn't happen: BeanDefinition " + bd + " is Neither a rootBeanDefinition or a ChildBeanDefinition");
	}

	/**
	 * 给定一个bean名称, 创建一个别名. 这必须尊重prototype/singleton行为.
	 * 我们通常使用此方法来支持XML ID中的非法名称(用于bean名称).
	 *
	 * @param name  bean的名称
	 * @param alias 别名, 其行为与bean名称相同
	 */
	public final void registerAlias(String name, String alias) {
		logger.debug("Creating alias '" + alias + "' for bean with name '" + name + "'");
		this.aliasMap.put(alias, name);
	}

	public final String[] getAliases(String name) {
		List aliases = new ArrayList();
		for (Iterator it = this.aliasMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry) it.next();
			if (entry.getValue().equals(name)) {
				aliases.add(entry.getKey());
			}
		}
		return (String[]) aliases.toArray(new String[aliases.size()]);
	}


	//---------------------------------------------------------------------
	// 具体子类实现的抽象方法
	//---------------------------------------------------------------------

	/**
	 * 此方法必须由具体子类定义, 以实现<b>模板方法</b> GoF设计模式.
	 * <br>子类通常应该实现缓存, 因为每次请求bean时此类都会调用此方法.
	 *
	 * @param beanName 要为其查找定义的bean的名称
	 * @return 此原型名称的BeanDefinition. 决不能返回null.
	 * @throws NoSuchBeanDefinitionException 如果无法解析bean定义
	 */
	protected abstract AbstractBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

}
