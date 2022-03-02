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

package com.interface21.web.servlet;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.BeansException;
import com.interface21.beans.PropertyValues;

/**
 * javax.servlet.http.HttpServlet的简单扩展, 将其配置参数视为bean属性.
 * 对于任何类型的servlet都是一个非常方便的超类. 类型转换是自动的. 子类也可以指定必需的属性.
 * 这个servelt将请求处理留给子类, 继承了HttpServlet的默认行为.
 * <p/>此servlet超类不依赖于应用程序上下文. 但是, 它确实使用Java1.4日志模拟, 它必须
 * 由另一个组件配置.
 *
 * @author Rod Johnson
 * @version $Revision: 1.2 $
 */
public class HttpServletBean extends HttpServlet {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 可能为空. 必须作为配置参数提供给此servlet的必需属性(字符串)的list.
	 */
	private List requiredProperties = new LinkedList();

	/**
	 * 保留name信息: 对日志记录很有用
	 */
	private String identifier;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Construct a new HttpServletBean
	 */
	public HttpServletBean() {
	}

	/**
	 * Subclasses can invoke this method to specify that this property
	 * (which must match a JavaBean property they expose) is mandatory,
	 * and must be supplied as a config parameter.
	 *
	 * @param property name of the required property
	 */
	protected final void addRequiredProperty(String property) {
		requiredProperties.add(property);
	}

	/**
	 * 将配置参数映射到此servlet的bean属性, 并调用子类初始化.
	 *
	 * @throws ServletException 如果bean属性无效(或缺少必需的属性), 或者子类初始化失败.
	 */
	public final void init() throws ServletException {
		this.identifier = "Servlet with name '" + getServletConfig().getServletName() + "' ";

		logger.info(getIdentifier() + "entering init...");

		// 设置bean属性
		try {
			PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), requiredProperties);
			BeanWrapper bw = new BeanWrapperImpl(this);
			bw.setPropertyValues(pvs);
			logger.debug(getIdentifier() + "properties bound OK");

			// 让子类做他们喜欢的任何初始化
			initServletBean();
			logger.info(getIdentifier() + "configured successfully");
		} catch (BeansException ex) {
			String mesg = getIdentifier() + ": error setting properties from ServletConfig";
			logger.error(mesg, ex);
			throw new ServletException(mesg, ex);
		} catch (Throwable t) {
			// 让子抛出unchecked exceptions
			String mesg = getIdentifier() + ": initialization error";
			logger.error(mesg, t);
			throw new ServletException(mesg, t);
		}
	}

	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this servlet will have been set before this
	 * method is invoked. This default implementation does nothing.
	 *
	 * @throws ServletException if subclass initialization fails
	 */
	protected void initServletBean() throws ServletException {
		logger.debug(getIdentifier() + "NOP default implementation of initServletBean");
	}

	/**
	 * Return the name of this servlet:
	 * handy to include in log messages. Subclasses may override it if
	 * necessary to include additional information. Use like this:
	 * <code>
	 * Category.getInstance(getClass()).debug(getIdentifier() + "body of message");
	 * </code>
	 *
	 * @return the name of this servlet
	 */
	protected String getIdentifier() {
		return this.identifier;
	}

}
