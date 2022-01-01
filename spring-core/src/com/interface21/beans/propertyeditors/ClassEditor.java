package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

/**
 * java.lang.Class的编辑器, 直接提供Class属性, 而不需要额外的类名属性.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 */
public class ClassEditor extends PropertyEditorSupport {

	public void setAsText(String text) throws IllegalArgumentException {
		Class clazz = null;
		try {
			clazz = Class.forName(text);
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Invalid class name [" + text + "]: " + ex.getMessage());
		}
		setValue(clazz);
	}

}
