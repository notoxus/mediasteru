#!/usr/bin/env bash
# Bumps the VJ Desktop app's release version everywhere it's hardcoded.
# Usage: ./bump-version.sh [new-version]   e.g. ./bump-version.sh 1.0.7

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

# GNU sed uses `-i -E`; BSD sed (macOS) requires an explicit backup suffix:
# `-i '' -E`. Keep the version bump script usable on both platforms.
sed_in_place() {
    local expression="$1"
    local file="$2"
    if sed --version >/dev/null 2>&1; then
        sed -i -E "$expression" "$file"
    else
        sed -i '' -E "$expression" "$file"
    fi
}

if [ $# -eq 0 ]; then
    # Auto-detect latest git tag
    LATEST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "v1.0.6")
    LATEST_CLEAN="${LATEST_TAG#v}"
    
    if [[ "$LATEST_CLEAN" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
        MAJOR="${BASH_REMATCH[1]}"
        MINOR="${BASH_REMATCH[2]}"
        PATCH="${BASH_REMATCH[3]}"
        NEW="$MAJOR.$MINOR.$((PATCH + 1))"
        echo "No version specified. Auto-bumping from $LATEST_TAG -> v$NEW"
    else
        echo "Error: Could not parse latest tag '$LATEST_TAG'."
        echo "Usage: $0 <new-version>   e.g. $0 1.0.7"
        exit 1
    fi
else
    NEW="${1#v}"
fi

if ! [[ "$NEW" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: version must look like 1.0.7 or v1.0.7"
    exit 1
fi

echo "Bumping to v$NEW ..."

# --- Gradle (Spring Boot VJ Desktop) ---
if [ -f build.gradle.kts ]; then
    sed_in_place "s/^(version = \")[^\"]+(\")/\1$NEW\2/" build.gradle.kts
    echo "  Updated build.gradle.kts"
elif [ -f build.gradle ]; then
    sed_in_place "s/^(version = ')[^']+(')/\1$NEW\2/" build.gradle
    echo "  Updated build.gradle"
fi

# --- Properties ---
for prop in core/src/main/resources/version.properties desktop/src/main/resources/version.properties src/main/resources/version.properties; do
    if [ -f "$prop" ]; then
        echo "version=$NEW" > "$prop"
        echo "  Updated $prop"
    fi
done

# --- Electron ---
if [ -f electron/package.json ]; then
    sed_in_place "s/(\"version\": \")[^\"]+(\")/\1$NEW\2/" electron/package.json
    echo "  Updated electron/package.json"
fi

# --- Android ---
if [ -f android/app/build.gradle.kts ]; then
    sed_in_place "s/(versionName = \")[^\"]+(\")/\1$NEW\2/" android/app/build.gradle.kts
    echo "  Updated android/app/build.gradle.kts"
fi

# --- Docs ---
if [ -f Installation.md ]; then
    sed_in_place "s/MediaSteru-v[0-9]+\.[0-9]+\.[0-9]+/MediaSteru-v$NEW/g" Installation.md
    echo "  Updated Installation.md"
fi

if [ -f README.md ]; then
    sed_in_place "s/(<!-- VERSION_START -->)[^<]+(<!-- VERSION_END -->)/\1$NEW\2/g" README.md
    echo "  Updated README.md"
fi

echo ""
echo "Done! Review changes before committing:"
echo "  git diff"
