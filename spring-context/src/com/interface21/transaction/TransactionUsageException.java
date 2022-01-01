
package com.interface21.transaction;


/**
 * Superclass for exceptions caused by inappropriate usage of
 * a Spring transaction API.
 *
 * @author Rod Johnson
 * @version $Revision: 1.1 $
 * @since 22-Mar-2003
 */
public class TransactionUsageException extends TransactionException {

	/**
	 * Constructor for TransactionInfrastructureException.
	 *
	 * @param s
	 */
	public TransactionUsageException(String s) {
		super(s);
	}

	/**
	 * Constructor for TransactionInfrastructureException.
	 *
	 * @param s
	 * @param ex
	 */
	public TransactionUsageException(String s, Throwable ex) {
		super(s, ex);
	}


}
