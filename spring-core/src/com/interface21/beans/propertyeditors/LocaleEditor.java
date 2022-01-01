package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.Locale;

import com.interface21.util.StringUtils;

/**
 * java.util.Locale的编辑器, 以直接提供locale属性.
 * 需要与Locale.toString相同的语法, 即语言+可选的国家/地区+可选的变体, 用"\"分隔
 * (例如"en", "en_US").
 *
 * @author Juergen Hoeller
 * @since 26.05.2003
 */
public class LocaleEditor extends PropertyEditorSupport {

	public void setAsText(String text) {
		String[] parts = StringUtils.delimitedListToStringArray(text, "_");
		String language = parts.length > 0 ? parts[0] : "";
		String country = parts.length > 1 ? parts[1] : "";
		String variant = parts.length > 2 ? parts[2] : "";
		setValue(language.length() > 0 ? new Locale(language, country, variant) : null);
	}

}
