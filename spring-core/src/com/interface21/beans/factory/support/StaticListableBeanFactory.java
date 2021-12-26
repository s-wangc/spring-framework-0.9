package com.interface21.beans.factory.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.BeanNotOfRequiredTypeException;
import com.interface21.beans.factory.ListableBeanFactory;
import com.interface21.beans.factory.NoSuchBeanDefinitionException;

/**
 * 仅限单例.
 * 允许以编程方式按name注册bean.
 * 主要用于测试.
 *
 * @author Rod Johnson
 * @since 06-Jan-03
 */
public class StaticListableBeanFactory implements ListableBeanFactory {

	/**
	 * 存放bean名称到bean实例映射的Map
	 */
	private Map beans = new HashMap();


	/**
	 * @see com.interface21.beans.factory.ListableBeanFactory#getBeanDefinitionCount()
	 */
	public int getBeanDefinitionCount() {
		return beans.size();
	}

	/**
	 * @see com.interface21.beans.factory.ListableBeanFactory#getBeanDefinitionNames()
	 */
	public String[] getBeanDefinitionNames() {
		return (String[]) beans.keySet().toArray();
	}

	/**
	 * @see com.interface21.beans.factory.ListableBeanFactory#getBeanDefinitionNames(Class)
	 */
	public String[] getBeanDefinitionNames(Class type) {

		Set keys = beans.keySet();
		List matches = new LinkedList();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			Class clazz = beans.get(name).getClass();
			if (type.isAssignableFrom(clazz)) {
				//log4jCategory.debug("Added " + name + " of type " + type);
				matches.add(name);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	/**
	 * @see com.interface21.beans.factory.BeanFactory#getBean(String, Class)
	 */
	public Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBean(name);

		if (!requiredType.isAssignableFrom(bean.getClass()))
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);

		return bean;
	}

	/**
	 * @see com.interface21.beans.factory.BeanFactory#getBean(String)
	 */
	public Object getBean(String name) throws BeansException {
		Object bean = this.beans.get(name);
		if (bean == null)
			throw new NoSuchBeanDefinitionException(name);
		return bean;
	}

	/**
	 * 添加一个新的单例bean
	 */
	public void addBean(String name, Object bean) {
		this.beans.put(name, bean);
	}

	/**
	 * @see com.interface21.beans.factory.BeanFactory#isSingleton(java.lang.String)
	 */
	public boolean isSingleton(String name) {
		return true;
	}

	public String[] getAliases(String name) {
		return null;
	}

}
