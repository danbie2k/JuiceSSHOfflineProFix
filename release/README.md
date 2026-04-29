# Release signing key

Local release keystore:

```text
release/juicessh-offline-pro-release.jks
```

Alias:

```text
org.lyf.juicesshofflinepro.release
```

Passwords are stored in macOS Keychain, not in this repo:

```bash
security find-generic-password -a org.lyf.juicesshofflinepro -s org.lyf.juicesshofflinepro.release.storepass -w
security find-generic-password -a org.lyf.juicesshofflinepro -s org.lyf.juicesshofflinepro.release.keypass -w
```

Certificate SHA-256 fingerprint:

```text
24:B8:38:2C:28:D4:62:2E:BC:1D:ED:A6:0F:05:76:E0:60:9A:91:00:EE:0F:41:1F:1C:E1:6C:91:32:B1:3D:D0
```
