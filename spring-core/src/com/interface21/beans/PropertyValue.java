
package com.interface21.beans;

/**
 * 类用于保存单个属性的信息和值.
 * 在此处使用一个对象, 而不是仅将所有属性存储在由属性命名作为key的map中,
 * 允许更大的灵活性, 并且在必要时处理索引属性等的能力.
 * <br/>请注意, 该值不必是最终所需的类型:
 * BeanFactory实现应该处理任何必要的转换, 因为该对象不知道它将应用到的对象的任何信息.
 *
 * @author Rod Johnson
 * @version $Id: PropertyValue.java,v 1.1.1.1 2003/02/11 08:10:11 johnsonr Exp $
 * @since 13 May 2001
 */
public class PropertyValue {

	//---------------------------------------------------------------------
	// 实例数据
	//---------------------------------------------------------------------
	/**
	 * 属性名
	 */
	private String name;

	/**
	 * 属性值
	 */
	private Object value;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * 创建新的PropertyValue
	 *
	 * @param name  属性的名称
	 * @param value 属性的值(在类型转换之前)
	 */
	public PropertyValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}


	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------

	/**
	 * 返回属性的名称
	 *
	 * @return 属性的名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 返回属性的值
	 *
	 * @return 属性的值. 可能不会发生类型转换. BeanWrapper实现负责执行类型转换.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Diagnostic method
	 */
	public String toString() {
		return "PropertyValue: name='" + name + "'; value=[" + value + "]";
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof PropertyValue))
			return false;
		PropertyValue pvOther = (PropertyValue) other;
		return this == other ||
				(this.name == pvOther.name && this.value == pvOther.value);
	}

}    // class PropertyValue
