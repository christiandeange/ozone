#!/usr/bin/env bash

set -euo pipefail

function replace_in_file {
  local replacement="${1}"
  local file="${2}"
  sed -i.bak "${replacement}" "${file}"
  rm "${file}.bak"
}

# Strip leading 'v' if present
NEXT_RELEASE="${1-}"
NEXT_RELEASE="${NEXT_RELEASE#v}"

if [[ -z "${NEXT_RELEASE}" ]]; then
  echo "Usage: $0 <new-version>" >&2
  exit 1
elif ! [[ "${NEXT_RELEASE}" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  echo "Error: '${NEXT_RELEASE}' is not a valid semantic version." >&2
  echo "Usage: $0 <new-version>" >&2
  exit 1
fi

LAST_RELEASE="$(awk -F\" '/sh.christian.ozone:bluesky/ { print $4 }' < gradle/libs.versions.toml)"

properties_files="$(find . -name gradle.properties)"
for file in $properties_files; do
  replace_in_file "s/POM_VERSION=.*/POM_VERSION=$NEXT_RELEASE/g" "${file}"
done

replace_in_file "s/$LAST_RELEASE/$NEXT_RELEASE/g" gradle/libs.versions.toml
replace_in_file "s/$LAST_RELEASE/$NEXT_RELEASE/g" README.md
