/**
 * The Spring framework is distributed under the Apache
 * Software License.
 */

package com.interface21.beans.factory;

import com.interface21.beans.BeansException;
import com.interface21.beans.PropertyValues;

/**
 * 由BeanFactory中使用的对象实现的接口, 这些对象本身就是工厂.
 * 如果一个bean实现了这个接口, 它将被用作工厂, 而不是直接用作bean.
 * <br>
 * <b>注意: 实现此接口的bean不能用作普通bean.</b>
 * <br>FactoryBeans可以支持singletons和prototypes.
 *
 * @author Rod Johnson
 * @version $Id: FactoryBean.java,v 1.1 2003/03/10 07:38:49 johnsonr Exp $
 * @see com.interface21.beans.factory.BeanFactory
 * @since March 08, 2003
 */
public interface FactoryBean {

	/**
	 * 返回此工厂管理的对象的实例(可能是共享的或独立的).
	 * 与BeanFactory一样, 这允许同时支持Singleton和Prototype设计模式.
	 *
	 * @return an instance of the bean
	 */
	Object getObject() throws BeansException;


	/**
	 * 这个工厂管理的bean是singleton还是prototype?
	 * 也就是说, getBwean()总是会返回相同的对象?
	 * <br>FactoryBean的单例状态通常由拥有它的BeanFactory提供.
	 *
	 * @return 这个bean是singleton吗
	 */
	boolean isSingleton();

	/**
	 * 要传递给此工厂创建的新bean实例的属性值. 使用反射直接映射到bean实例.
	 * 这发生在工厂本身执行的任何实例配置<i>之后</i>, 是拥有BeanFactory的
	 * 控制范围内的可选步骤.
	 *
	 * @return 传递给每个新实例的PropertyValues, 如果没有要传递给实例的属性,
	 * 则返回null(默认值).
	 */
	PropertyValues getPropertyValues();

}

