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
	 * 初始化并附加到给定的context.
	 *
	 * @param servletContext 要用于加载配置ideServletContext,
	 *                       其中应该将此Web应用程序context设置为属性.
	 */
	public void setServletContext(ServletContext servletContext) throws ApplicationContextException {
		this.servletContext = servletContext;

		// 设置配置文件地址
		this.configLocation = getConfigLocationForNamespace();
		logger.info("Using config location '" + this.configLocation + "'");
		// 调用ApplicationContext的钩子函数应用配置
		refresh();

		if (this.namespace == null) {
			// 我们是root上下文
			WebApplicationContextUtils.publishConfigObjects(this);
			// 作为servletContext对象公开
			WebApplicationContextUtils.publishWebApplicationContext(this);
		}
	}

	public final ServletContext getServletContext() {
		return this.servletContext;
	}

	/**
	 * 初始化当前命名空间的配置位置. 这可以在自定义配置查到的子类中重写.
	 * <p>如果设置了命名空间, 则默认实现将返回具有默认前缀"WEB-INF/"和后缀".xml"的命名空间.
	 * 对于根context, 使用"configLocation" servlet context参数, 如果未找到参数, 则返回
	 * WEB-INF/applicationContext.xml".
	 *
	 * @return 要使用的配置的URL或路径
	 */
	protected String getConfigLocationForNamespace() {
		// 如果配置了命名空间, 那么配置文件地址为: 前缀(默认为/WEB-INF/)+命名空间+后缀(默认为.xml)
		if (getNamespace() != null) {
			// 获取contextConfigLocationPrefix这个初始化属性
			String configLocationPrefix = this.servletContext.getInitParameter(CONFIG_LOCATION_PREFIX_PARAM);
			String prefix = (configLocationPrefix != null) ? configLocationPrefix : DEFAULT_CONFIG_LOCATION_PREFIX;
			// 获取contextConfigLocationSuffix这个初始化属性
			String configLocationSuffix = this.servletContext.getInitParameter(CONFIG_LOCATION_SUFFIX_PARAM);
			String suffix = (configLocationSuffix != null) ? configLocationSuffix : DEFAULT_CONFIG_LOCATION_SUFFIX;
			return prefix + getNamespace() + suffix;
		}
		// 如果没有配置命名空间, 那么配置文件地址为"contextConfigLocation"属性的值, 默认为/WEB-INF/applicationContext.xml
		else {
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
	 * 打开并返回此命名空间的bean工厂的输入流.
	 * 如果namespace为null, 则返回默认bean工厂的输入流.
	 *
	 * @throws IOException 如果找不到所需的XML文档
	 */
	protected InputStream getInputStreamForBeanFactory() throws IOException {
		InputStream in = getResourceAsStream(this.configLocation);
		if (in == null) {
			throw new FileNotFoundException("Config location not found: " + this.configLocation);
		}
		return in;
	}

	/**
	 * 此实现支持Web应用程序根目录下的文件路径.
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		if (path.charAt(0) != '/') {
			path = "/" + path;
		}
		return getServletContext().getResourceAsStream(path);
	}

	/**
	 * 此实现返回与此WebApplicationContext关联的Web应用程序的根目录的实际路径.
	 *
	 * @see com.interface21.context.ApplicationContext#getResourceBasePath
	 * @see javax.servlet.ServletContext#getRealPath
	 */
	public String getResourceBasePath() {
		return getServletContext().getRealPath("/");
	}

	/**
	 * @return 诊断信息
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString() + "; ");
		sb.append("config path='" + configLocation + "'; ");
		return sb.toString();
	}

}
