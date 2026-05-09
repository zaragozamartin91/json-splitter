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
QUALIFIER=$(echo "$VERSION" | cut -d'-' -f2)

# Split the base version into major, minor, and patch
MAJOR=$(echo "$BASE_VERSION" | cut -d'.' -f1)
MINOR=$(echo "$BASE_VERSION" | cut -d'.' -f2)
PATCH=$(echo "$BASE_VERSION" | cut -d'.' -f3)

# Increment the patch version
NEW_PATCH=$((PATCH + 1))

# Reconstruct the version string
NEW_VERSION="$MAJOR.$MINOR.$NEW_PATCH"

# Append the qualifier back if it existed
if [ -n "$QUALIFIER" ]; then
    NEW_VERSION="$NEW_VERSION-$QUALIFIER"
fi

echo "$NEW_VERSION"
