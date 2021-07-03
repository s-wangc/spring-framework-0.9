package org.aopalliance;

import java.lang.reflect.Method;

public interface MethodInvocation extends Invocation {
    Method getMethod();

    Object setResource(String key, Object resource);

    int getArgumentCount();

    int getCurrentInterceptorIndex();

    Interceptor getInterceptor(int index);

    int getInterceptorCount();

    Class getTargetInterface();

    Object getArgument(int i);

}
