/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.transaction.interceptor;

/**
 * Tag class. Its class means it has the opposite
 * behaviour to the RollbackRule superclass.
 *
 * @author Rod Johnson
 * @version $Revision: 1.1 $
 * @since 09-Apr-2003
 */
public class NoRollbackRuleAttribute extends RollbackRuleAttribute {

	/**
	 * @param exceptionName
	 */
	public NoRollbackRuleAttribute(String exceptionName) {
		super(exceptionName);
	}

}
