/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.beans.factory.support;

import com.interface21.beans.PropertyValues;

/**
 * 内部BeanFactory实现类. 此抽象基类定义了BeanFactory类型.
 * 使用Factorybean定制返回application bean时的行为.
 *
 * <p> BeanDefinition描述了一个bean实例, 它具有属性值以及由具体类
 * 或子接口提供的更多信息.
 *
 * <p>配置完成后, BeanFactory将能够返回对BeanDefinition定义的对象的直接引用.
 *
 * @author Rod Johnson
 * @version $Revision: 1.6 $
 */
public abstract class AbstractBeanDefinition {

	/**
	 * 这是一个单例bean?
	 */
	private boolean singleton;

	/**
	 * Property map
	 */
	private PropertyValues pvs;

	/**
	 * 创建新的BeanDefinition
	 *
	 * @param pvs bean的属性
	 */
	protected AbstractBeanDefinition(PropertyValues pvs, boolean singleton) {
		this.pvs = pvs;
		this.singleton = singleton;
	}

	protected AbstractBeanDefinition() {
		this.singleton = true;
	}

	/**
	 * 这是一个<b>Singleton</b>, 在所有调用中返回一个共享实例, 或BeanFactory
	 * 是否应用<b>Prototype</b>设计模式, 每个调用者请求实例获取一个独立实例?
	 * 如何定义将取决于BeanFactory.
	 * "Singletons" 是最常见的类型.
	 *
	 * @return 这是否是一个单例
	 */
	public final boolean isSingleton() {
		return singleton;
	}

	public void setPropertyValues(PropertyValues pvs) {
		this.pvs = pvs;
	}

	/**
	 * 返回要应用于此bean的新实例的PropertyValues.
	 *
	 * @return 要应用于此bean的新实例的PropertyValues
	 */
	public PropertyValues getPropertyValues() {
		return pvs;
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof AbstractBeanDefinition))
			return false;
		AbstractBeanDefinition obd = (AbstractBeanDefinition) other;
		return this.singleton = obd.singleton &&
				this.pvs.changesSince(obd.pvs).getPropertyValues().length == 0;
	}

}
