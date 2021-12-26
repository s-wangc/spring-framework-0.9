/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.beans.factory.support;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.interface21.beans.BeansException;
import com.interface21.beans.FatalBeanException;
import com.interface21.beans.MutablePropertyValues;
import com.interface21.beans.PropertyValue;
import com.interface21.beans.factory.ListableBeanFactory;
import com.interface21.beans.factory.NoSuchBeanDefinitionException;
import com.interface21.beans.factory.BeanFactory;
import com.interface21.util.StringUtils;

/**
 * ListableBeanFactory的具体实现.
 * 包括从Map和ResourceBundle填充工厂, 以及逐个添加bean定义的便捷方法.
 *
 * <p>可以用作独立的bean工厂, 也可以用作自定义bean工厂的超类.
 *
 * @author Rod Johnson
 * @version $Id: ListableBeanFactoryImpl.java,v 1.8 2003/06/20 20:28:43 jhoeller Exp $
 * @since 16 April 2001
 */
public class ListableBeanFactoryImpl extends AbstractBeanFactory implements ListableBeanFactory {

	/**
	 * Map中bean定义key的前缀.
	 */
	public static final String DEFAULT_PREFIX = "beans.";

	/**
	 * 根bean定义的class属性的前缀.
	 */
	public static final String CLASS_KEY = "class";

	/**
	 * 为区分所有者而添加的特殊字符串.(singleton)=true
	 * 默认值为true.
	 */
	public static final String SINGLETON_KEY = "(singleton)";

	/**
	 * 保留的"property"表示子bean定义的父级.
	 */
	public static final String PARENT_KEY = "parent";

	/**
	 * bean名称和属性名称之间的分隔符.
	 * 我们遵守正常的Java约定.
	 */
	public static final String SEPARATOR = ".";

	/**
	 * 引用当前BeanFactory中其他bean的属性后缀:
	 * 例如 owner.dog(ref)=fido.
	 * 这是对singleton的引用还是对prototype的引用将取决于目标bean的定义.
	 */
	public static final String REF_SUFFIX = "(ref)";

	/**
	 * 引用其他bean的值之前的前缀
	 */
	public static final String REF_PREFIX = "*";


	//---------------------------------------------------------------------
	// 实例数据
	//---------------------------------------------------------------------

	/**
	 * BeanDefinition对象的Map, 由原型名称作为key
	 */
	private Map beanDefinitionHash = new HashMap();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * 创建一个新的ListableBeanFactoryImpl
	 */
	public ListableBeanFactoryImpl() {
		super();
	}

	/**
	 * 使用给定的父级创建新的ListableBeanFactoryImpl
	 */
	public ListableBeanFactoryImpl(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * 为这个BeanFactory设置默认的父bean. 如果此工厂处理的子bean定义
	 * (即没有class属性的定义)未提供parent属性, 则使用此默认值.
	 * <p>例如, 可用于view定义文件, 为所有view定义具有公共属性的父级.
	 */
	public void setDefaultParentBean(String defaultParentBean) {
		this.defaultParentBean = defaultParentBean;
	}

	/**
	 * 返回此bean工厂的默认父bean.
	 */
	public String getDefaultParentBean() {
		return defaultParentBean;
	}


	//---------------------------------------------------------------------
	// ListableBeanFactory的实现
	//---------------------------------------------------------------------

	/**
	 * @see ListableBeanFactory#getBeanDefinitionCount()
	 */
	public int getBeanDefinitionCount() {
		return beanDefinitionHash.size();
	}


	/**
	 * @see ListableBeanFactory#getBeanDefinitionNames()
	 */
	public final String[] getBeanDefinitionNames() {
		Set keys = beanDefinitionHash.keySet();
		String[] names = new String[keys.size()];
		Iterator itr = keys.iterator();
		int i = 0;
		while (itr.hasNext()) {
			names[i++] = (String) itr.next();
		}
		return names;
	}


	/**
	 * 请注意, 此方法很慢. 不要经常调用它: 最好只在应用程序初始化时使用它.
	 */
	public final String[] getBeanDefinitionNames(Class type) {
		Set keys = beanDefinitionHash.keySet();
		List matches = new LinkedList();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			Class clazz = getBeanClass((AbstractBeanDefinition) beanDefinitionHash.get(name));
			if (type.isAssignableFrom(clazz)) {
				matches.add(name);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}


	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------

	/**
	 * 子类或用户应该调用此方法来为此类注册新的bean定义.
	 * 此类中的所有其他注册方法都使用此方法.
	 * <br/>此方法不保证是线程安全的. 应该在访问任何bean实例之前调用它.
	 *
	 * @param prototypeName  要注册的bean实例的名称
	 * @param beanDefinition 要注册的bean实例的定义
	 */
	public final void registerBeanDefinition(String prototypeName, AbstractBeanDefinition beanDefinition) throws BeansException {
		beanDefinitionHash.put(prototypeName, beanDefinition);
	}


	/**
	 * 确保即使实例化了潜在的未引用的单例, 子类或调用者也应在需要此行为时调用它.
	 */
	public void preInstantiateSingletons() {
		// 确保实例化了未引用的单例
		String[] beanNames = getBeanDefinitionNames();
		// 遍历所有的bean定义, 如果该bean为单例模式, 则实例化
		for (int i = 0; i < beanNames.length; i++) {
			AbstractBeanDefinition bd = getBeanDefinition(beanNames[i]);
			if (bd.isSingleton()) {
				Object singleton = getBean(beanNames[i]);
				logger.debug("Instantiated singleton: " + singleton);
			}
		}
	}


	/**
	 * 在属性文件中注册有效的bean定义. 忽略不合格的属性.
	 *
	 * @param m      Map name -> property (String or Object). 如果来自属性文件等,
	 *               属性值将是字符串. 属性名称(key)<b>必须</b>是字符串.Class key必须是字符串.
	 *               <code>
	 *               employee.class=MyClass              // 特殊属性
	 *               //employee.abstract=true              // 这个原型无法直接实例化
	 *               employee.group=Insurance Services   // real property
	 *               employee.usesDialUp=false           // 默认值, 除非被重写
	 *               <p>
	 *               employee.manager(ref)=tony		   // 对于同一文件中定义的另一个原型的引用将检测到循环引用和未解析引用
	 *               salesrep.parent=employee
	 *               salesrep.department=Sales and Marketing
	 *               <p>
	 *               techie.parent=employee
	 *               techie.department=Software Engineering
	 *               techie.usesDialUp=true              // 重写的属性
	 *               </code>
	 * @param prefix Map中keys里面的匹配项或过滤器(以此开头的才会注册):
	 *               例如 'beans.'
	 * @return 找到的bean定义数
	 * @throws BeansException 如果尝试注册定义时出错
	 */
	public final int registerBeanDefinitions(Map m, String prefix) throws BeansException {
		if (prefix == null)
			prefix = "";
		int beanCount = 0;

		Set keys = m.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			if (key.startsWith(prefix)) {
				// Key的格式为prefix<name>.property
				String nameAndProperty = key.substring(prefix.length());
				int sepIndx = nameAndProperty.indexOf(SEPARATOR);
				// 如果找到了属性分隔符
				if (sepIndx != -1) {
					// bean名称
					String beanName = nameAndProperty.substring(0, sepIndx);
					logger.debug("Found bean name '" + beanName + "'");
					if (beanDefinitionHash.get(beanName) == null) {
						// 如果我们还没有注册它...
						registerBeanDefinition(beanName, m, prefix + beanName);
						++beanCount;
					}
				} else {
					// 忽略它: 它不是一个有效的bean名称和属性, 尽管它确实以所需的前缀开头
					logger.debug("invalid name and property '" + nameAndProperty + "'");
				}
			}    // 如果key以我们正在寻找的前缀开头
		}    // 还有更多的key

		return beanCount;
	}


	/**
	 * 获取所有属性值, 给定一个前缀(将被剥离), 并将他们定义的bean添加到具有给定名称的工厂.
	 *
	 * @param beanName 要定义的bean的名称
	 * @param m        包含字符串对的映射
	 * @param prefix   每个条目的前缀, 将被剥离
	 */
	private void registerBeanDefinition(String beanName, Map m, String prefix) throws BeansException {
		String className = null;
		String parent = null;
		boolean singleton = true;

		MutablePropertyValues pvs = new MutablePropertyValues();
		Set keys = m.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			if (key.startsWith(prefix + SEPARATOR)) {
				String property = key.substring(prefix.length() + SEPARATOR.length());
				// 如果是class
				if (property.equals(CLASS_KEY)) {
					className = (String) m.get(key);
				}
				// 如果是singleton
				else if (property.equals(SINGLETON_KEY)) {
					String val = (String) m.get(key);
					singleton = val == null || !val.toUpperCase().equals("FALSE");
				}
				// 如果是parent
				else if (property.equals(PARENT_KEY)) {
					parent = (String) m.get(key);
				}
				// 如果是ref
				else if (property.endsWith(REF_SUFFIX)) {
					// 这不是一个真正的属性, 而是对另一个原型的引用
					// 提取属性名称: 属性的形式为dog(ref)
					property = property.substring(0, property.length() - REF_SUFFIX.length());
					String ref = (String) m.get(key);

					// 不管被引用的bean是否还没有注册:
					// 这将确保在runtime时解析引用
					// 默认情况下不使用singleton
					Object val = new RuntimeBeanReference(ref);
					pvs.addPropertyValue(new PropertyValue(property, val));
				} else {
					// 普通bean属性
					Object val = m.get(key);
					if (val instanceof String) {
						String sval = (String) val;
						// 如果它以未转义的前缀开头...
						if (sval.startsWith(REF_PREFIX)) {
							// 展开引用
							String targetName = ((String) val).substring(1);
							if (sval.startsWith("**")) {
								val = targetName;
							} else {
								val = new RuntimeBeanReference(targetName);
							}
						}
					}

					pvs.addPropertyValue(new PropertyValue(property, val));
				}
			}
		}
		logger.debug(pvs.toString());

		if (parent == null)
			parent = defaultParentBean;

		if (className == null && parent == null)
			throw new FatalBeanException("Invalid bean definition. Classname or parent must be supplied for bean with name '" + beanName + "'", null);

		try {

			AbstractBeanDefinition beanDefinition = null;
			if (className != null) {
				// 如果可用的话, 使用特殊的类加载器加载类.
				// 否则依赖线程上下文类加载器.
				Class clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
				beanDefinition = new RootBeanDefinition(clazz, pvs, singleton);
			} else {
				beanDefinition = new ChildBeanDefinition(parent, pvs, singleton);
			}
			registerBeanDefinition(beanName, beanDefinition);
		} catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Cannot find class '" + className + "' for bean with name '" + beanName + "'", ex);
		}
	}


	/**
	 * 在ResourceBundle中注册bean定义. 与Map类似的语法.
	 * 此方法对于支持标准Java国际化支持非常有用.
	 */
	public final int registerBeanDefinitions(ResourceBundle rb, String prefix) throws BeansException {
		// 只需创建一个Map并调用重载方法
		Map m = new HashMap();
		Enumeration keys = rb.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			m.put(key, rb.getObject(key));
		}

		return registerBeanDefinitions(m, prefix);
	}


	//---------------------------------------------------------------------
	// 超类protected抽象方法的实现
	//---------------------------------------------------------------------

	/**
	 * @see AbstractBeanFactory#getBeanDefinition(String)
	 */
	protected final AbstractBeanDefinition getBeanDefinition(String prototypeName) throws NoSuchBeanDefinitionException {
		AbstractBeanDefinition bd = (AbstractBeanDefinition) beanDefinitionHash.get(prototypeName);
		if (bd == null)
			throw new NoSuchBeanDefinitionException(prototypeName, toString());
		return bd;
	}


	public String toString() {
		return getClass() + ": defined beans [" + StringUtils.arrayToDelimitedString(getBeanDefinitionNames(), ",") + "]";
	}

}
