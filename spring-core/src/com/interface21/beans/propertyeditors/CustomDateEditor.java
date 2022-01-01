package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Date的PropertyEditor, 支持自定义DateFormat.
 *
 * <p>这不是要用作系统PropertyEditor, 而是用作自定义controller
 * 代码中特定于语言环境的日期编辑器, 用于将用户输入的日期字符串解析
 * 为bean的Date属性, 并在UI表单中呈现它们.
 *
 * <p>在web MVC代码中, 此编辑器通常将在BaseCommandController的initBinder
 * 方法的实现中使用binder.registerCustomEditor调用进行注册.
 *
 * @author Juergen Hoeller
 * @see com.interface21.validation.DataBinder#registerCustomEditor
 * @see com.interface21.web.servlet.mvc.BaseCommandController#initBinder
 * @since 28.04.2003
 */
public class CustomDateEditor extends PropertyEditorSupport {

	private final DateFormat dateFormat;

	private final boolean allowEmpty;

	/**
	 * 创建一个新实例, 使用给定的DateFormat进行解析和渲染.
	 * <p>allowEmpty参数指出是否应允许空字符串进行解析, 即将其解释为null值.
	 * 否则, 在这种情况下会抛出IllegalArgumentException.
	 *
	 * @param dateFormat 用于解析和呈现的DateFormat
	 * @param allowEmpty 如果应该允许空字符串
	 */
	public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
	}

	/**
	 * 使用指定的DateFormat从给定文本中解析Date.
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && text.trim().equals("")) {
			// treat empty String as null value
			setValue(null);
		} else {
			try {
				setValue(this.dateFormat.parse(text));
			} catch (ParseException ex) {
				throw new IllegalArgumentException("Could not parse date: " + ex.getMessage());
			}
		}
	}

	/**
	 * 使用指定的DateFormat将Date格式化为String.
	 */
	public String getAsText() {
		return this.dateFormat.format((Date) getValue());
	}

}
