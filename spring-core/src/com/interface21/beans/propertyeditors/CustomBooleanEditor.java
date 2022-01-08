package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

/**
 * Boolean属性的属性编辑器.
 *
 * <p>这不是要用作系统PropertyEditor, 而是用作自定义controller代码
 * 中特定于语言环境的布尔编辑器, 用于将用户输入的布尔字符串解析为bean
 * 的布尔属性, 并在UI表单中对它们进行求值.
 *
 * <p>在web MVC代码中, 此编辑器通常将在BaseCommandController的initBinder
 * 方法的实现中使用binder.registerCustomEditor调用进行注册.
 *
 * @author Juergen Hoeller
 * @since 10.06.2003
 */
public class CustomBooleanEditor extends PropertyEditorSupport {

	private boolean allowEmpty;

	/**
	 * 创建一个新实例.
	 * allowEmpty参数指出是否应允许空字符串进行解析, 即将其解释为null值.
	 * 否则, 在这种情况下会抛出IllegalArgumentException.
	 *
	 * @param allowEmpty 如果应该允许空字符串
	 */
	public CustomBooleanEditor(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && text.trim().equals("")) {
			setValue(null);
		} else if (text.equalsIgnoreCase("true")) {
			setValue(Boolean.TRUE);
		} else if (text.equalsIgnoreCase("false")) {
			setValue(Boolean.FALSE);
		} else
			throw new IllegalArgumentException("Invalid Boolean value [" + text + "]");
	}

}
