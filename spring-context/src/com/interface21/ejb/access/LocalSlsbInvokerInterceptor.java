package com.interface21.ejb.access;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBLocalObject;

import org.aopalliance.AspectException;
import org.aopalliance.MethodInvocation;

import com.interface21.aop.framework.AopUtils;
import com.interface21.ejb.access.AbstractSlsbInvokerInterceptor;

/**
 * Interceptor that invokes a local SLSB, after caching
 * the home object. A local EJB home can never go stale.
 *
 * @author Rod Johnson
 * @version $Id: LocalSlsbInvokerInterceptor.java,v 1.1 2003/06/13 13:40:37 jhoeller Exp $
 */
public class LocalSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {

	protected EJBLocalObject newSessionBeanInstance() {
		if (logger.isDebugEnabled())
			logger.debug("Trying to create EJB");

		EJBLocalObject session = (EJBLocalObject) getHomeBeanWrapper().invoke(CREATE_METHOD, null);

		if (logger.isDebugEnabled())
			logger.debug("EJB created OK [" + session + "]");
		return session;
	}

	/**
	 * This is the last invoker in the chain
	 *
	 * @see org.aopalliance.MethodInterceptor#invoke(org.aopalliance.Invocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		EJBLocalObject ejb = newSessionBeanInstance();
		try {
			return invocation.getMethod().invoke(ejb, AopUtils.getArguments(invocation));
		} catch (InvocationTargetException ex) {
			logger.warn(ex + " thrown invoking remote EJB method " + invocation.getMethod());
			throw ex.getTargetException();
		} catch (Throwable t) {
			throw new AspectException("Failed to invoke remote EJB", t);
		}
	}

}
