/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.beans.factory.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.interface21.beans.BeansException;
import com.interface21.beans.FatalBeanException;
import com.interface21.beans.MutablePropertyValues;
import com.interface21.beans.PropertyValue;
import com.interface21.beans.PropertyValues;
import com.interface21.beans.factory.BeanDefinitionStoreException;
import com.interface21.beans.factory.BeanFactory;
import com.interface21.beans.factory.support.AbstractBeanDefinition;
import com.interface21.beans.factory.support.ChildBeanDefinition;
import com.interface21.beans.factory.support.ListableBeanFactoryImpl;
import com.interface21.beans.factory.support.ManagedList;
import com.interface21.beans.factory.support.ManagedMap;
import com.interface21.beans.factory.support.RootBeanDefinition;
import com.interface21.beans.factory.support.RuntimeBeanReference;

/**
 * ListableBeanFactoryImpl的扩展, 它使用DOM读取XML文档中的bean定义.
 * 所需XML文档的结构, 元素和属性名称在此类中进行了硬编码.
 * (当然, 如果需要生成这种格式, 可以运行转换.)
 *
 * <p>"beans"不需要是XML文档的根元素:
 * 此类将解析XML文件中的所有bean定义元素.
 *
 * <p>该类向ListableBeanFactoryImpl超类注册每个bean定义, 并依赖后者对BeanFactory
 * 接口的实现. 它支持singletons, prototypes和对这两种bean中的引用.
 *
 * <p>预先实例化singletons. TODO: 这可以配置.
 *
 * @author Rod Johnson
 * @version $Id: XmlBeanFactory.java,v 1.2 2003/06/22 20:02:52 jhoeller Exp $
 * @since 15 April 2001
 */
public class XmlBeanFactory extends ListableBeanFactoryImpl {

	/**
	 * 表示true的T/F属性的值.
	 * 其他任何东西都是false. 区分大小写.
	 */
	private static final String TRUE_ATTRIBUTE_VALUE = "true";

	private static final String BEAN_ELEMENT = "bean";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String PARENT_ATTRIBUTE = "parent";

	private static final String ID_ATTRIBUTE = "id";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String SINGLETON_ATTRIBUTE = "singleton";

	private static final String DISTINGUISHED_VALUE_ATTRIBUTE = "distinguishedValue";

	private static final String NULL_DISTINGUISHED_VALUE = "null";

	private static final String PROPERTY_ELEMENT = "property";

	private static final String REF_ELEMENT = "ref";

	private static final String LIST_ELEMENT = "list";

	private static final String MAP_ELEMENT = "map";

	private static final String KEY_ATTRIBUTE = "key";

	private static final String ENTRY_ELEMENT = "entry";

	private static final String BEAN_REF_ATTRIBUTE = "bean";

	private static final String EXTERNAL_REF_ATTRIBUTE = "external";

	private static final String VALUE_ELEMENT = "value";

	private static final String PROPS_ELEMENT = "props";

	private static final String PROP_ELEMENT = "prop";


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * 创建新的XmlBeanFactory, 使用java.io读取具有给定文件名称的XML文档
	 *
	 * @param filename          包含XML文档的文件的名称
	 * @param parentBeanFactory parent bean factory
	 */
	public XmlBeanFactory(String filename, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		try {
			logger.info("Loading XmlBeanFactory from file '" + filename + "'");
			loadBeanDefinitions(new FileInputStream(filename));
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException("Can't open file [" + filename + "]", ex);
		}
	}

	/**
	 * 创建新的XmlBeanFactory, 使用java.io读取具有给定文件名的XML文档
	 *
	 * @param filename 包含XML文档的文件的名称
	 */
	public XmlBeanFactory(String filename) throws BeansException {
		this(filename, null);
	}

	/**
	 * 使用给定的输入流创建一个新的XmlBeanFactory, 它必须可以使用DOM进行解析.
	 *
	 * @param is                包含XML的InputStream
	 * @param parentBeanFactory 父bean工厂
	 * @throws BeansException
	 */
	public XmlBeanFactory(InputStream is, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		loadBeanDefinitions(is);
	}

	/**
	 * 使用给定的输入流创建一个新的XMLBeanFactory, 它必须可以使用DOM进行解析.
	 *
	 * @param is 包含XML的InputStream
	 * @throws BeansException
	 */
	public XmlBeanFactory(InputStream is) throws BeansException {
		this(is, null);
	}

	/**
	 * 从DOM文档创建新的XmlBeanFactory
	 *
	 * @param doc               已解析的DOM文档
	 * @param parentBeanFactory 父bean工厂
	 */
	public XmlBeanFactory(Document doc, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		loadBeanDefinitions(doc);
	}

	/**
	 * 从DOM文档创建新的XmlBeanFactory
	 *
	 * @param doc 已解析的DOM文档
	 */
	public XmlBeanFactory(Document doc) throws BeansException {
		this(doc, null);
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * 从该输入流加载定义并关闭它
	 */
	private void loadBeanDefinitions(InputStream is) throws BeansException {
		if (is == null)
			throw new BeanDefinitionStoreException("InputStream cannot be null: expected an XML file", null);

		try {
			logger.info("Loading XmlBeanFactory from InputStream [" + is + "]");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			logger.debug("Using JAXP implementation [" + factory + "]");
			factory.setValidating(true);
			DocumentBuilder db = factory.newDocumentBuilder();
			// 设置错误处理程序
			db.setErrorHandler(new BeansErrorHandler());
			// 设置实体解析器
			db.setEntityResolver(new BeansDtdResolver());
			Document doc = db.parse(is);
			loadBeanDefinitions(doc);
		} catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException("ParserConfiguration exception parsing XML", ex);
		} catch (SAXException ex) {
			throw new BeanDefinitionStoreException("XML document is invalid", ex);
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException("IOException parsing XML document", ex);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ex) {
				throw new FatalBeanException("IOException closing stream for XML document", ex);
			}
		}
	} // loadDefinitions (InputStream)

	/**
	 * 从给定的DOM文档加载bean定义.
	 * 所有的调用都通过这个.
	 */
	private void loadBeanDefinitions(Document doc) throws BeansException {
		Element root = doc.getDocumentElement();
		logger.debug("Loading bean definitions");
		NodeList nl = root.getElementsByTagName(BEAN_ELEMENT);
		logger.debug("Found " + nl.getLength() + " <" + BEAN_ELEMENT + "> elements defining beans");
		// 遍历所有的bean节点
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			loadBeanDefinition((Element) n);
		}

		// 让超类急切地实例化单例
		preInstantiateSingletons();
	}

	/**
	 * 解析一个元素定义: 我们知道这是一个bean元素.
	 */
	private void loadBeanDefinition(Element el) throws BeansException {
		// 获取bean的Id
		String id = getBeanId(el);
		logger.debug("Parsing bean definition with id '" + id + "'");

		// 立即创建BeanDefinition: 我们稍后将构建PropertyValues
		AbstractBeanDefinition beanDefinition;

		// 将bean节点的property子节点解析出来生成PropertyValues对象
		PropertyValues pvs = getPropertyValueSubElements(el);
		// 解析出标准bean定义
		beanDefinition = parseBeanDefinition(el, id, pvs);
		// 将BeanDefinition存放起来
		registerBeanDefinition(id, beanDefinition);

		// 解析注册bean的别名
		String name = el.getAttribute(NAME_ATTRIBUTE);
		if (name != null && !"".equals(name)) {
			// 自动创建此别名. 用于id属性中不合法的名称
			registerAlias(id, name);
		}
	}

	/**
	 * 解析标准bean定义.
	 */
	private AbstractBeanDefinition parseBeanDefinition(Element el, String beanName, PropertyValues pvs) {
		String classname = null;
		boolean singleton = true;
		if (el.hasAttribute(SINGLETON_ATTRIBUTE)) {
			// 默认值为singleton
			// 如果需要, 可以通过制作non-singleton来覆盖
			singleton = TRUE_ATTRIBUTE_VALUE.equals(el.getAttribute(SINGLETON_ATTRIBUTE));
		}
		try {
			// 获取class(类名)属性
			if (el.hasAttribute(CLASS_ATTRIBUTE))
				classname = el.getAttribute(CLASS_ATTRIBUTE);
			String parent = null;
			// 获取parent(父bean)属性
			if (el.hasAttribute(PARENT_ATTRIBUTE))
				parent = el.getAttribute(PARENT_ATTRIBUTE);
			if (classname == null && parent == null)
				throw new FatalBeanException("No classname or parent in bean definition [" + beanName + "]", null);
			if (classname != null) {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				return new RootBeanDefinition(Class.forName(classname, true, cl), pvs, singleton);
			} else {
				return new ChildBeanDefinition(parent, pvs, singleton);
			}
		} catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Error creating bean with name [" + beanName + "]: class '" + classname + "' not found", ex);
		}
	}


	/**
	 * 解析此bean元素的property子元素.
	 */
	private PropertyValues getPropertyValueSubElements(Element beanEle) {
		NodeList nl = beanEle.getElementsByTagName(PROPERTY_ELEMENT);
		MutablePropertyValues pvs = new MutablePropertyValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Element propEle = (Element) nl.item(i);
			parsePropertyElement(pvs, propEle);
		}
		return pvs;
	}

	/**
	 * 解析property元素.
	 */
	private void parsePropertyElement(MutablePropertyValues pvs, Element e) throws DOMException {
		// 获取节点的name属性
		String propertyName = e.getAttribute(NAME_ATTRIBUTE);
		if (propertyName == null || "".equals(propertyName))
			throw new BeanDefinitionStoreException("Property without a name", null);

		Object val = getPropertyValue(e);
		pvs.addPropertyValue(new PropertyValue(propertyName, val));
	}

	/**
	 * 获取bean的id(bean节点的id属性)
	 *
	 * @param e 需要获取其id属性的bean节点
	 * @return 节点的id属性
	 * @throws BeanDefinitionStoreException
	 */
	private String getBeanId(Element e) throws BeanDefinitionStoreException {
		// 如果这个节点不是bean节点, 则抛出异常
		if (!e.getTagName().equals(BEAN_ELEMENT))
			throw new FatalBeanException("Internal error: trying to treat element with tagname <"
					+ e.getTagName() + "> as a <bean> element");
		// 获取其id属性值
		String propertyName = e.getAttribute(ID_ATTRIBUTE);
		// 如果id属性值为空, 则抛出异常
		if (propertyName == null || "".equals(propertyName))
			throw new BeanDefinitionStoreException("Bean without id attribute", null);
		return propertyName;
	}

	/**
	 * 获取property元素的值. 可能是一个list.
	 */
	private Object getPropertyValue(Element e) {
		// 获取distinguishedValue属性
		String distinguishedValue = e.getAttribute(DISTINGUISHED_VALUE_ATTRIBUTE);
		if (distinguishedValue != null && distinguishedValue.equals(NULL_DISTINGUISHED_VALUE)) {
			return null;
		}

		// 只能有一个element子元素:
		// value, ref, collection
		NodeList nl = e.getChildNodes();
		Element childEle = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				if (childEle != null)
					throw new BeanDefinitionStoreException("<property> element can have only one child element, not " + nl.getLength(), null);
				childEle = (Element) nl.item(i);
			}
		}

		return parsePropertySubelement(childEle);
	}

	private Object parsePropertySubelement(Element ele) {
		// 如果是ref节点, 则创建一个运行时引用, 在运行时进行处理
		if (ele.getTagName().equals(REF_ELEMENT)) {
			// 引用此工厂中的另一个bean?
			String beanName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if ("".equals(beanName)) {
				// 对外部bean的引用(在父工厂中)?
				beanName = ele.getAttribute(EXTERNAL_REF_ATTRIBUTE);
				if ("".equals(beanName)) {
					throw new FatalBeanException("Either 'bean' or 'external' is required for a reference");
				}
			}
			return new RuntimeBeanReference(beanName);
		}
		// 如果是value标签, 直接返回其文本值
		else if (ele.getTagName().equals(VALUE_ELEMENT)) {
			// 他是一个文字值
			return getTextValue(ele);
		}
		// 如果是list标签, 则遍历其子节点, 递归解析是否为ref、value、list、map等节点
		else if (ele.getTagName().equals(LIST_ELEMENT)) {
			return getList(ele);
		}
		// 如果是map标签, 解析entry子节点, entry节点中有一个key属性, 其子节点又可能是ref、value、list、map等
		else if (ele.getTagName().equals(MAP_ELEMENT)) {
			return getMap(ele);
		}
		// 如果是prop标签, prop标签中有一个属性为key值, 节点内容为value
		else if (ele.getTagName().equals(PROPS_ELEMENT)) {
			return getProps(ele);
		}
		throw new BeanDefinitionStoreException("Unknown subelement of <property>: <" + ele.getTagName() + ">", null);
	}


	/**
	 * 返回list集合.
	 */
	private List getList(Element collectionEle) {
		NodeList nl = collectionEle.getChildNodes();
		ManagedList l = new ManagedList();
		// 遍历list的子节点
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				l.add(parsePropertySubelement(ele));
			}
		}
		return l;
	}

	/**
	 * 返回Map集合
	 * 获取其entry节点, entry节点中有一个属性为key值, 其子节点可能又是ref、value、map等
	 *
	 * @param mapEle
	 * @return
	 */
	private Map getMap(Element mapEle) {
		ManagedMap m = new ManagedMap();
		NodeList nl = mapEle.getElementsByTagName(ENTRY_ELEMENT);
		// 遍历map节点下的entry子节点
		for (int i = 0; i < nl.getLength(); i++) {
			Element entryEle = (Element) nl.item(i);
			// 获取entry节点的key属性值
			String key = entryEle.getAttribute(KEY_ATTRIBUTE);
			// TODO hack: make more robust
			NodeList subEles = entryEle.getElementsByTagName("*");
			m.put(key, parsePropertySubelement((Element) subEles.item(0)));
		}
		return m;
	}

	/**
	 * 返回Properties对象
	 *
	 * @param propsEle
	 * @return
	 */
	private Properties getProps(Element propsEle) {
		Properties p = new Properties();
		// 遍历prop标签, prop标签中有一个属性为key值, 节点内容为value
		NodeList nl = propsEle.getElementsByTagName(PROP_ELEMENT);
		for (int i = 0; i < nl.getLength(); i++) {
			Element propEle = (Element) nl.item(i);
			String key = propEle.getAttribute(KEY_ATTRIBUTE);
			String value = getTextValue(propEle);
			p.setProperty(key, value);
		}
		return p;
	}

	/**
	 * 让可怕的DOM API变得更加可忍受:
	 * 获取此元素包含的文本值
	 */
	private String getTextValue(Element e) {
		NodeList nl = e.getChildNodes();
		if (nl.item(0) == null) {
			// treat empty value as empty String
			return "";
		}
		if (nl.getLength() != 1 || !(nl.item(0) instanceof Text)) {
			throw new FatalBeanException("Unexpected element or type mismatch: " +
					"expected single node of " + nl.item(0).getClass() + " to be of type Text: "
					+ "found " + e, null);
		}
		Text t = (Text) nl.item(0);
		// This will be a String
		return t.getData();
	}


	/**
	 * 验证XML时使用的SAX ErrorHandle的私有实现.
	 */
	private class BeansErrorHandler implements ErrorHandler {

		/**
		 * 接受可恢复的错误的通知
		 *
		 * @param e
		 * @throws SAXException
		 */
		public void error(SAXParseException e) throws SAXException {
			logger.error(e);
			throw e;
		}

		/**
		 * 接受不可恢复的错误的通知
		 *
		 * @param e
		 * @throws SAXException
		 */
		public void fatalError(SAXParseException e) throws SAXException {
			throw e;
		}

		/**
		 * 接受警告的通知
		 *
		 * @param e
		 * @throws SAXException
		 */
		public void warning(SAXParseException e) throws SAXException {
			logger.warn("Ignored XML validation warning: " + e);
		}
	}

}
