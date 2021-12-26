/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package com.interface21.beans.factory;

/**
 * BeanFactory的扩展将由bean工厂实现, 该工厂可以枚举所有bean实例,
 * 而不是按客户端的请求逐个尝试按名称查找bean.
 * <br/>除了getBeanDefinitionCount()之外, 此接口的方法不是为频繁
 * 调用而设计的. 实现可能很慢.
 * <br/>预加载所有bean的BeanFactory实现(例如, 基于DOM的XML工厂)
 * 可以实现此接口.
 * <br/>这个接口在Rod Johnson的"Expert One-on-One J2EE"中讨论.
 *
 * @author Rod Johnson
 * @since 16 April 2001
 */
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * 返回工厂中定义的bean数
	 *
	 * @return 工厂中定义的bean数量
	 */
	int getBeanDefinitionCount();

	/**
	 * 返回此工厂中定义的所有bean的名称
	 *
	 * @return 此工厂中定义的所有bean的名称.
	 * 如果没有定义bean, 则返回空String[], 而不是null.
	 */
	String[] getBeanDefinitionNames();

	/**
	 * 返回与给定对象类型匹配的bean的nam(包括子类).
	 *
	 * @param type 要匹配的类或接口
	 * @return 与给定对象类型(包括子类)匹配的bean的name. 永远不会返回null.
	 */
	String[] getBeanDefinitionNames(Class type);

}

