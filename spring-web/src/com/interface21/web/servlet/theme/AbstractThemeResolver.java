package com.interface21.web.servlet.theme;

import com.interface21.web.servlet.ThemeResolver;

/**
 * ThemeResolver实现的抽象基类.
 * 提供对默认主题名称的支持.
 *
 * @author Juergen Hoeller
 * @since 17.06.2003
 */
public abstract class AbstractThemeResolver implements ThemeResolver {

	public final static String ORIGINAL_DEFAULT_THEME_NAME = "theme";

	private String defaultThemeName = ORIGINAL_DEFAULT_THEME_NAME;

	/**
	 * 设置默认主题的名称.
	 *
	 * @param defaultThemeName 新的默认主题名称
	 */
	public void setDefaultThemeName(String defaultThemeName) {
		this.defaultThemeName = defaultThemeName;
	}

	/**
	 * 返回默认主题的名称.
	 *
	 * @return 默认主题名称
	 */
	public String getDefaultThemeName() {
		return defaultThemeName;
	}

}
