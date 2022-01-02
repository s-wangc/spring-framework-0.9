
package com.interface21.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.interface21.util.StringUtils;

/**
 * PropertyValues接口的默认实现.
 * 允许对属性进行简单操作, 并提供构造函数以支持Map的深度复制和构造.
 *
 * @author Rod Johnson
 * @version $Id: MutablePropertyValues.java,v 1.2 2003/02/27 10:10:39 jhoeller Exp $
 * @since 13 May 2001
 */
public class MutablePropertyValues implements PropertyValues {

	//---------------------------------------------------------------------
	// 实例数据
	//---------------------------------------------------------------------
	/**
	 * PropertyValue对象List
	 */
	private List propertyValuesList;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * 创建一个新的空MutablePropertyValues对象.
	 * 可以使用addPropertyValue()方法添加PropertyValue对象.
	 */
	public MutablePropertyValues() {
		propertyValuesList = new ArrayList(10);
	}

	/**
	 * 深度拷贝构造函数.
	 * 保证PropertyValue引用是独立的, 但它不能深度复制当前由各个PropertyValue对象引用的对象.
	 */
	public MutablePropertyValues(PropertyValues other) {
		PropertyValue[] pvs = other.getPropertyValues();
		propertyValuesList = new ArrayList(pvs.length);
		for (int i = 0; i < pvs.length; i++)
			addPropertyValue(new PropertyValue(pvs[i].getName(), pvs[i].getValue()));
	}

	/**
	 * 从Map构造一个新的PropertyValues对象.
	 *
	 * @param map 使用属性名称作为key的属性值Map, 该属性值必须是String
	 */
	public MutablePropertyValues(Map map) {
		Set keys = map.keySet();
		propertyValuesList = new ArrayList(keys.size());
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			addPropertyValue(new PropertyValue(key, map.get(key)));
		}
	}

	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------

	/**
	 * 添加PropertyValue对象
	 *
	 * @param pv 要添加的PropertyValue对象
	 */
	public void addPropertyValue(PropertyValue pv) {
		propertyValuesList.add(pv);
	}

	/**
	 * 返回此对象中保存的PropertyValue对象的数字.
	 *
	 * @return 此对象中保存的PropertyValue对象的数组.
	 */
	public PropertyValue[] getPropertyValues() {
		return (PropertyValue[]) propertyValuesList.toArray(new PropertyValue[0]);
	}

	/**
	 * 此属性是否有propertyValue对象?
	 *
	 * @param propertyName 我们感兴趣的属性的名称
	 * @return 此属性是否有propertyValue对象?
	 */
	public boolean contains(String propertyName) {
		return getPropertyValue(propertyName) != null;
	}

	public PropertyValue getPropertyValue(String propertyName) {
		for (int i = 0; i < propertyValuesList.size(); i++) {
			PropertyValue pv = (PropertyValue) propertyValuesList.get(i);
			if (pv.getName().equals(propertyName))
				return pv;
		}
		return null;
	}

	/**
	 * 修改此对象中保存的从0开始索引的PropertyValue对象
	 */
	public void setPropertyValueAt(PropertyValue pv, int i) {
		propertyValuesList.set(i, pv);
	}

	public String toString() {
		PropertyValue[] pvs = getPropertyValues();
		StringBuffer sb = new StringBuffer("MutablePropertyValues: length=" + pvs.length + "; ");
		sb.append(StringUtils.arrayToDelimitedString(pvs, ","));
		return sb.toString();
	}

	/**
	 * @see PropertyValues#changesSince(PropertyValues)
	 */
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this)
			return changes;

		// 遍历新集合中的每个属性值
		for (int i = 0; i < this.propertyValuesList.size(); i++) {
			PropertyValue newPv = (PropertyValue) this.propertyValuesList.get(i);
			// 如果没有旧值, 请添加它
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null) {
				//System.out.println("No old pv for " + newPv.getName());
				changes.addPropertyValue(newPv);
			} else if (!pvOld.equals(newPv)) {
				// It's changed
				//System.out.println("pv changed for " + newPv.getName());
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}

}    // class MutablePropertyValues
