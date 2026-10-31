## 📦 Tag & Release Management

We use a custom Bash script to synchronize versions across Gradle, properties files, and the README before tagging.

```
# Auto-detect the latest tag and bump the patch version (e.g., v1.0.0 -> v1.0.1):
./bump-version.sh

# Or force a specific version:
./bump-version.sh 1.0.1

git add <files>
git commit -m "release: v1.0.1"
git tag v1.0.1
git push origin main --tags

```
