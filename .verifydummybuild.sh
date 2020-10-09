#!/bin/bash
# this script verifies that the commit is really only in the range of a sequence of dummy builds.

if ! git diff --name-only $TRAVIS_COMMIT_RANGE | grep -qvE '(.md$)'
then
  echo "Only docs were updated, not running the CI."
  exit 0
else
  echo "The commit range has changes in non-markdown files"
  git diff --name-only $TRAVIS_COMMIT_RANGE | grep -vE '(.md$)'
  exit 1
fi

