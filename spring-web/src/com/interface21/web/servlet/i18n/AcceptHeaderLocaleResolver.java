package com.interface21.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.web.servlet.LocaleResolver;

/**
 * LocaleResolver的实现, 它只使用HTTP请求的"accept-language"请求头
 * 中指定的主locale(即客户端浏览器发送的locale, 通常是客户端操作系统的locale).
 *
 * <p>注意: 不支持setLocale, 因为无法更改accepte请求头.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 */
public class AcceptHeaderLocaleResolver implements LocaleResolver {

	public Locale resolveLocale(HttpServletRequest request) {
		return request.getLocale();
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new IllegalArgumentException("Cannot change HTTP accept header - use a different locale resolution strategy");
	}

}
