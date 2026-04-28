package de.robv.android.xposed;
public final class XposedHelpers {
  public static Class<?> findClass(String className, ClassLoader cl) throws ClassNotFoundException { return Class.forName(className, false, cl); }
  public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader cl, String methodName, Object... parameterTypesAndCallback) { return null; }
  public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) { return null; }
  public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) throws Exception { for (java.lang.reflect.Method m: clazz.getDeclaredMethods()) if (m.getName().equals(methodName) && m.getParameterTypes().length==args.length) { m.setAccessible(true); return m.invoke(null,args); } throw new NoSuchMethodException(methodName); }
}
