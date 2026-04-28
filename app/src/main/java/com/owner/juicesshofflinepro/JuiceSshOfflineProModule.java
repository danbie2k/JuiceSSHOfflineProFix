package com.owner.juicesshofflinepro;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public final class JuiceSshOfflineProModule extends XposedModule {
    private static final String TAG = "JuiceSSHOfflinePro";
    private static final String TARGET = "com.sonelli.juicessh";

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        log(Log.INFO, TAG, "module loaded; framework=" + getFrameworkName() + " api=" + getApiVersion());
    }

    @Override
    public void onPackageReady(XposedModuleInterface.PackageReadyParam param) {
        if (!TARGET.equals(param.getPackageName())) {
            return;
        }
        ClassLoader cl = param.getClassLoader();
        log(Log.INFO, TAG, "hooking " + param.getPackageName());
        hookProCheck(cl);
        hookUserSignatureAndSession(cl);
        hookApiUserGate(cl);
    }

    private void hookProCheck(ClassLoader cl) {
        try {
            Class<?> oi0 = Class.forName("com.sonelli.oi0", false, cl);
            Method proCheck = oi0.getDeclaredMethod("d", Object.class);
            proCheck.setAccessible(true);
            hook(proCheck)
                    .setPriority(XposedInterface.PRIORITY_HIGHEST)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> Boolean.TRUE);
            log(Log.INFO, TAG, "hooked com.sonelli.oi0.d(Object) => true");
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "failed to hook oi0.d", t);
        }
    }

    private void hookUserSignatureAndSession(ClassLoader cl) {
        try {
            Class<?> user = userClass(cl);

            Method signatureCheck = user.getDeclaredMethod("H");
            signatureCheck.setAccessible(true);
            hook(signatureCheck)
                    .setPriority(XposedInterface.PRIORITY_HIGHEST)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> Boolean.TRUE);
            log(Log.INFO, TAG, "hooked User.H() => true");

            Method secondsUntilExpiry = user.getDeclaredMethod("w");
            secondsUntilExpiry.setAccessible(true);
            hook(secondsUntilExpiry)
                    .setPriority(XposedInterface.PRIORITY_HIGHEST)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> 315360000L); // ten years, in seconds
            log(Log.INFO, TAG, "hooked User.w() => ten years");
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "failed to hook User methods", t);
        }
    }

    private void hookApiUserGate(ClassLoader cl) {
        try {
            Class<?> api = Class.forName("com.sonelli.pi0", false, cl);
            Class<?> callback = Class.forName("com.sonelli.pi0$p", false, cl);
            Method currentUser = userClass(cl).getDeclaredMethod("A", Context.class);
            currentUser.setAccessible(true);
            Method onUser = callback.getDeclaredMethod("b", userClass(cl));
            onUser.setAccessible(true);

            Method hasPro = api.getDeclaredMethod("m", Context.class);
            hasPro.setAccessible(true);
            hook(hasPro)
                    .setPriority(XposedInterface.PRIORITY_HIGHEST)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> Boolean.TRUE);
            log(Log.INFO, TAG, "hooked pi0.m(Context) => true");

            Method getUser = api.getDeclaredMethod("j", Context.class, callback);
            getUser.setAccessible(true);
            hook(getUser)
                    .setPriority(XposedInterface.PRIORITY_HIGHEST)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(
                            chain -> {
                                Object cb = chain.getArg(1);
                                if (cb == null) {
                                    return null;
                                }
                                Object user = null;
                                try {
                                    Object ctx = chain.getArg(0);
                                    if (ctx instanceof Context) {
                                        user = currentUser.invoke(null, ctx);
                                    }
                                } catch (Throwable ignored) {
                                }
                                if (user == null) {
                                    user = userClass(cl).getDeclaredConstructor().newInstance();
                                }
                                onUser.invoke(cb, user);
                                return null;
                            });
            log(Log.INFO, TAG, "hooked pi0.j(Context,p) => local callback, no network refresh");
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "failed to hook API user gate", t);
        }
    }

    private Class<?> userClass(ClassLoader cl) throws ClassNotFoundException {
        return Class.forName("com.sonelli.juicessh.models.User", false, cl);
    }
}
