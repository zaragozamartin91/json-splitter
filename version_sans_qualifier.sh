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

echo "$BASE_VERSION"
