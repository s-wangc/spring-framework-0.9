package com.interface21.web.servlet.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 仅使用固定主题的ThemeResolver的实现.
 * 可以通过defaultTheme属性定义固定名称.
 *
 * <p>主题: 不支持setThemeName, 因为主题已固定.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see #setDefaultThemeName
 * @since 17.06.2003
 */
public class FixedThemeResolver extends AbstractThemeResolver {

	public String resolveThemeName(HttpServletRequest request) {
		return getDefaultThemeName();
	}

	public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
		throw new IllegalArgumentException("Cannot change theme - use a different theme resolution strategy");
	}

}
