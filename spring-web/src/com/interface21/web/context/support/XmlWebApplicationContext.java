/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify. However, please
 * acknowledge the source and include the above URL in each
 * class using or derived from this code.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package com.interface21.web.context.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextException;
import com.interface21.ui.context.support.AbstractXmlUiApplicationContext;
import com.interface21.web.context.WebApplicationContext;

/**
 * 从XML文档获取配置的WebApplicationContext实现.
 *
 * <p>支持用于持配置文件查找的各种servlet上下文init参数. 默认情况下, 查找发生
 * 在Web应用程序的WEB-INF目录中, 为根上下文查找"WEB-INF/applicationContext.xml",
 * 为名称为test-servlet"的命名空间context查找"WEB-INF/test-servlet.xml"
 * (类似于为web.xml servlet名称"test"的DispatcherServlet实例).
 *
 * <p>将(file)路径解释为servlet上下文资源, 即作为Web应用程序根目录下的路径.
 * 因此, 绝对路径, 即Web应用程序根目录之外的文件, 应通过"file:" URLs访问.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Revision: 1.14 $
 */
public class XmlWebApplicationContext extends AbstractXmlUiApplicationContext implements WebApplicationContext {

	/**
	 * servlet上下文参数的名称, 该参数可以指定命名空间上下文的配置位置前缀,
	 * 可以回退到DEFAULT_CONFIG_LOCATION_PREFIX.
	 */
	public static final String CONFIG_LOCATION_PREFIX_PARAM = "contextConfigLocationPrefix";

	/**
	 * servlet上下文参数的名称, 可以指定命名空间上下文的配置位置后缀,
	 * 回退到DEFAULT_CONFIG_LOCATION_SUFFIX.
	 */
	public static final String CONFIG_LOCATION_SUFFIX_PARAM = "contextConfigLocationSuffix";

	/**
	 * servlet上下文参数的名称, 该参数可以指定根上下文的配置位置, 并回退到DEFAULT_CONFIG_LOCATION.
	 */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	/**
	 * 配置位置的默认前缀, 后跟命名空间
	 */
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";

	/**
	 * 配置位置的默认后缀, 位于命名空间后
	 */
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";

	/**
	 * 根上下文的默认配置位置.
	 */
	public static final String DEFAULT_CONFIG_LOCATION =
			DEFAULT_CONFIG_LOCATION_PREFIX + "applicationContext" + DEFAULT_CONFIG_LOCATION_SUFFIX;


	/**
	 * 此上下文的命名空间, 如果是root, 则为null
	 */
	private String namespace = null;

	/**
	 * 从中加载配置的URL
	 */
	private String configLocation;

	/**
	 * 运行此上下文的servlet上下文
	 */
	private ServletContext servletContext;

	/**
	 * 创建新的根Web应用程序上下文, 以便在整个Web应用程序中使用.
	 * 此上下文将是各个servlet上下文的父级.
	 */
	public XmlWebApplicationContext() {
		setDisplayName("Root WebApplicationContext");
	}

	/**
	 * 创建一个新的子WebApplicationContext.
	 */
	public XmlWebApplicationContext(ApplicationContext parent, String namespace) {
		super(parent);
		this.namespace = namespace;
		setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
	}


	/**
	 * @return 此上下文的命名空间, 如果是root, 则为null
	 */
	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * Initialize and attach to the given context.
	 * @param servletContext ServletContext to use to load configuration,
	 * and in which this web application context should be set as an attribute.
	 */
	public void setServletContext(ServletContext servletContext) throws ApplicationContextException {
		this.servletContext = servletContext;

		this.configLocation = getConfigLocationForNamespace();
		logger.info("Using config location '" + this.configLocation + "'");
		refresh();

		if (this.namespace == null) {
			// We're the root context
			WebApplicationContextUtils.publishConfigObjects(this);
			// Expose as a ServletContext object
			WebApplicationContextUtils.publishWebApplicationContext(this);
		}
	}

	public final ServletContext getServletContext() {
		return this.servletContext;
	}

	/**
	 * Initialize the config location for the current namespace.
	 * This can be overridden in subclasses for custom config lookup.
	 * <p>Default implementation returns the namespace with the default prefix
	 * "WEB-INF/" and suffix ".xml", if a namespace is set. For the root context,
	 * the "configLocation" servlet context parameter is used, falling back to
	 * "WEB-INF/applicationContext.xml" if no parameter is found.
	 * @return the URL or path of the configuration to use
	 */
	protected String getConfigLocationForNamespace() {
		if (getNamespace() != null) {
			String configLocationPrefix = this.servletContext.getInitParameter(CONFIG_LOCATION_PREFIX_PARAM);
			String prefix = (configLocationPrefix != null) ? configLocationPrefix : DEFAULT_CONFIG_LOCATION_PREFIX;
			String configLocationSuffix = this.servletContext.getInitParameter(CONFIG_LOCATION_SUFFIX_PARAM);
			String suffix = (configLocationSuffix != null) ? configLocationSuffix : DEFAULT_CONFIG_LOCATION_SUFFIX;
			return prefix + getNamespace() + suffix;
		} else {
			String configLocation = this.servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
			return (configLocation != null) ? configLocation : DEFAULT_CONFIG_LOCATION;
		}
	}

	/**
	 * @return 配置的URL或路径
	 */
	protected final String getConfigLocation() {
		return this.configLocation;
	}


	/**
	 * Open and return the input stream for the bean factory for this namespace.
	 * If namespace is null, return the input stream for the default bean factory.
	 * @exception IOException if the required XML document isn't found
	 */
	protected InputStream getInputStreamForBeanFactory() throws IOException {
		InputStream in = getResourceAsStream(this.configLocation);
		if (in == null) {
			throw new FileNotFoundException("Config location not found: " + this.configLocation);
		}
		return in;
	}

	/**
	 * This implementation supports file paths beneath the root
	 * of the web application.
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		if (path.charAt(0) != '/') {
			path = "/" + path;
		}
		return getServletContext().getResourceAsStream(path);
	}

	/**
	 * This implementation returns the real path of the root directory of the
	 * web application that this WebApplicationContext is associated with.
	 * @see com.interface21.context.ApplicationContext#getResourceBasePath
	 * @see javax.servlet.ServletContext#getRealPath
	 */
	public String getResourceBasePath() {
		return getServletContext().getRealPath("/");
	}

	/**
	 * @return diagnostic information
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString() + "; ");
		sb.append("config path='" + configLocation + "'; ");
		return sb.toString();
	}

}
