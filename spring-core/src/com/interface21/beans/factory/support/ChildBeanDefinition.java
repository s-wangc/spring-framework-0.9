/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.beans.factory.support;

import com.interface21.beans.PropertyValues;


/**
 * 扩展BeanDefinition接口, 用于其类由祖先定义的bean. 由父项定义的
 * PropertyValues也将是"inherited", 尽管可以通过在与子项相关联的属性
 * 值中重新定义它们来覆盖它们.
 *
 * @author Rod Johnson
 * @version $Revision: 1.2 $
 */
public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;

	/**
	 * 创建新的BeanDefinition
	 */
	public ChildBeanDefinition(String parentName, PropertyValues pvs, boolean singleton) {
		super(pvs, singleton);
		this.parentName = parentName;
	}

	/**
	 * 返回当前bean工厂中父bean定义的名称.
	 *
	 * @return 当前bean工厂中父bean定义的名称
	 */
	public String getParentName() {
		return parentName;
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof ChildBeanDefinition))
			return false;
		return super.equals(arg0) && ((ChildBeanDefinition) arg0).getParentName().equals(this.getParentName());
	}
}