## 📦 Tag & Release Management

We use a custom Bash script to synchronize versions across Gradle, properties files, and the README before tagging.

```
# Auto-detect the latest tag and bump the patch version (e.g., v1.0.0 -> v1.0.1):
bash ./bump-version.sh

# Or force a specific version:
bash ./bump-version.sh 1.0.1

git add <release-related-files>
git commit -m "release: v1.0.1"
git tag v1.0.1
git push origin main --tags

```

Pushing a `vX.Y.Z` tag starts the **Build & Release** workflow. It creates
separate, platform-specific packages for Windows x64, macOS Intel and Apple
Silicon, and Linux x64 and ARM64. Each package contains only that platform's
engine tools and its trimmed JRE. The workflow uses `jdeps` to detect the
application's JDK module dependencies and `jlink` to build the runtime image;
the TLS elliptic-curve provider is included explicitly because it is loaded
dynamically. The workflow publishes the packages and checksums to GitHub; no
manual upload is needed.

### If a release workflow fails

Fix the cause and commit the fix to `main`. If the failed tag still points to
the older commit, move it to the new commit and push the updated tag to run the
workflow again:

```bash
git tag -f vX.Y.Z
git push origin main
git push --force origin vX.Y.Z
```

Replace `vX.Y.Z` with the failed release tag. Only move the tag if GitHub has
not already published a successful release for it. When the working tree has
other staged changes, commit only the workflow fix so those changes do not get
included by accident. For example:

```bash
git commit --only .github/workflows/release.yml -m "fix: invoke release script with bash"
```
