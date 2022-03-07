package com.interface21.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于web的主题解析策略的接口, 允许通过请求进行主题解析, 并通过request
 * 和response进行主题修改.
 *
 * <p>此接口允许基于session、cookie等实现. 默认是现实FixedThemeresolver,
 * 只需要使用配置的默认主题.
 *
 * <p>注意, 这个解析器只负责确定当前主题名. DispatcherServlet通过相应的
 * 主题源(即当前的WebApplicationContext)查找已解析主题名的主题实例.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see com.interface21.web.servlet.theme.FixedThemeResolver
 * @see com.interface21.ui.context.Theme
 * @see com.interface21.ui.context.ThemeSource
 * @since 17.06.2003
 */
public interface ThemeResolver {

	/**
	 * 通过给定的请求解析当前主题名称.
	 * 在任何情况下都应该返回默认主题作为回退.
	 *
	 * @param request 用于解析的请求
	 * @return 当前的主题名称
	 */
	String resolveThemeName(HttpServletRequest request);

	/**
	 * 将当前的themeName设置为给定的名称.
	 *
	 * @param request   用于修改主题名称的请求
	 * @param response  用于修改主题名称的响应
	 * @param themeName 新的主题名称
	 */
	void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName);

}
