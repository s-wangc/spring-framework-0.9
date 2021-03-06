/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.transaction;

/**
 * Thrown when an attempt to commit a transaction resulted
 * in an unexpected rollback
 *
 * @author Rod Johnson
 * @version $Revision: 1.1 $
 * @since 17-Mar-2003
 */
public class UnexpectedRollbackException extends TransactionException {


	/**
	 * @param s
	 * @param ex
	 */
	public UnexpectedRollbackException(String s, Throwable ex) {
		super(s, ex);
	}

}
