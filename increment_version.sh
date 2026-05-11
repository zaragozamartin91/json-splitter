#!/bin/bash

# Check if a version argument was provided
if [ -z "$1" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 3.5.0-SNAPSHOT"
    exit 1
fi

VERSION=$1

# Extract the base version (remove -SNAPSHOT or other qualifiers)
BASE_VERSION=$(echo "$VERSION" | cut -d'-' -f1)

# Extract the qualifier (e.g., SNAPSHOT)
if [[ "$VERSION" == *"-"* ]]; then
    QUALIFIER=$(echo "$VERSION" | cut -d'-' -f2)
else
    QUALIFIER=""
fi

# Split the base version into major, minor, and patch
MAJOR=$(echo "$BASE_VERSION" | cut -d'.' -f1)
MINOR=$(echo "$BASE_VERSION" | cut -d'.' -f2)
PATCH=$(echo "$BASE_VERSION" | cut -d'.' -f3)

# Use major-minor-patch as default if VERSION_SCHEME is not set
SCHEME=${VERSION_SCHEME:-major-minor-patch}

case "$SCHEME" in
    major)
        NEW_MAJOR=$((MAJOR + 1))
        NEW_MINOR=0
        NEW_PATCH=0
        ;;
    major-minor)
        NEW_MAJOR=$MAJOR
        NEW_MINOR=$((MINOR + 1))
        NEW_PATCH=0
        ;;
    major-minor-patch)
        NEW_MAJOR=$MAJOR
        NEW_MINOR=$MINOR
        NEW_PATCH=$((PATCH + 1))
        ;;
    *)
        echo "Error: Invalid VERSION_SCHEME '$SCHEME'. Valid values are: major, major-minor, major-minor-patch" >&2
        exit 1
        ;;
esac

# Reconstruct the version string
NEW_VERSION="$NEW_MAJOR.$NEW_MINOR.$NEW_PATCH"

# Append the qualifier back if it existed
if [ -n "$QUALIFIER" ]; then
    NEW_VERSION="$NEW_VERSION-$QUALIFIER"
fi

echo "$NEW_VERSION"
