package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 任何Number子类(如Integer, Long, Float, Double)的属性编辑器.
 * 使用给定的NumberFormat进行(特定于语言环境)解析和呈现.
 *
 * <p>这不是要用作系统PropertyEditor, 而是用作自定义controller代码
 * 中特定于语言环境的数字编辑器, 用于将用户输入的数字字符串解析为
 * bean的Number属性, 并在UI表单中呈现它们.
 *
 * <p>在web MVC代码中, 此编辑器通常将在BaseCommandController的initBinder
 * 方法的实现中使用binder.registerCustomEditor调用进行注册.
 *
 * @author Juergen Hoeller
 * @since 06.06.2003
 */
public class CustomNumberEditor extends PropertyEditorSupport {

	private Class numberClass;

	private NumberFormat numberFormat;

	private final boolean allowEmpty;

	/**
	 * 创建一个新实例, 使用给定的NumberFormat进行解析和渲染.
	 * <p>allowEmpty参数指出是否应允许空字符串进行解析, 即将其解释为null值.
	 * 否则, 在这种情况下会抛出IllegalArgumentException.
	 *
	 * @param numberClass  要生成的数字子类
	 * @param numberFormat 用于解析和呈现的NumberFormat
	 * @param allowEmpty   如果应该允许空字符串
	 * @throws IllegalArgumentException 如果指定了无效的numberClass
	 */
	public CustomNumberEditor(Class numberClass, NumberFormat numberFormat, boolean allowEmpty)
			throws IllegalArgumentException {
		if (!Number.class.isAssignableFrom(numberClass)) {
			throw new IllegalArgumentException("Property class must be a subclass of Number");
		}
		this.numberClass = numberClass;
		this.numberFormat = numberFormat;
		this.allowEmpty = allowEmpty;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && text.trim().equals("")) {
			setValue(null);
		} else {
			try {
				Number number = this.numberFormat.parse(text);
				if (this.numberClass.isInstance(number)) {
					setValue(number);
				} else if (this.numberClass.equals(Long.class)) {
					setValue(new Long(number.longValue()));
				} else if (this.numberClass.equals(Integer.class)) {
					setValue(new Integer(number.intValue()));
				} else if (this.numberClass.equals(Double.class)) {
					setValue(new Double(number.doubleValue()));
				} else if (this.numberClass.equals(Float.class)) {
					setValue(new Float(number.floatValue()));
				} else {
					throw new IllegalArgumentException("Cannot convert [" + text + "] to [" + this.numberClass + "]");
				}
			} catch (ParseException ex) {
				throw new IllegalArgumentException("Cannot parse number: " + ex.getMessage());
			}
		}
	}

	public String getAsText() {
		return this.numberFormat.format(getValue());
	}

}
