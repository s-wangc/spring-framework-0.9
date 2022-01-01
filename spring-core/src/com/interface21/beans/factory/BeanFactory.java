/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package com.interface21.beans.factory;

import com.interface21.beans.BeansException;

/**
 * 接口将由包含许多bean定义的对象实现, 每个bean定义由一个String名称唯一标识.
 * 可以获得任何这些对象的独立实例(Prototype设计模式), 或者可以获得单个
 * 共享实例(Singleton设计模式的高级替代方案). 将返回哪种类型的实例取决于
 * bean工厂配置 - API是相同的. Singleton方法在实践中更有用, 更常见.
 * <p/>这种方法的要点是BeanFactory是应用程序组件的中央注册表, 并集中配置
 * 应用程序组件(例如, 单个对象不再需要读取属性文件). 有关此方法的优点的讨论,
 * 请参见"Expert One-on-One J2EE"的第4章和第11章.
 * <br/>通常, BeanFactory将加载存储在配置源(例如XML文档)中的bean定义,并使用
 * com.interface21.beans包来配置bean. 但是, 实现可以直接在Java代码中返回它
 * 根据需要创建的Java对象.
 * 对于定义的存储方式没有任何限制: LDAP, RDBMS, XML, properties文件等. 鼓励
 * 实现支持bean之间的引用, 无论是Singletons或Prototypes.
 *
 * @author Rod Johnson
 * @since 13 April 2001
 * @version $RevisionId$
 */
public interface BeanFactory {

	/**
	 * 返回给定bean名称的实例(可能是共享的或独立的).
	 * 此方法允许使用bean工厂来替代Singleton或Prototype设计模式.
	 * <br/>请注意, 调用者应保留对返回对象的引用. 无法保证此方法的实现效率.
	 * 例如, 它可能是同步的, 或者可能需要运行PDBMS查询.
	 *
	 * @param name 要返回的bean的名称
	 * @return bean的实例
	 * @throws NoSuchBeanDefinitionException 如果没有这样的bean定义
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * 返回给定bean名称的实例(可能是共享的或独立的).
	 * 如果bean不是所需的类型, 则通过抛出异常来提供类型安全性的度量.
	 * 此方法允许使用bean工厂来替代Singleton或Prototype设计模式.
	 * <br/>请注意, 调用者应保留对返回对象的引用. 无法保证此方法的实现效率.
	 * 例如, 它可能是同步的,  或者可能需要运行PDBMS查询.
	 *
	 * @param name         要返回的bean的名称
	 * @param requiredType 输入bean可能匹配的类型. 可以是实际类的接口或超类. 例如,
	 *                     如果值为Object.class, 无论返回实例的类是什么, 此方法都将成功.
	 * @return bean的实例
	 * @throws BeanNotOfRequiredTypeException 如果bean不是所需的类型
	 * @throws NoSuchBeanDefinitionException  如果没有这样的bean定义
	 */
	Object getBean(String name, Class requiredType) throws BeansException;

	/**
	 * 这个bean是单例吗? getBean()总是会返回相同的对象吗?
	 *
	 * @param name 要查询的bean的名称
	 * @return 这个bean是单例吗
	 * @throws NoSuchBeanDefinitionException 如果没有具有给定name的bean
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 如果已定义, 则返回给定bean名称的别名.
	 *
	 * @param name 用于检查别名的bean的名称
	 * @return 别名, 如果没有, 则返回空数组
	 */
	String[] getAliases(String name);

}
