package com.interface21.web.servlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interface21.beans.MutablePropertyValues;
import com.interface21.beans.PropertyValue;
import com.interface21.beans.PropertyValues;
import com.interface21.util.StringUtils;

/**
 * 从ServletConfig参数创建的propertyvalues实现.
 * 一旦初始化, 这个类就不可变.
 *
 * @author Rod Johnson
 */
class ServletConfigPropertyValues implements PropertyValues {

	protected final Log logger = LogFactory.getLog(getClass());
	/**
	 * PropertyValues代理. 我们使用代理而不是简单地将MutablePropertyValues子类化,
	 * 因为我们不想公开MutablePropertyValues的update方法. 初始化后, 此类是不可变的.
	 */
	private MutablePropertyValues mutablePropertyValues;

	/**
	 * 创建一个新的PropertyValues对象
	 *
	 * @param config 我们将使用的从中获取PropertyValues的ServletConfig
	 * @throws ServletException 不应从此方法中抛出
	 */
	public ServletConfigPropertyValues(ServletConfig config) throws ServletException {
		this(config, null);
	}

	/**
	 * 创建一个新的PropertyValues对象
	 *
	 * @param config             我们将使用的从中获取PropertyValues的ServletConfig
	 * @param requiredProperties 我们需要的属性名称数组, 我们不能接受默认值
	 * @throws ServletException 如果缺少任何必需的属性
	 */
	public ServletConfigPropertyValues(ServletConfig config, List requiredProperties) throws ServletException {
		// 确保我们有一个深拷贝副本
		List missingProps = (requiredProperties == null) ? new ArrayList(0) : new ArrayList(requiredProperties);

		mutablePropertyValues = new MutablePropertyValues();
		Enumeration enum = config.getInitParameterNames();
		while (enum.hasMoreElements()) {
			String property = (String) enum.nextElement();
			Object value = config.getInitParameter(property);
			mutablePropertyValues.addPropertyValue(new PropertyValue(property, value));
			// Check it off
			missingProps.remove(property);
		}

		// 如果我们仍然找不到(必须)属性, 则失败
		if (missingProps.size() > 0) {
			throw new ServletException("Initialization from ServletConfig for servlet '" + config.getServletName() + "' failed: the following required properties were missing -- (" +
					StringUtils.collectionToDelimitedString(missingProps, ", ") + ")");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Found PropertyValues in ServletConfig: " + mutablePropertyValues);
		}
	}


	/**
	 * 返回此对象中包含的PropertyValue对象的数组.
	 *
	 * @return 此对象中包含的PropertyValue对象的数组.
	 */
	public PropertyValue[] getPropertyValues() {
		// 我们只是让delegate来处理这个问题
		return mutablePropertyValues.getPropertyValues();
	}

	/**
	 * 此属性是否有propertyValue对象?
	 *
	 * @param propertyName 我们感兴趣的属性名称
	 * @return 此属性是否有propertyValue对象?
	 */
	public boolean contains(String propertyName) {
		return mutablePropertyValues.contains(propertyName);
	}

	public PropertyValue getPropertyValue(String propertyName) {
		// 把它传给delegate...
		return mutablePropertyValues.getPropertyValue(propertyName);
	}

	public PropertyValues changesSince(PropertyValues old) {
		// 把它传给delegate...
		return mutablePropertyValues.changesSince(old);
	}

}
