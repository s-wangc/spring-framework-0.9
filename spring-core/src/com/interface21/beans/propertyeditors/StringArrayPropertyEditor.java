package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import com.interface21.util.StringUtils;

/**
 * String[]类型的属性编辑器.
 * 字符串必须为CSV格式.
 * 此属性编辑器由BeanWrapperImpl注册.
 *
 * @author Rod Johnson
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

	/**
	 * @see java.beans.PropertyEditor#setAsText(String)
	 */
	public void setAsText(String s) throws IllegalArgumentException {
		String[] sa = StringUtils.commaDelimitedListToStringArray(s);
		setValue(sa);
	}

}

