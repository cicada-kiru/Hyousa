package hyousa.common.util;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by yousa on 2017/12/11.
 */
public class ReflectUtil {
    //TODO: to implement

    public static Object newInstance(String className)
        throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        return Class.forName(className).getConstructor().newInstance();
    }

    public static Object newInstance(Class<?> clazz)
        throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        return clazz.getConstructor().newInstance();
    }
}
