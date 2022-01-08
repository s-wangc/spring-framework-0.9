
package com.interface21.beans.factory;

/**
 * 接口由Bean实现, 这些bean需要在BeanFaoctory设置完
 * 所有属性后需要作出反应:
 * 例如, 执行初始化, 或仅检查是否设置了所有必需属性.
 *
 * @author Rod Johnson
 */
public interface InitializingBean {

	/**
	 * 由BeanFactory在设置了提供的所有bean属性之后调用它.
	 * <br/>此方法允许bean实例仅在设置了所有bean属性时执行初始化,
	 * 并在配置错误时抛出异常.
	 *
	 * @throws Exception 如果发生配置错误(例如未能设置基本属性)或初始化失败.
	 */
	void afterPropertiesSet() throws Exception;

}