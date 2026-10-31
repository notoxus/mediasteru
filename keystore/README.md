# Release Signing Keystore

This directory contains `mediasteru-release.jks` — the private signing key used to sign release builds of the Android app (`com.mediasteru.android`).

- **Key Alias**: `standalone`

## ⚠️ Security & Backup

- **Never commit passwords or secrets to Git.**
- Keep `mediasteru-release.jks` and your passwords safely stored in a secure password manager.
- Once an APK is installed on devices, Android enforces signature verification. Any subsequent update must be signed with the exact same key.

## Setup Instructions

Both `*.jks` and `keystore.properties` are excluded in `.gitignore` to protect signing secrets.

To configure local or CI release signing:
1. Copy `android/keystore.properties.example` to `android/keystore.properties`.
2. Fill in your secure credentials:

```properties
storeFile=../../keystore/mediasteru-release.jks
storePassword=<your-store-password>
keyAlias=standalone
keyPassword=<your-key-password>
```

If `keystore.properties` is omitted, release builds will remain unsigned, allowing manual signing afterwards.
