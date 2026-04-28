package de.robv.android.xposed;
import java.lang.reflect.Member;
public abstract class XC_MethodHook {
  public XC_MethodHook() {}
  public XC_MethodHook(int priority) {}
  public static class MethodHookParam { public Member method; public Object thisObject; public Object[] args; public Object getResult(){return null;} public void setResult(Object r){} public Throwable getThrowable(){return null;} public void setThrowable(Throwable t){} }
  public static class Unhook { public void unhook() {} }
  protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
  protected void afterHookedMethod(MethodHookParam param) throws Throwable {}
}
