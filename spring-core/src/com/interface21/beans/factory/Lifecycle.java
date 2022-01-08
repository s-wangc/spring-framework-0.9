/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package com.interface21.beans.factory;

/**
 * 接口将由希望其所属BeanFactory通知生命周期事件的bean实现.
 * 还提供了一种方法, 使bean可以获得其拥有的BeanFactory的引用,
 * 它们可以使用该引用来查找协作bean.
 *
 * @author Rod Johnson
 * @version $Revision: 1.1 $
 * @since 11-Mar-2003
 */
public interface Lifecycle {
	/**
	 * BeanFactory中使用的生命周期回调bean可以实现接收回调,
	 * 从而公开工厂本身. 这使他们能够从工厂获得其他bean.
	 * <br>
	 * 如果bean还实现了InitializingBean, 则在<code>afterPropertiesSet</code>方法
	 * 之后将调用Lifecycle方法.
	 *
	 * @param beanFactory 拥有BeanFactory. 不能为null. bean可以立即调用工厂中的方法.
	 * @throws Exception 这个方法可以抛出任何异常. 通常我们希望方法声明更精确的异常,
	 *                   但在这种情况下, 拥有BeanFactory的人将捕获并处理异常(将其视为致命异常), 我们希望
	 *                   通过将开发人员从捕获和包装致命异常的需要中解放出来, 使实现Lifecocle bean变得容易.
	 *                   这里抛出的异常被认为是致命的.
	 */
	void setBeanFactory(BeanFactory beanFactory) throws Exception;

	// TAKE PVS?
	//void propertyChange();

	//void dispose();

}
