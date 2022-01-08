package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * java.util.Properties对象的编辑器. 处理从String到Properties对象的转换.
 * 不是GUI编辑器.
 * <br>注意: 此编辑器必须先在JavaBean API中注册才能使用. 此包中的编辑器由
 * BeanWrapperImpl注册.
 * <br>java.util.Properties文档中定义了所需的格式.
 * 每个属性必须在新行上.
 *
 * @author Rod Johnson
 * @version $Id: PropertiesEditor.java,v 1.5 2003/05/21 21:15:20 johnsonr Exp $
 */
public class PropertiesEditor extends PropertyEditorSupport {

	/**
	 * 这些字符串中的任何一个, 如果它们在空格之后或在一行中的第一个, 都意味着该行是注释,
	 * 应该被忽略.
	 */
	private final static String COMMENT_MARKERS = "#!";

	/**
	 * @see java.beans.PropertyEditor#setAsText(String)
	 */
	public void setAsText(String s) throws IllegalArgumentException {

		if (s == null)
			throw new IllegalArgumentException("Cannot set properties to null");

		Properties props = load(s);
		//parse(s);
		setValue(props);
	}


	/**
	 * 自己解析字符串.
	 * Orion 1.6问题的解决方法
	 *
	 * @param s
	 * @return Properties
	 */
	private Properties parse(String s) {
		Properties props = new Properties();

		// Zap whitespace
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			//System.out.println("Tok=[" + tok + "]");

			// Tokens look like "/welcome.html=mainController"
			int eqpos = tok.indexOf("=");
			if (eqpos == -1) {
				// 我们只有属性名, 但值是空字符串的值
				props.put(tok, "");
			} else {
				String key = tok.substring(0, eqpos);
				String value = tok.substring(eqpos + 1);
				props.put(key, value);
			}
		}

		return props;
	}    // parse


	/**
	 * 注意: 下面的代码, 使用属性默认在JBoss3.0.0中工作, 但在Orion 1.6中不工作
	 */
	private Properties load(String s) {
		Properties props = new Properties();
		try {
			props.load(new ByteArrayInputStream(s.getBytes()));
			dropComments(props);
		} catch (IOException ex) {
			// Shouldn't happen
			throw new IllegalArgumentException("Failed to read String");
		}

		return props;
	}


	/**
	 * 删除注释行. 根据java.util.Properties文档, 我们不应该这样做, 但如果
	 * 我们不这样做, 如果我们在注释标记之前有空格, 我们最终会得到
	 * "#this=is a comment"这样的属性.
	 */
	private void dropComments(Properties props) {
		Iterator keys = props.keySet().iterator();
		List commentKeys = new LinkedList();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			// 注释行以我们的注释标记之一开头
			if (key.length() > 0 && COMMENT_MARKERS.indexOf(key.charAt(0)) != -1) {
				// 我们实际上无法删除它, 因为iterator会出现并发修改异常
				commentKeys.add(key);
			}
		}
		for (int i = 0; i < commentKeys.size(); i++) {
			String key = (String) commentKeys.get(i);
			//System.out.println("Removed comment " + commentKeys.get(i));
			props.remove(key);
		}
	}

}

