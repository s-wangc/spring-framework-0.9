package org.aopalliance;

import java.lang.reflect.AccessibleObject;

public interface AttributeRegistry {
    Object[] getAttributes(AccessibleObject ao);

    Object[] getAttributes(Class clazz);
}
