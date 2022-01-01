package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.Properties;

import com.interface21.beans.MutablePropertyValues;

/**
 * PropertyValues对象的编辑器不是GUI编辑器.
 * <br>注意: 此编辑器必须在JavaBeans API中注册才能使用.
 * 此包中的编辑器由BeanWrapperImpl注册.
 * <br>java.util.Properties文档中定义了所需的格式.
 * 每个属性必须在新行上.
 * <br>
 * 本实现依赖于PropertiesEditor.
 *
 * @author Rod Johnson
 */
public class PropertyValuesEditor extends PropertyEditorSupport {


	/**
	 * @see java.beans.PropertyEditor#setAsText(String)
	 */
	public void setAsText(String s) throws IllegalArgumentException {
		PropertiesEditor pe = new PropertiesEditor();
		pe.setAsText(s);
		Properties props = (Properties) pe.getValue();
		setValue(new MutablePropertyValues(props));
	}

}

