/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.beans.factory.support;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.BeansException;
import com.interface21.beans.PropertyValues;

/**
 * root bean定义具有class和properties.
 *
 * @author Rod Johnson
 */
public class RootBeanDefinition extends AbstractBeanDefinition {

	/**
	 * 包装对象的类
	 */
	private Class clazz;

	/**
	 * 创建新的AbstractRootBeanDefinition
	 */
	public RootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton) {
		super(pvs, singleton);
		this.clazz = clazz;
	}

	protected RootBeanDefinition() {
	}

	protected void setBeanClass(Class clazz) {
		this.clazz = clazz;
	}

	/**
	 * JavaBean目标类名称的Setter.
	 */
	public void setBeanClassName(String classname) throws ClassNotFoundException {
		this.clazz = Class.forName(classname);
	}


	/**
	 * @return 包装的bean类
	 */
	public final Class getBeanClass() {
		return this.clazz;
	}

	/**
	 * 子类可以重写此方法, 以不同方式创建bean包装器或执行自定义预处理.
	 * 此实现直包装bean类.
	 *
	 * @return 一个新的BeanWrapper包装目标对象
	 */
	protected BeanWrapper newBeanWrapper() {
		return new BeanWrapperImpl(getBeanClass());
	}

	/**
	 * 给定一个bean wrapper, 添加listeners
	 */
	public final BeanWrapper getBeanWrapperForNewInstance() throws BeansException {
		BeanWrapper bw = newBeanWrapper();

		return bw;
	} // getBeanWrapperForNewInstance


	public String toString() {
		return "RootBeanDefinition: class is " + getBeanClass();
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof RootBeanDefinition))
			return false;
		return super.equals(arg0) && ((RootBeanDefinition) arg0).getBeanClass().equals(this.getBeanClass());
	}

} // class RootBeanDefinition