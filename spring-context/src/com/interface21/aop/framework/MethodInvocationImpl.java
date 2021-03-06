/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package com.interface21.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.AspectException;
import org.aopalliance.AttributeRegistry;
import org.aopalliance.Interceptor;
import org.aopalliance.Invocation;
import org.aopalliance.MethodInterceptor;
import org.aopalliance.MethodInvocation;


/**
 * Spring implementation of AOP Alliance MethodInvocation interface
 *
 * @author Rod Johnson
 * @version $Id: MethodInvocationImpl.java,v 1.4 2003/06/18 19:10:39 johnsonr Exp $
 */
public class MethodInvocationImpl implements MethodInvocation {

	/**
	 * Interface this invocation is against.
	 * May not be the same as the method's declaring interface.
	 */
	private final Class targetInterface;

	private final Method method;

	private final Object[] arguments;

	/**
	 * Not final as it can be set during invocations
	 */
	private Object target;

	private final Object proxy;

	/**
	 * Interceptors invoked in this list
	 */
	public final List interceptors;

	/**
	 * Any resources attached to this invocation.
	 * Lazily initialized for efficiency.
	 */
	private HashMap resources;

	private final AttributeRegistry attributeRegistry;

	/**
	 * Index from 0 of the current interceptor we're invoking.
	 * -1 until we invoke: then the current interceptor
	 */
	private int currentInterceptor = -1;


	/**
	 * Method Invocation.
	 *
	 * @param proxy
	 * @param target    may be null
	 * @param m
	 * @param arguments
	 * @param pointcuts list of MethodPointCut
	 * @param attList   TODO take interceptor chain as well?
	 */
	public MethodInvocationImpl(Object proxy, Object target,
								Class targetInterface, Method m, Object[] arguments,
								List pointcuts,
								AttributeRegistry attributeRegistry) {
		if (pointcuts == null || pointcuts.size() == 0)
			throw new AopConfigException("Must provide pointcuts");

		this.proxy = proxy;
		this.targetInterface = targetInterface;
		this.target = target;
		this.method = m;
		this.arguments = arguments;

		// TODO make more efficient. Could just hold indices in an int array
		this.interceptors = new LinkedList();
		for (Iterator iter = pointcuts.iterator(); iter.hasNext(); ) {
			MethodPointcut pc = (MethodPointcut) iter.next();
			if (pc.applies(m, arguments, attributeRegistry)) {
				this.interceptors.add(pc.getInterceptor());
			}
		}

		this.attributeRegistry = attributeRegistry;
	}


	/**
	 * Return the method invoked on the proxied interface.
	 * May or may not correspond with a method invoked on an underlying
	 * implementation of that interface.
	 *
	 * @return Method
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * Return the proxy that this interception was made through
	 *
	 * @return Object
	 */
	public Object getProxy() {
		return this.proxy;
	}


	public String toString() {
		// Don't do toString on target, it may be
		// proxied

		// ToString on args may also fail
		String s = "Invocation: method=[" + method + "] " +
				//"args=[" + StringUtils.arrayToDelimitedString(arguments, ",") +
				"args=" + this.arguments +
				"] ";

		s += (this.target == null) ? "target is null" :
				"target is of class " + target.getClass().getName();
		return s;

	}


	public Object setResource(String key, Object resource) {
		// Invocations are single-threaded, so we can lazily
		// instantiate the resource map if we have to
		if (this.resources == null) {
			this.resources = new HashMap();
		}
		Object oldValue = this.resources.get(key);
		this.resources.put(key, resource);
		return oldValue;
	}

	/**
	 * @return the resource or null
	 */
	public Object getResource(String key) {
		// Resource map may be null if it hasn't been instantiated
		return (this.resources == null) ? null : this.resources.get(key);
	}

	/**
	 * Private optimization method
	 *
	 * @return Object[]
	 */
	public Object[] getArguments() {
		return this.arguments;
	}

	/**
	 * @see org.aopalliance.MethodInvocation#getArgument(int)
	 */
	public Object getArgument(int i) {
		return this.arguments[i];
	}

	/**
	 * @see org.aopalliance.MethodInvocation#getArgumentCount()
	 */
	public int getArgumentCount() {
		return (this.arguments != null) ? this.arguments.length : 0;
	}

	/**
	 * @see org.aopalliance.MethodInvocation#getCurrentInterceptorIndex()
	 */
	public int getCurrentInterceptorIndex() {
		return this.currentInterceptor;
	}

	/**
	 * @see org.aopalliance.MethodInvocation#getInterceptor(int)
	 */
	public Interceptor getInterceptor(int index) {
		if (index > getInterceptorCount() - 1)
			throw new AspectException("Index " + index + " out of bounds: only " + getInterceptorCount() + " interceptors");
		return (Interceptor) this.interceptors.get(index);
	}


	/**
	 * @see org.aopalliance.MethodInvocation#getNumberOfInterceptors()
	 */
	public int getInterceptorCount() {
		return this.interceptors.size();
	}

	/**
	 * @see org.aopalliance.MethodInvocation#getTargetInterface()
	 */
	public Class getTargetInterface() {
		return this.targetInterface;
	}

	/**
	 * @see org.aopalliance.Invocation#invokeNext()
	 */
	public Object invokeNext() throws Throwable {
		if (this.currentInterceptor >= this.interceptors.size() - 1)
			throw new AspectException("All interceptors have already been invoked");

		// We begin with -1 and increment early

		// TODO think about removing cast
		MethodInterceptor interceptor = (MethodInterceptor) this.interceptors.get(++this.currentInterceptor);
		return interceptor.invoke(this);
	}

	/**
	 * @see org.aopalliance.Invocation#detach()
	 */
	public Invocation detach() {
		return this;
	}

	/**
	 * @see org.aopalliance.Invocation#getAttributeRegistry()
	 */
	public AttributeRegistry getAttributeRegistry() {
		return this.attributeRegistry;
	}


	/**
	 * @param object
	 */
	public void setTarget(Object object) {
		this.target = object;
	}


	/**
	 * @see org.aopalliance.MethodInvocation#setArgument(int, java.lang.Object)
	 */
	public void setArgument(int index, Object argument) {
		throw new UnsupportedOperationException("setArgument");
	}


	/**
	 * @see org.aopalliance.Invocation#getInvokedObject()
	 */
	public Object getInvokedObject() {
		return this.target;
	}


	/**
	 * @see org.aopalliance.Invocation#cloneInstance()
	 */
	public Invocation cloneInstance() {
		return this;
	}

}    // class MethodInvocationImpl
