package com.interface21.web.servlet;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于Web的locale设置解析策略的接口, 允许通过request进行locale设置解析,
 * 并通过request和response修改locale设置.
 *
 * <p>此接口允许基于request, sesssion, cookie等实现. 默认实现是
 * AcceptHeaderLocaleResolver, 只需使用相应的HTTP请求头提供的locale设置.
 *
 * @author Juergen Hoeller
 * @see com.interface21.web.servlet.i18n.AcceptHeaderLocaleResolver
 * @since 27.02.2003
 */
public interface LocaleResolver {

	/**
	 * 通过给定request解析当前locale设置.
	 * 在任何情况下都应该返回默认locale作为回退.
	 *
	 * @param request 用于解析的请求
	 * @return 当前locale
	 */
	Locale resolveLocale(HttpServletRequest request);

	/**
	 * 将当前localse设置为给定的locale设置.
	 *
	 * @param request  用于修改locale的request
	 * @param response 用于设置locale的response
	 * @param locale   新的locale
	 */
	void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale);

}
