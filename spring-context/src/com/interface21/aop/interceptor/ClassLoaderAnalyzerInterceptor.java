package com.interface21.aop.interceptor;

import org.aopalliance.MethodInterceptor;
import org.aopalliance.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interface21.util.ClassLoaderAnalyzer;

/**
 * Trivial classloader analyzer interceptor
 *
 * @author Rod Johnson
 * @author Dmitriy Kopylenko
 * @version $Id: ClassLoaderAnalyzerInterceptor.java,v 1.1 2003/06/13 13:40:11 jhoeller Exp $
 */
public class ClassLoaderAnalyzerInterceptor implements MethodInterceptor {

	protected final Log logger = LogFactory.getLog(getClass());

	public Object invoke(MethodInvocation pInvocation) throws Throwable {
		logger.info("Begin...");

		logger.info(ClassLoaderAnalyzer.showClassLoaderHierarchy(
				pInvocation.getInvokedObject(),
				pInvocation.getInvokedObject().getClass().getName(),
				"\n",
				"-"));
		Object rval = pInvocation.invokeNext();

		logger.info("End.");

		return rval;
	}

}
