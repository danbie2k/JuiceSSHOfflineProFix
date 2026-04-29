package org.lyf.juicesshofflinepro;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class JuiceSshOfflineProModule implements IXposedHookLoadPackage {
    private static final String TAG = "JuiceSSHOfflinePro";
    private static final String TARGET = "com.sonelli.juicessh";
    private static final String MODULE_PACKAGE = "org.lyf.juicesshofflinepro";
    private static final long SECONDS_PER_YEAR = 365L * 24L * 60L * 60L;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) {
            return;
        }
        Config config = Config.load();
        log("hooking " + lpparam.packageName + " with " + config);
        if (config.hookProCheck) {
            hookProCheck(lpparam.classLoader);
        }
        if (config.hookUserSignature || config.hookSessionExpiry) {
            hookUserSignatureAndSession(lpparam.classLoader, config);
        }
        if (config.hookApiGate) {
            hookApiUserGate(lpparam.classLoader);
        }
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

    private void hookUserSignatureAndSession(ClassLoader cl, Config config) {
        if (config.hookUserSignature) {
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
        }

        if (config.hookSessionExpiry) {
            try {
                XposedHelpers.findAndHookMethod(
                        "com.sonelli.juicessh.models.User",
                        cl,
                        "w",
                        XC_MethodReplacement.returnConstant(config.sessionYears * SECONDS_PER_YEAR));
                log("hooked User.w() => " + config.sessionYears + " years");
            } catch (Throwable t) {
                log("failed to hook User.w", t);
            }
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

    private static final class Config {
        final boolean hookProCheck;
        final boolean hookUserSignature;
        final boolean hookSessionExpiry;
        final boolean hookApiGate;
        final int sessionYears;

        Config(boolean hookProCheck, boolean hookUserSignature, boolean hookSessionExpiry, boolean hookApiGate, int sessionYears) {
            this.hookProCheck = hookProCheck;
            this.hookUserSignature = hookUserSignature;
            this.hookSessionExpiry = hookSessionExpiry;
            this.hookApiGate = hookApiGate;
            this.sessionYears = Math.max(1, Math.min(100, sessionYears));
        }

        static Config load() {
            try {
                XSharedPreferences prefs = new XSharedPreferences(MODULE_PACKAGE, SettingsActivity.PREFS);
                prefs.makeWorldReadable();
                prefs.reload();
                return new Config(
                        prefs.getBoolean(SettingsActivity.KEY_HOOK_PRO_CHECK, true),
                        prefs.getBoolean(SettingsActivity.KEY_HOOK_USER_SIGNATURE, true),
                        prefs.getBoolean(SettingsActivity.KEY_HOOK_SESSION_EXPIRY, true),
                        prefs.getBoolean(SettingsActivity.KEY_HOOK_API_GATE, true),
                        prefs.getInt(SettingsActivity.KEY_SESSION_YEARS, 10));
            } catch (Throwable t) {
                log("failed to read settings; using defaults", t);
                return new Config(true, true, true, true, 10);
            }
        }

        @Override
        public String toString() {
            return "Config{" +
                    "pro=" + hookProCheck +
                    ", userSig=" + hookUserSignature +
                    ", session=" + hookSessionExpiry +
                    ", apiGate=" + hookApiGate +
                    ", years=" + sessionYears +
                    '}';
        }
    }
}
