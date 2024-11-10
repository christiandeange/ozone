#!/usr/bin/env bash

set -euo pipefail

git checkout main

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Error: Uncommitted changes present. Please re-run with no local changes." >&2
  exit 1
fi

# Strip leading 'v' if present
NEXT_RELEASE="${1-}"
NEXT_RELEASE="${NEXT_RELEASE#v}"

./scripts/bump_version.sh "${NEXT_RELEASE}"

git commit -am "Releasing v${NEXT_RELEASE}"
git tag "v$NEXT_RELEASE" --force
