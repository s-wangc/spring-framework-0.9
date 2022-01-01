package org.aopalliance;

public interface Invocation {
	Object getInvokedObject();

	Object invokeNext() throws Throwable;

	Invocation detach() throws Throwable;

	AttributeRegistry getAttributeRegistry();

	void setArgument(int index, Object argument);

	Invocation cloneInstance();
}
