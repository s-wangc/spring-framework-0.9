/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package com.interface21.context;

import java.io.InputStream;
import java.io.IOException;

import com.interface21.beans.factory.ListableBeanFactory;

/**
 * 用于为应用程序提供配置的接口.
 * 这在应用程序运行时是只读的, 但如果实现支持, 则可以重新加载.
 * <p>配置提供:
 * <ul>
 * <li>解析消息, 支持国际化的能力;
 * <li>发布事件的能力. 实现必须提供注册事件listener的方法;
 * <li>通过将对象发布到context来共享对象的能力;
 * <li>Bean工厂方法, 继承自ListableBeanFactory. 这避免了应用程序使用单例的需要.
 * <li>由context的context初始化的bean的通知, 允许与应用程序的其余部分进行通信,
 * 例如通过发布事件. BeanFactory父接口没有提供类似的机制.
 * <li>从父context继承. 后代context的定义将始终优先.例如, 这意味着
 * 一个单一的父context可以被整个Web应用程序可以使用, 而每个servlet
 * 都有自己的子context, 独立于任何其他servlet的子context.
 * </ul>
 *
 * @author Rod Johnson
 * @version $Revision: 1.8 $
 */
public interface ApplicationContext extends MessageSource, ListableBeanFactory {

	/**
	 * 返回父context, 如果没有父context, 则返回null, 这是context层次结构的根.
	 *
	 * @return 父context, 如果没有父context, 则为null
	 */
	ApplicationContext getParent();

	/**
	 * 返回此context的友好名称.
	 *
	 * @return 此context的显示名称
	 */
	String getDisplayName();

	/**
	 * 返回首次加载此context时的时间戳
	 *
	 * @return 首次加载此context时的时间戳(ms)
	 */
	long getStartupDate();

	/**
	 * 返回此context option. 这些控制reloading等.
	 * <p>ApplicationContext实现可以将ContextOptions子类化以添加其他属性. 它必须始终是一个bean.
	 *
	 * @return context option(必须不是null).
	 */
	ContextOptions getOptions();

	/**
	 * 加载或刷新配置的持久表示, 例如可以是XML文件, properties文件或关系数据库模式.
	 *
	 * @throws ApplicationContextException 如果配置无法加载
	 */
	void refresh() throws ApplicationContextException;

	/**
	 * 将应用程序事件通知给在此应用程序中注册的所有listener.
	 * 事件可以是框架事件(例如RequestHandledEvent)或特定于应用程序的事件.
	 *
	 * @param event event to publish
	 */
	void publishEvent(ApplicationEvent event);

	/**
	 * 打开指定资源的InputStream:
	 * <ul>
	 * <li>必须支持完全限定的URL, 例如"file:C:/test.dat".
	 * <li>应该支持相对文件路径, 例如"WEB-INF/test.dat".
	 * <li>可以允许绝对文件路径, "C:/test.dat".
	 * </ul>
	 * 请注意, 访问绝对文件路径最安全的方法时通过"file:" URL, 因为所有实现都必须支持这一点.
	 * <p>注意: 调用者负责关闭输入流.
	 *
	 * @param location 资源的位置
	 * @return 指定资源的InputStream
	 * @throws IOException 打开指定资源时出现异常
	 */
	InputStream getResourceAsStream(String location) throws IOException;

	/**
	 * 返回此应用程序context的相对寻址资源的基本路径. 通常, 此路径与
	 * getResourceAsStream用于计算相对路径的路径相同.
	 * <p>请注意, 如果此应用程序context没有专用的基本路径, 则此方法返回null.
	 * 因此, getResourceAsStream可能根本不支持相对路径, 或者使用多个基本路径
	 * 来计算相对路径.
	 *
	 * @return 资源基础路径(以分隔符结尾)或null
	 */
	String getResourceBasePath();

	/**
	 * 放置一个可供共享的对象. 请注意, 此方法不同步. 与Java2集合一样,
	 * 需要调用代码来确保线程安全. 而且, 这在集群中不起作用. 它类似于
	 * 将某个东西放置在ServletContext中.
	 *
	 * @param key object key
	 * @param o   object to put
	 */
	void shareObject(String key, Object o);

	/**
	 * 检索通过调用shareObject()添加的共享对象.
	 *
	 * @return object, 如果在此name下没有对象, 则返回null(这不是错误).
	 */
	Object sharedObject(String key);

	/**
	 * 删除通过调用shareObject()添加的共享对象.
	 * 如果对象为null则不执行任何操作.
	 *
	 * @param key 已添加的对象
	 * @return 如果找到则返回对象, 否则为null.
	 */
	Object removeSharedObject(String key);

}
