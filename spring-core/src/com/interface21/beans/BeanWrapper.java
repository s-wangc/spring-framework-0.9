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

package com.interface21.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Map;

/**
 * Interface21 JavaBeans基础设施的中央接口. 接口将由可以操作Java bean
 * 的类实现.
 * <br/>实现类能够获取和设置属性值(单独或批量), 获取属性描述符并
 * 查询属性的readability和writability.
 * <p/>此接口支持<b>嵌套属性</b>, 可以将子属性上的属性设置为无限深度.
 * <br/>如果属性更新导致异常, 则将抛出PropertyVetoException. 遇到异常
 * 后批量更新继续, 并抛出一个异常, 将更新过程中遇到的<B>所有</B>异常包装起来.
 * <br/>BeanWrapper实现可以重复使用, 其"target"或包装对象也可以更改.
 * <br/>此接口支持添加标准JavaBeans API PropertyChangeListeners和
 * VetoableChangeListeners的功能, 而无需在目标类中提供支持代码.
 * VetoableChangeListeners可以否决个别属性更改.
 *
 * @author Rod Johnson
 * @version 1.1
 * @since 13 April 2001
 */
public interface BeanWrapper {

	/**
	 * 嵌套属性的路径分隔符.
	 * 遵循正常的Java约定:
	 * getFoo().getBar()就是foo.bar
	 */
	String NESTED_PROPERTY_SEPARATOR = ".";

	/**
	 * 设置属性值. 提供此方法仅是为了方便起见.
	 * setPropertyValue(PropertyValue)方法功能更强大, 提供了设置索引属性等功能.
	 *
	 * @param propertyName 要设置值的属性的名称
	 * @param value        the new value
	 */
	void setPropertyValue(String propertyName, Object value) throws PropertyVetoException, BeansException;

	/**
	 * 更新属性值.
	 * <b>这是更新单个属性的首选方法.</b>
	 *
	 * @param pv 包含新属性值的对象
	 */
	void setPropertyValue(PropertyValue pv) throws PropertyVetoException, BeansException;

	/**
	 * 获取属性的值
	 *
	 * @param propertyName 要获取值的属性的名称
	 * @return 属性的值.
	 * @throws FatalBeanException 如果没有这样的属性, 如果属性不可读, 或者如果属性getter抛出异常.
	 */
	Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * 获取索引属性的值
	 *
	 * @param propertyName 要获取其值的属性的名称
	 * @param index        索引从0开始
	 * @return 属性的值
	 * @throws FatalBeanException 如果没有这样的索引属性或者getter方法抛出异常.
	 */
	Object getIndexedPropertyValue(String propertyName, int index) throws BeansException;


	/**
	 * 从Map执行批量更新.
	 * 从PropertyValues批量更新功能更强大: 提供此方法是为了方便. 使用此方法无法
	 * 设置索引属性. 否则, 行为将于setPropertyValues(PropertyValues)方法相同.
	 *
	 * @param m 从中获取属性的Map. 包含属性值对象, 由属性名称作为Key.
	 * @throws BeansException
	 */
	void setPropertyValues(Map m) throws BeansException;

	/**
	 * 执行批量更新的首选方法.
	 * 请注意, 执行批量更新与执行单个更新不同, 因为如果遇到<b>可恢复</b>的错误(例如否决
	 * 的属性更改或类型不匹配, 但<b>不是</b>无效的字段名称等), 此类的实现将继续更新属性,
	 * 抛出包含所有单个错误的PropertyVetoExceptionsException. 不允许未知字段. 相当于
	 * setPropertyValues(pvs, false, null).
	 * 稍后可以检查此异常以查看所有绑定错误.
	 * 已成功更新的属性保持更改状态.
	 *
	 * @param pvs 要在目标对象上设置的PropertyValues
	 */
	void setPropertyValues(PropertyValues pvs) throws BeansException;

	/**
	 * 在完全控制行为的请下执行批量更新.
	 * 请注意, 执行批量更新与执行单个更新不同, 因为如果遇到<b>可恢复</b>的错误(例如被否决
	 * 的属性更改或类型不匹配, 但不是无效的字段名等),
	 * 抛出包含所有单个错误的PropertyVetoExceptionsException.
	 * 稍后可以检查此异常以查看所有绑定错误.
	 * 已成功更新的属性保持更改状态.
	 *
	 * @param pvs           要在目标对象上设置的PropertyValues
	 * @param ignoreUnknown 我们应该忽略未知值(在bean中找不到!?)
	 * @param pvsValidator  属性值validator. 如果它为null则忽略.
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, PropertyValuesValidator pvsValidator) throws BeansException;

	/**
	 * 获取此对象上标识的PropertyDescriptors标准JavaBeans内省.
	 *
	 * @return 在此对象上标识的PropertyDescriptors标准JavaBeans内省
	 */
	PropertyDescriptor[] getPropertyDescriptors() throws BeansException;

	/**
	 * 获取特定属性的属性描述符, 如果没有此属性, 则返回null
	 *
	 * @param propertyName 要检查状态的属性
	 * @return 特定属性的属性描述符, 如果没有此属性, 则返回null
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException;

	/**
	 * 返回此属性是否可读
	 *
	 * @param propertyName 要检查状态的属性
	 * @return 此属性是否可读
	 */
	boolean isReadableProperty(String propertyName);

	/**
	 * 返回此属性是否可写
	 *
	 * @param propertyName 要检查状态的属性
	 * @return 此属性是否可写
	 */
	boolean isWritableProperty(String propertyName);

	/**
	 * 返回此对象包装的bean.
	 * 不能为null
	 *
	 * @return 被这个对象包装的bean
	 */
	Object getWrappedInstance();

	/**
	 * 更改包装的对象. 需要实现来允许包装对象类型的更改.
	 *
	 * @param obj 我们正在操纵的包装对象
	 */
	void setWrappedInstance(Object obj) throws BeansException;

	/**
	 * 为了提高效率, 包括了这种方法. 如果实现缓存了有关该类的所有必要信息,
	 * 那么在类中实例化新实例可能比创建一个新包装器来处理新对象要快.
	 */
	void newWrappedInstance() throws BeansException;

	/**
	 * 为了提高效率, 包括了这种方法. 如果实现缓存了有关该类的所有必要信息,
	 * 那么实例化一个新的包装器来复制缓存的信息可能比再次使用内省要快得<b>多</b>.
	 * 包装的实例是独立的, 就像新的BeanWrapper一样:
	 * 只复制缓存的内省信息. <b>不</b>复制listener.
	 */
	BeanWrapper newWrapper(Object obj) throws BeansException;

	/**
	 * 返回包装对象的类的方便方法
	 *
	 * @return 包装对象的类
	 */
	Class getWrappedClass();

	/**
	 * 为给定的类型和属性或给定类型的所有属性注册给定的自定义属性编辑器.
	 *
	 * @param requiredType   属性的类型, 如果给出了属性, 则可以为null,
	 *                       但在任何情况下都应该指定以进行一致性检查
	 * @param propertyPath   属性的路径(名称或嵌套路径), 如果为给定类型的所有属性注册编辑器, 则为null
	 * @param propertyEditor 要注册的editor
	 */
	void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor);

	/**
	 * 查找给定类型和属性的自定义属性编辑器.
	 *
	 * @param requiredType 属性的类型, 如果给出了属性, 则可以为null,
	 *                     但在任何情况下都应该指定以进行一致性检查
	 * @param propertyPath 属性的路径(名称或嵌套路径), 如果为给定类型的所有属性查找编辑器, 则为null
	 * @return 已注册的编辑器, 如果没有则为null
	 */
	PropertyEditor findCustomEditor(Class requiredType, String propertyPath);

	//---------------------------------------------------------------------
	// Bean event support
	//---------------------------------------------------------------------

	/**
	 * 添加将通知属性更新的VetoableChangeListener
	 *
	 * @param l 将收到属性更新通知的VetoableChangeListener
	 */
	void addVetoableChangeListener(VetoableChangeListener l);

	/**
	 * 删除将收到属性更新通知的VetoableChangeListener
	 *
	 * @param l 要删除的VetoableChangeListener
	 */
	void removeVetoableChangeListener(VetoableChangeListener l);

	/**
	 * 添加一个VetoableChangeListener，它将收到单个属性更新的通知
	 *
	 * @param l            要添加的VetoableChangeListener
	 * @param propertyName 此listener将侦听其更新的属性的名称
	 */
	void addVetoableChangeListener(String propertyName, VetoableChangeListener l);

	/**
	 * 添加一个VetoableChangeListener, 它将收到单个属性更新的通知
	 *
	 * @param l            要添加的VetoableChangeListener
	 * @param propertyName 此listener将侦听其更新的属性的名称
	 *                     <p>
	 *                     /
	 *                     void addVetoableChangeListener(String propertyName, VetoableChangeListener l);
	 *                     <p>
	 *                     /**
	 *                     删除将收到单个属性更新的VetoableChangeListener
	 * @param l            要删除的VetoableChangeListener
	 * @param propertyName 此listener以前侦听更新的属性的名称
	 */
	void removeVetoableChangeListener(String propertyName, VetoableChangeListener l);

	/**
	 * 添加将收到属性更新通知的PropertyChangeListener
	 *
	 * @param l PropertyChangeListener通知所有属性更新
	 */
	void addPropertyChangeListener(PropertyChangeListener l);

	/**
	 * 删除以前收到属性更新通知的PropertyChangeListener
	 *
	 * @param l 要删除的PropertyChangeListener
	 */
	void removePropertyChangeListener(PropertyChangeListener l);

	/**
	 * 添加一个PropertyChangeListener, 它将收到单个属性的通知
	 *
	 * @param propertyName listener感兴趣的属性
	 * @param l            PropertyChangeListener收到有关此属性更新的通知
	 */
	void addPropertyChangeListener(String propertyName, PropertyChangeListener l);

	/**
	 * 删除已经通知单个属性更新的PropertyChangeListener
	 *
	 * @param propertyName listener感兴趣的属性
	 * @param l            要删除的PropertyChangeListener
	 */
	void removePropertyChangeListener(String propertyName, PropertyChangeListener l);

	/**
	 * 我们应该发出事件通知吗?
	 * 禁用此功能(默认情况下已启用)可以提高性能.
	 *
	 * @return 我们是否通知listener属性的更新
	 */
	boolean isEventPropagationEnabled();

	/**
	 * 启用或禁用事件传播
	 * 将保留任何现有listener, 并在重新启用事件传播时再次通知事件.
	 * 但是, 在此期间不能添加新的listener:
	 * 添加或删除listener的调用将被忽略.
	 *
	 * @param flag 我们是否通知listener属性的更新
	 */
	void setEventPropagationEnabled(boolean flag);

	/**
	 * 调用命名方法. 此接口旨在鼓励使用bean属性而不是方法, 因此在大多数情况下
	 * 不应该使用此方法, 但有必要提供一种调用命名方法的简单方法.
	 *
	 * @param methodName 要调用的方法的名称
	 * @param args       要通过的参数
	 * @return 遵循java.util.Method.invoke(). void调用返回null; 基本数据类型被包装为对象
	 */
	Object invoke(String methodName, Object[] args) throws BeansException;

}

