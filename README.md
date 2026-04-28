# JuiceSSH Offline Pro Fix (libxposed API 101)

LSPosed/libxposed API 101 module for keeping a legitimate JuiceSSH Pro entitlement usable offline after the original Google/Sonelli verification path disappeared.

## Debug APK

Built APK:

```text
../dist/JuiceSSHOfflineProFix-api101-debug.apk
```

## Hooks

Scoped only to:

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
adb install -r dist/JuiceSSHOfflineProFix-api101-debug.apk
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

The project uses:

```gradle
compileOnly 'io.github.libxposed:api:101.0.1'
```

Modern Xposed metadata is packaged under `META-INF/xposed/`:

- `java_init.list`
- `scope.list`
- `module.prop`
