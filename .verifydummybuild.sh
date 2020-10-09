#!/bin/bash
# this script verifies that the commit is really only in the range of a sequence of dummy builds.

echo "Testing whether this build may be declared as a comment build or whether there are non-documentation changes in the code base."
echo "Commit range is $TRAVIS_COMMIT_RANGE" 

if ! git diff --name-only $TRAVIS_COMMIT_RANGE | grep -qvE '(.md$)'
then
  echo "Only docs were updated, not running the CI."
  exit 0
else
  echo "The commit range has changes in non-markdown files, which is forbidden for dummy builds, because this could be used to circumvent the CI when true code changes were made. Please make sure to not include the --documentation-- tag into your commit message. Here are the changed non-markdown files (one per line):"
  git diff --name-only $TRAVIS_COMMIT_RANGE | grep -vE '(.md$)' | sed 's/.*/ - &/'
  exit 1
fi

