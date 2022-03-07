package com.interface21.web.servlet.theme;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.web.util.WebUtils;

/**
 * 在自定义设置的情况下, 使用发送回用户的cookie的ThemeResolver实现, 并回退到固定的locale.
 * 这对于没有使用session的无状态应用尤其有用.
 *
 * <p>因此, 自定义controller可以通过调用setTheme来覆盖用户的主题, 例如响应某个主题更改请求.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 */
public class CookieThemeResolver extends AbstractThemeResolver {

	public static final String THEME_REQUEST_ATTRIBUTE_NAME = CookieThemeResolver.class.getName() + ".THEME";

	public static final String DEFAULT_COOKIE_NAME = CookieThemeResolver.class.getName() + ".THEME";

	public static final int DEFAULT_COOKIE_MAX_AGE = Integer.MAX_VALUE;

	private String cookieName = DEFAULT_COOKIE_NAME;

	private int cookieMaxAge = DEFAULT_COOKIE_MAX_AGE;

	/**
	 * 使用给定的名称作为主题cookie name.
	 */
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public String getCookieName() {
		return cookieName;
	}

	/**
	 * 使用给定的最大有效期(以秒为单位)指定local设置的cookie.
	 * 有用的特殊值: -1...不持久, 在客户端关闭时删除
	 */
	public void setCookieMaxAge(int cookieMaxAge) {
		this.cookieMaxAge = cookieMaxAge;
	}

	public int getCookieMaxAge() {
		return cookieMaxAge;
	}

	public String resolveThemeName(HttpServletRequest request) {
		// 检查主题以获得已准备好的响应. 预设主题
		String theme = (String) request.getAttribute(THEME_REQUEST_ATTRIBUTE_NAME);
		if (theme != null)
			return theme;

		// 检索cookie值
		Cookie cookie = WebUtils.getCookie(request, getCookieName());

		if (cookie != null) {
			return cookie.getValue();
		}

		// fallback
		return getDefaultThemeName();
	}

	public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
		Cookie cookie = null;
		if (themeName != null) {
			// 设置request属性并添加cookie
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
			cookie = new Cookie(getCookieName(), themeName);
			cookie.setMaxAge(getCookieMaxAge());
		} else {
			// 将request属性设置为fallback主题并删除cookie
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, getDefaultThemeName());
			cookie = new Cookie(getCookieName(), "");
			cookie.setMaxAge(0);
		}
		response.addCookie(cookie);
	}

}
