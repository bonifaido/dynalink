package org.dynalang.dynalink.support;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.dyn.NoAccessException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A wrapper around MethodHandles.Lookup; accounts for lack of findSpecial() in
 * the backport.
 * @author Attila Szegedi
 * @version $Id: $
 */
public class Lookup
{
    private final MethodHandles.Lookup lookup;
    
    /**
     * Creates a new instance, bound to an instance of 
     * {@link MethodHandles.Lookup}.
     * @param lookup the {@link MethodHandles.Lookup} it delegates to. 
     */
    public Lookup(MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }
    
    /**
     * Performs a findSpecial on the underlying lookup, except for the backport
     * where it rather uses unreflect.
     * @param declaringClass class declaring the method
     * @param name the name of the method
     * @param type the type of the method
     * @return a method handle for the method
     * @throws NoAccessException if the method does not exist or is 
     * inacessible.
     */
    public MethodHandle findSpecial(Class<?> declaringClass, String name, 
            MethodType type)
    {
        if(Backport.inUse) {
            try {
                final Method m = declaringClass.getDeclaredMethod(name, 
                        type.parameterArray());
                if(!Modifier.isPublic(declaringClass.getModifiers()) || 
                        !Modifier.isPublic(m.getModifiers()))
                {
                    m.setAccessible(true);
                }
                return lookup.unreflect(m);
            }
            catch(NoSuchMethodException e) {
                throw new NoAccessException(e);
            }
        }
        return lookup.findSpecial(declaringClass, name, type, declaringClass);
    }
}
