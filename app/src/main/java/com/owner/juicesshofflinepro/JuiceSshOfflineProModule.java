package com.owner.juicesshofflinepro;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class JuiceSshOfflineProModule implements IXposedHookLoadPackage {
    private static final String TAG = "JuiceSSHOfflinePro";
    private static final String TARGET = "com.sonelli.juicessh";
    private static final long TEN_YEARS_SECONDS = 315360000L;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) {
            return;
        }
        log("hooking " + lpparam.packageName);
        hookProCheck(lpparam.classLoader);
        hookUserSignatureAndSession(lpparam.classLoader);
        hookApiUserGate(lpparam.classLoader);
    }

    private void hookProCheck(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.sonelli.oi0",
                    cl,
                    "d",
                    Object.class,
                    XC_MethodReplacement.returnConstant(Boolean.TRUE));
            log("hooked com.sonelli.oi0.d(Object) => true");
        } catch (Throwable t) {
            log("failed to hook oi0.d", t);
        }
    }

    private void hookUserSignatureAndSession(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.sonelli.juicessh.models.User",
                    cl,
                    "H",
                    XC_MethodReplacement.returnConstant(Boolean.TRUE));
            log("hooked User.H() => true");
        } catch (Throwable t) {
            log("failed to hook User.H", t);
        }

        try {
            XposedHelpers.findAndHookMethod(
                    "com.sonelli.juicessh.models.User",
                    cl,
                    "w",
                    XC_MethodReplacement.returnConstant(TEN_YEARS_SECONDS));
            log("hooked User.w() => ten years");
        } catch (Throwable t) {
            log("failed to hook User.w", t);
        }
    }

    private void hookApiUserGate(final ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.sonelli.pi0",
                    cl,
                    "m",
                    Context.class,
                    XC_MethodReplacement.returnConstant(Boolean.TRUE));
            log("hooked pi0.m(Context) => true");
        } catch (Throwable t) {
            log("failed to hook pi0.m", t);
        }

        try {
            Class<?> callbackClass = XposedHelpers.findClass("com.sonelli.pi0$p", cl);
            XposedHelpers.findAndHookMethod(
                    "com.sonelli.pi0",
                    cl,
                    "j",
                    Context.class,
                    callbackClass,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            Object callback = param.args[1];
                            if (callback == null) {
                                return null;
                            }

                            Object user = null;
                            try {
                                if (param.args[0] instanceof Context) {
                                    user = XposedHelpers.callStaticMethod(
                                            XposedHelpers.findClass("com.sonelli.juicessh.models.User", cl),
                                            "A",
                                            param.args[0]);
                                }
                            } catch (Throwable ignored) {
                            }

                            if (user == null) {
                                user = XposedHelpers.findClass("com.sonelli.juicessh.models.User", cl)
                                        .getDeclaredConstructor()
                                        .newInstance();
                            }

                            Method onUser = callback.getClass().getMethod("b", user.getClass());
                            onUser.setAccessible(true);
                            onUser.invoke(callback, user);
                            return null;
                        }
                    });
            log("hooked pi0.j(Context,p) => local callback, no network refresh");
        } catch (Throwable t) {
            log("failed to hook pi0.j", t);
        }
    }

    private static void log(String msg) {
        XposedBridge.log(TAG + ": " + msg);
        Log.i(TAG, msg);
    }

    private static void log(String msg, Throwable t) {
        XposedBridge.log(TAG + ": " + msg + "\n" + Log.getStackTraceString(t));
        Log.e(TAG, msg, t);
    }
}
