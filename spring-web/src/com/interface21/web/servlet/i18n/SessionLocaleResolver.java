package com.interface21.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.web.servlet.LocaleResolver;
import com.interface21.web.util.WebUtils;

/**
 * LocaleResolver的实现, 在自定义设置的情况下, 在用户会话中使用locale属性, 并回退
 * 到accept请求头设置. 如果应用程序仍然需要用户session, 这是最合适的.
 *
 * <p>自定义controller可以通过调用setLocale覆盖用户的locale设置, 例如响应locale设置更改请求.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 */
public class SessionLocaleResolver implements LocaleResolver {

	public static final String LOCALE_SESSION_ATTRIBUTE_NAME = SessionLocaleResolver.class.getName() + ".LOCALE";

	public Locale resolveLocale(HttpServletRequest request) {
		Locale locale = (Locale) WebUtils.getSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME);
		// 特定locale设置, 或回退到请求locale设置?
		return (locale != null ? locale : request.getLocale());
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		WebUtils.setSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME, locale);
	}

}
