
package com.interface21.beans;

/**
 * 包含0个或更多PropertyValue的对象, 包含一个更新.
 *
 * @author Rod Johnson
 * @version $Id: PropertyValues.java,v 1.1.1.1 2003/02/11 08:10:11 johnsonr Exp $
 * @since 13 May 2001
 */
public interface PropertyValues {

	/**
	 * 返回此对象中保存的PropertyValue对象的数组.
	 *
	 * @return 此对象中保存的PropertyValue对象的数组.
	 */
	PropertyValue[] getPropertyValues();

	/**
	 * 是否有此属性的propertyValue对象?
	 *
	 * @param propertyName 我们感兴趣的属性的名称
	 * @return 是否有此属性的propertyValue对象?
	 */
	boolean contains(String propertyName);

	/**
	 * 返回具有给定名称的属性值
	 *
	 * @param propertyName 要搜索的名称
	 * @return pv or null
	 */
	PropertyValue getPropertyValue(String propertyName);

	/**
	 * 返回自上一个PropertyValues以来所做的更改.
	 * 子类也应该重新equals.
	 *
	 * @param old 旧属性值
	 * @return PropertyValues更新或新属性. 如果没有更改, 则返回空的PropertyValues.
	 */
	PropertyValues changesSince(PropertyValues old);

}
