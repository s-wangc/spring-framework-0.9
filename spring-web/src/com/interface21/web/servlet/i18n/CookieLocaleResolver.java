package com.interface21.web.servlet.i18n;

import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.web.servlet.LocaleResolver;
import com.interface21.web.util.WebUtils;

/**
 * LocaleResolver的实现, 它使用在自定义设置的情况下发送回用户的cookie,
 * 并回退到accept头的locale设置.
 * 这对于没有使用session的无状态应用程序尤其有用.
 *
 * <p>因此, 自定义controller可以通过调用setLocale覆盖用户的语言环境, 例如响应某个locale设置更改请求.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 */
public class CookieLocaleResolver implements LocaleResolver {

	public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";

	public static final String DEFAULT_COOKIE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";

	public static final int DEFAULT_COOKIE_MAX_AGE = Integer.MAX_VALUE;

	private String cookieName = DEFAULT_COOKIE_NAME;

	private int cookieMaxAge = DEFAULT_COOKIE_MAX_AGE;

	/**
	 * 对locale cookie使用给定的name.
	 */
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public String getCookieName() {
		return cookieName;
	}

	/**
	 * 对locale cookie使用给定的最大age(以秒为单位).
	 * 有用的特殊值: -1... 不持久, 在客户端关闭时删除
	 */
	public void setCookieMaxAge(int cookieMaxAge) {
		this.cookieMaxAge = cookieMaxAge;
	}

	public int getCookieMaxAge() {
		return cookieMaxAge;
	}

	public Locale resolveLocale(HttpServletRequest request) {
		// 检查预先分配的locale设置. 预设locale设置
		Locale locale = (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
		if (locale != null)
			return locale;

		// 检索cookie值
		Cookie cookie = WebUtils.getCookie(request, getCookieName());

		if (cookie != null) {
			// 解析cookie值
			String language = "";
			String country = "";
			String variant = "";

			StringTokenizer tokenizer = new StringTokenizer(cookie.getValue());
			if (tokenizer.hasMoreTokens())
				language = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				country = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				variant = tokenizer.nextToken();

			// 计算结果
			if (language != null) {
				locale = new Locale(language, country, variant);
				request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
				return locale;
			}
		}

		// fallback
		return request.getLocale();
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		Cookie cookie = null;
		if (locale != null) {
			// 设置request属性并添加cookie
			request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
			cookie = new Cookie(getCookieName(), locale.getLanguage() + " " + locale.getCountry() + " " + locale.getVariant());
			cookie.setMaxAge(getCookieMaxAge());
		} else {
			// 将request属性设置为回退locale设置并删除cookie
			request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, request.getLocale());
			cookie = new Cookie(getCookieName(), "");
			cookie.setMaxAge(0);
		}
		response.addCookie(cookie);
	}
}
