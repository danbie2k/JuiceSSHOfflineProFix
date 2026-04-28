# JuiceSSH Offline Pro Fix (Xposed API 82 branch)

Legacy Xposed/LSPosed API 82-compatible module for keeping a legitimate JuiceSSH Pro entitlement usable offline after the original Google/Sonelli verification path disappeared.

## Debug APK

Built APK:

```text
../dist/JuiceSSHOfflineProFix-xposed82-debug.apk
```

## Hooks

Scope in LSPosed should be only:

```text
com.sonelli.juicessh
```

The module hooks:

- `com.sonelli.oi0.d(Object)` -> `true` (central Pro check)
- `com.sonelli.juicessh.models.User.H()` -> `true` (local user signature gate)
- `User.w()` -> ten years of remaining session time (prevents dead API refresh)
- `com.sonelli.pi0.m(Context)` -> `true`
- `com.sonelli.pi0.j(Context, pi0.p)` -> calls the callback with the local user instead of forcing the dead network refresh path

## Install

```bash
adb install -r ../dist/JuiceSSHOfflineProFix-xposed82-debug.apk
```

Then in LSPosed:

1. Enable `JuiceSSH Offline Pro Fix`.
2. Scope it to `JuiceSSH` / `com.sonelli.juicessh`.
3. Force-stop and reopen JuiceSSH, or reboot.

## Build

```bash
cd lsposed-juicessh-offline-pro
gradle assembleDebug --no-daemon
```

This branch uses legacy metadata:

- `assets/xposed_init`
- AndroidManifest `xposedmodule`, `xposeddescription`, `xposedminversion=82`

The dependency is a local compile-only stub jar:

```gradle
compileOnly files('libs/xposed-api-82-stub.jar')
```

The stub is for compilation only and is not packaged into the APK; the actual Xposed classes are provided by LSPosed/Xposed at runtime.
