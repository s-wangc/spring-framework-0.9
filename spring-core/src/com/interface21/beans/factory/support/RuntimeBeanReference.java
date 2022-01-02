package com.interface21.beans.factory.support;

/**
 * 不可变占位符类, 当PropertyValue对象引用此工厂中要在运行时解析的另一个bean时,
 * 用于该对象的值.
 *
 * @author Rod Johnson
 */
public class RuntimeBeanReference {

	private final String beanName;

	/**
	 * 为给定的bean name创建一个新的RuntimeBeanReference.
	 *
	 * @param beanName 目标bean的名称
	 */
	public RuntimeBeanReference(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * 返回目标bean名称.
	 *
	 * @return 目标bean的名称.
	 */
	public String getBeanName() {
		return beanName;
	}
}