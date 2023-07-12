#!/usr/bin/env bash

set -euo pipefail

NEXT_SNAPSHOT_VERSION="${1-}"

if [[ -z "${1}" ]]; then
  echo "Usage: $0 <new-version>" >&2
  exit 1
elif ! [[ "${1}" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  echo "Error: '${1}' is not a valid semantic version." >&2
  echo "Usage: $0 <new-version>" >&2
  exit 1
fi

git checkout main

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Error: Uncommitted changes present. Please re-run with no local changes." >&2
  exit 1
fi

sed -i '' "s/-SNAPSHOT//g" gradle.properties

NEXT_RELEASE="$(awk -F= '/POM_VERSION/ { print $2 }' < gradle.properties)"
CURRENT_RELEASE="$(awk -F\" '/sh.christian.ozone:bluesky/ { print $4 }' < gradle/libs.versions.toml)"
sed -i '' "s/$CURRENT_RELEASE/$NEXT_RELEASE/g" gradle/libs.versions.toml
sed -i '' "s/$CURRENT_RELEASE/$NEXT_RELEASE/g" README.md

git add README.md
git add gradle.properties
git add gradle/libs.versions.toml

git commit -m "Releasing v$NEXT_RELEASE"
git tag "v$NEXT_RELEASE" --force

sed -i '' "s/$NEXT_RELEASE/$NEXT_SNAPSHOT_VERSION-SNAPSHOT/g" gradle.properties

git add gradle.properties
git commit -m "Prepare next development cycle."
