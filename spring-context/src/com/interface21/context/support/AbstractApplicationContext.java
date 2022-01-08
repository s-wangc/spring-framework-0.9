/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package com.interface21.context.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.ListableBeanFactory;
import com.interface21.beans.factory.NoSuchBeanDefinitionException;
import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextAware;
import com.interface21.context.ApplicationContextException;
import com.interface21.context.ApplicationEvent;
import com.interface21.context.ApplicationEventMulticaster;
import com.interface21.context.ApplicationListener;
import com.interface21.context.ContextOptions;
import com.interface21.context.MessageSource;
import com.interface21.context.MessageSourceResolvable;
import com.interface21.context.NestingMessageSource;
import com.interface21.context.NoSuchMessageException;
import com.interface21.util.StringUtils;


/**
 * ApplicationContext的部分实现. 不强制要求用于配置的存储类型, 而是实现通用功能.
 *
 * <p>此类使用模板方法设计模式, 需要具体的子类来实现受保护的抽象方法.
 *
 * <p>context options可以作为默认bean工厂中的bean提供, 名为"contextOptions".
 *
 * <p>message source可以作为默认bean工厂中的bean提供, 名称为"messageSource".
 * 否则, 消息解析将委托给父上下文.
 *
 * @author Rod Johnson
 * @version $Revision: 1.22 $
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see #OPTIONS_BEAN_NAME
 * @see #MESSAGE_SOURCE_BEAN_NAME
 * @since January 21, 2001
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

	/**
	 * 工厂中options bean的名称. 如果未提供, 则将使用默认的ContextOptions实例.
	 *
	 * @see ContextOptions
	 */
	public static final String OPTIONS_BEAN_NAME = "contextOptions";

	/**
	 * 工厂中MessageSource bean的名称.
	 * 如果未提供, 则将消息解析委托给父类.
	 *
	 * @see MessageSource
	 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

	//---------------------------------------------------------------------
	// 实例数据
	//---------------------------------------------------------------------

	/**
	 * 此类使用的Log4j logger. 可用于子类.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 父context
	 */
	private ApplicationContext parent;

	/**
	 * Display name
	 */
	private String displayName = getClass().getName() + ";hashCode=" + hashCode();

	/**
	 * 此context启动时的系统时间(以毫秒为单位)
	 */
	private long startupTime;

	/**
	 * 处理配置的特殊bean
	 */
	private ContextOptions contextOptions;

	/**
	 * 事件发布中使用的辅助类.
	 * TODO: 可以将其参数化为JavaBean(指定了可分辨名称),
	 * 从而为事件发布启用不同的线程使用策略.
	 */
	private ApplicationEventMulticaster eventMulticaster = new ApplicationEventMulticasterImpl();

	/**
	 * 我们将此接口的实现委托给MessageSource辅助程序
	 */
	private MessageSource messageSource;

	/**
	 * 共享对象的哈希表, 由String作为key
	 */
	private Map sharedObjects = new HashMap();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * 创建一个没有父项的新AbstractApplicationContext.
	 */
	public AbstractApplicationContext() {
	}

	/**
	 * 使用给定的父context创建一个新的AbstractApplicationContext.
	 *
	 * @param parent 父context
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		this.parent = parent;
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContext
	//---------------------------------------------------------------------

	/**
	 * 返回父context, 如果没有父, 则返回null, 这是context层次结构的根.
	 *
	 * @return 父context, 如果没有父, 则为null
	 */
	public ApplicationContext getParent() {
		return parent;
	}

	/**
	 * 子类可以在构造函数之后调用它来设置父级.
	 * 请注意, 不应该更改父级:
	 * 只有在创建此类的对象时父级不可用, 才应该在以后设置父级.
	 *
	 * @param ac parent context
	 */
	protected void setParent(ApplicationContext ac) {
		this.parent = ac;
	}

	/**
	 * 返回此context的友好名称
	 *
	 * @return context的显示名称
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * 为了避免无限的构造函数链接, 只有具体的类在其构造函数中采用这种方法, 然后调用此方法
	 */
	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * 首次加载此context时返回的时间戳
	 *
	 * @return 首次加载context时的时间戳(ms)
	 */
	public final long getStartupDate() {
		return startupTime;
	}

	/**
	 * 返回context options. 这些控制reloading等.
	 *
	 * @return context options
	 */
	public final ContextOptions getOptions() {
		return this.contextOptions;
	}

	/**
	 * 加载或重新加载配置.
	 *
	 * @throws ApplicationContextException 如果配置无效或找不到, 或者配置已经加载并且
	 *                                     重新加载DYNAMIC CLASSLOADER ISSUE...子类来获取classloader!?
	 */
	public final void refresh() throws ApplicationContextException {
		if (this.contextOptions != null && !this.contextOptions.isReloadable())
			throw new ApplicationContextException("Forbidden to reload config");

		this.startupTime = System.currentTimeMillis();

		refreshBeanFactory();
		if (getBeanDefinitionCount() == 0)
			logger.warn("No beans defined in ApplicationContext: " + getDisplayName());
		else
			logger.info(getBeanDefinitionCount() + " beans defined in ApplicationContext: " + getDisplayName());

		configureAllManagedObjects();
		refreshListeners();

		try {
			loadOptions();
		} catch (BeansException ex) {
			throw new ApplicationContextException("Unexpected error loading context options", ex);
		}

		try {
			this.messageSource = (MessageSource) getBeanFactory().getBean(MESSAGE_SOURCE_BEAN_NAME);
			// 设置父message source(如果适用), 如果消息源是此context中定义的,
			// 则不在父context中定义
			if (this.parent != null && (this.messageSource instanceof NestingMessageSource) &&
					Arrays.asList(getBeanFactory().getBeanDefinitionNames()).contains(MESSAGE_SOURCE_BEAN_NAME)) {
				((NestingMessageSource) this.messageSource).setParent(this.parent);
			}
		} catch (NoSuchBeanDefinitionException ex) {
			logger.warn("No MessageSource found for: " + getDisplayName());
			// 使用空message source来接受getMessage调用
			this.messageSource = new StaticMessageSource();
		}

		onRefresh();
		publishEvent(new ContextRefreshedEvent(this));
	}

	/**
	 * 回调方法, 可以重写该方法以添加特定于context的刷新工作.
	 *
	 * @throws ApplicationContextException 如果刷新时出错
	 */
	protected void onRefresh() throws ApplicationContextException {
		// 对于子类: 默认情况下不执行任何操作.
	}

	/**
	 * 调用此方法之前必须加载BeanFactory
	 */
	private void loadOptions() throws BeansException {
		if (this.contextOptions == null) {
			// 尝试从bean加载
			try {
				this.contextOptions = (ContextOptions) getBeanFactory().getBean(OPTIONS_BEAN_NAME);
			} catch (NoSuchBeanDefinitionException ex) {
				logger.info("No options bean (\"" + OPTIONS_BEAN_NAME + "\") found: using default");
				this.contextOptions = new ContextOptions();
			}
		}
	}

	/**
	 * 在context中的所有对象上调用setApplicationContext()回调.
	 * 这涉及到实例化对象. 只会热切地实例化单例.
	 */
	private void configureAllManagedObjects() throws ApplicationContextException {
		logger.info("Configuring singleton beans in context");
		String[] beanNames = getBeanDefinitionNames();
		logger.debug("Found " + beanNames.length + " listeners in bean factory: names=[" +
				StringUtils.arrayToDelimitedString(beanNames, ",") + "]");
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			if (isSingleton(beanName)) {
				try {
					Object bean = getBeanFactory().getBean(beanName);
					configureManagedObject(bean);
				} catch (BeansException ex) {
					throw new ApplicationContextException("Couldn't instantiate object with name '" + beanName + "'", ex);
				}
			}
		}
	}

	/**
	 * 添加实现listener的bean作为listeners.
	 * 不影响其他listener, 可以添加而不必成为bean.
	 */
	private void refreshListeners() throws ApplicationContextException {
		logger.info("Refreshing listeners");
		String[] listenerNames = getBeanDefinitionNames(ApplicationListener.class);
		logger.debug("Found " + listenerNames.length + " listeners in bean factory: names=[" +
				StringUtils.arrayToDelimitedString(listenerNames, ",") + "]");
		for (int i = 0; i < listenerNames.length; i++) {
			String beanName = listenerNames[i];
			try {
				Object bean = getBeanFactory().getBean(beanName);
				ApplicationListener l = (ApplicationListener) bean;
				addListener(l);
				logger.info("Bean listener added: [" + l + "]");
			} catch (BeansException ex) {
				throw new ApplicationContextException("Couldn't load config listener with name '" + beanName + "'", ex);
			}
		}
	}

	/**
	 * 如果对象是context-aware, 则为其提供此对象的引用.
	 * 请注意, ApplicationContextAware接口的实现必须检查自己是否已经初始化为resp.
	 * 如果要执行重新初始化.
	 *
	 * @param o 如果它实现了ApplicationContextAware接口, 则调用对象上的setApplicationContext()方法
	 */
	protected void configureManagedObject(Object o) throws ApplicationContextException {
		if (o instanceof ApplicationContextAware) {
			logger.debug("Setting application context on ApplicationContextAware object [" + o + "]");
			ApplicationContextAware aca = (ApplicationContextAware) o;
			aca.setApplicationContext(this);
		}
	}

	/**
	 * 将给定事件发布给所有listener.
	 *
	 * @param event 要发布的事件. 该事件可能是特定于application的, 也可以是标准框架事件.
	 */
	public final void publishEvent(ApplicationEvent event) {
		logger.debug("Publishing event in context [" + getDisplayName() + "]: " + event.toString());
		this.eventMulticaster.onApplicationEvent(event);
		if (this.parent != null)
			parent.publishEvent(event);
	}

	/**
	 * 添加一个listener. 任何作为listener的bean都会自动添加.
	 */
	protected void addListener(ApplicationListener l) {
		this.eventMulticaster.addApplicationListener(l);
	}

	/**
	 * 此实现通过getResourceByPath支持完全限定的URL和适当的(文件)路径.
	 * 如果getResourceByPath返回null, 则抛出FileNotFoundException.
	 *
	 * @see #getResourceByPath
	 */
	public final InputStream getResourceAsStream(String location) throws IOException {
		try {
			// try URL
			URL url = new URL(location);
			logger.debug("Opening as URL: " + location);
			return url.openStream();
		} catch (MalformedURLException ex) {
			// no URL -> try (file) path
			InputStream in = getResourceByPath(location);
			if (in == null) {
				throw new FileNotFoundException("Location '" + location + "' isn't a URL and cannot be interpreted as (file) path");
			}
			return in;
		}
	}

	/**
	 * 返回给定(文件)路径上资源的将输入流.
	 * <p>默认实现支持文件路径, 可以是绝对路径, 也可以是相对于应用程序的
	 * 工作目录的相对路径. 这应该适用于独立立实现, 但可以重写, 例如, 针对容器的实现.
	 *
	 * @param path path to the resource
	 * @return 指定资源的InputStream, 如果找不到则可以为null(而不是抛出异常)
	 * @throws IOException 打开指定资源时的异常
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		return new FileInputStream(path);
	}

	/**
	 * 此实现返回Java VM的工作目录.
	 * 这应该适用于独立实现, 但可以针对容器的实现进行重写.
	 */
	public String getResourceBasePath() {
		return (new File("")).getAbsolutePath() + File.separatorChar;
	}

	public synchronized Object sharedObject(String key) {
		return this.sharedObjects.get(key);
	}

	public synchronized void shareObject(String key, Object o) {
		logger.info("Set shared object '" + key + "'");
		this.sharedObjects.put(key, o);
	}

	public synchronized Object removeSharedObject(String key) {
		logger.info("Removing shared object '" + key + "'");
		Object o = this.sharedObjects.remove(key);
		if (o == null) {
			logger.warn("Shared object '" + key + "' not present; could not be removed");
		} else {
			logger.info("Removed shared object '" + key + "'");
		}
		return o;
	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource
	//---------------------------------------------------------------------

	/**
	 * 尝试解析该message. 如果找不到message, 则返回默认message.
	 *
	 * @param code           要查找的code, 例如'calculator.noRateSet'. 鼓励此类用户将message
	 *                       名称基于相关的完全限定类名, 从而避免冲突并确保最大程度的清晰性.
	 * @param args           将填充消息中的参数的参数数组(参数在消息中看起来像"{0}", "{1,date}", "{2,time}"),
	 *                       如果没有则为null.
	 * @param locale         要在其中进行查找操作的locale
	 * @param defaultMessage 查找失败时返回的字符串
	 * @return 如果查找成功, 则返回已解析的消息; 否则返回作为参数传递的默认消息;
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return this.messageSource.getMessage(code, args, defaultMessage, locale);
	}

	/**
	 * 尝试解析该message. 如果找不到message, 则视为错误.
	 *
	 * @param code   要查找的code, 例如'calculator.noRateSet'
	 * @param args   将填充消息中的参数的参数数组(参数在消息中看起来像"{0}", "{1,date}", "{2,time}"),
	 *               如果没有则为null.
	 * @param locale 要在其中进行查找操作的locale
	 * @return message
	 * @throws NoSuchMessageException 在任何locale中都找不到
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, args, locale);
	}

	/**
	 * <b>使用传入的<code>MessageSourceResolvable</code>arg中包含的所有属性
	 * (<code>locale</code>属性除外)</b>,
	 * 尝试从<code>Context</code>中包含的<code>MessageSource</code>解析消息.<p>
	 * <p>
	 * 注意: 我们必须对此方法抛出<code>NoSuchMessageException</code>, 因为在调用此方法时,
	 * 我们无法确定<code>defaultMessage</code>属性是否为null.
	 *
	 * @param resolvable value对象, 存储正确解析消息所需的4个属性.
	 * @param locale     Locale用作"driver", 以确定要返回的消息.
	 * @return message Resolved message.
	 * @throws NoSuchMessageException 在任何locale中都找不到
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(resolvable, locale);
	}

	//---------------------------------------------------------------------
	// Implementation of BeanFactory
	//---------------------------------------------------------------------

	/**
	 * 尝试在层次结构中查找bean实例.
	 */
	public Object getBean(String name) throws BeansException {
		Object bean = getBeanFactory().getBean(name);
		configureManagedObject(bean);
		return bean;
	}

	public Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBeanFactory().getBean(name, requiredType);
		configureManagedObject(bean);
		return bean;
	}

	/**
	 * 这个bean是单例吗? getBean()总是会返回相同的对象吗?
	 *
	 * @param name 要查询的bean的name
	 * @return 这个bean是单例吗
	 * @throws NoSuchBeanDefinitionException 如果没有给定name的bean
	 */
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	/**
	 * 如果已定义, 则返回给定bean名称的别名.
	 *
	 * @param name 用于检查别名的bean的name
	 * @return 别名, 如果没有, 则返回空数组
	 */
	public String[] getAliases(String name) {
		return getBeanFactory().getAliases(name);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	/**
	 * 返回工厂中定义的bean数
	 *
	 * @return 工厂中定义的bean数量
	 */
	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	/**
	 * 返回此工厂中定义的所有bean的名称
	 *
	 * @return 此工厂中定义的所有bean的name.
	 * 如果没有定义bean, 则返回空String[], 而不是null.
	 */
	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	/**
	 * 返回与给定对象类型匹配的bean的name(包括子类).
	 *
	 * @param type 要匹配的类或接口
	 * @return 与给定对象类型匹配的bean的name(包括子类). 永远不会返回null.
	 */
	public String[] getBeanDefinitionNames(Class type) {
		return getBeanFactory().getBeanDefinitionNames(type);
	}

	/**
	 * 显示有关此context的信息
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("ApplicationContext: displayName=**" + displayName + "'**; ");
		sb.append("class=[" + getClass().getName() + "]; ");
		sb.append("BeanFactory={" + getBeanFactory() + "}; ");
		sb.append("} MessageSource={" + this.messageSource + "}; ");
		sb.append("ContextOptions={" + this.contextOptions + "}; ");
		sb.append("Startup date=" + new Date(startupTime) + "; ");
		if (this.parent == null)
			sb.append("ROOT of ApplicationContext hierarchy");
		else
			sb.append("Parent={" + this.parent + "}");
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// 必须由子类实现的抽象方法
	//---------------------------------------------------------------------

	/**
	 * 子类必须实现此方法才能执行实际的配置加载.
	 */
	protected abstract void refreshBeanFactory() throws ApplicationContextException;

	/**
	 * 未实现的接口方法. 子类必须有效地实现它, 以便可以重复调用它而不会降低性能.
	 *
	 * @return 此应用程序context的默认BeanFactory
	 */
	protected abstract ListableBeanFactory getBeanFactory();

}
