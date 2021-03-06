/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.aop.framework;

import java.util.List;

import org.aopalliance.AttributeRegistry;
import org.aopalliance.Interceptor;

/**
 * Interface to be implemented by classes that hold the configuration
 * of a factory of AOP proxies. This configuration includes
 * the interceptors and pointcuts, and the proxied interfaces.
 *
 * @author Rod Johnson
 * @version $Revision: 1.3 $
 * @since 13-Mar-2003
 */
public interface ProxyConfig {

	boolean getExposeInvocation();

	AttributeRegistry getAttributeRegistry();

	/**
	 * List of interceptor and pointcut
	 */
	List getMethodPointcuts();


	Class[] getProxiedInterfaces();

	/**
	 * Add to tail
	 *
	 * @param interceptor
	 */
	void addInterceptor(Interceptor interceptor);


	/**
	 * @param pos         index from 0 (head).
	 * @param interceptor
	 */
	void addInterceptor(int pos, Interceptor interceptor);

	/**
	 * Add a pointcut
	 *
	 * @param pc
	 */
	void addMethodPointcut(MethodPointcut pc);

	/**
	 * Add a pointcut
	 *
	 * @param pc
	 */
	void addMethodPointcut(int pos, MethodPointcut pc);

	/**
	 * Remove the interceptor
	 *
	 * @param interceptor
	 * @return if the interceptor was found and removed
	 */
	boolean removeInterceptor(Interceptor interceptor);

	/**
	 * Can return null if now target. Returns true if we have
	 * a target interceptor. A target interceptor must be the last
	 * interceptor. Implementations should be efficient, as this
	 * will be invoked on each invocation.
	 *
	 * @return Object
	 */
	Object getTarget();

}
