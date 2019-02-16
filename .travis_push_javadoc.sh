#!/bin/sh

setup_git() {
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis CI"
}

commit_website_files() {  
  git remote add origin-pages https://${GH_TOKEN}@github.com/fmohr/AILibs.git > /dev/null 2>&1  
  if [ -z "${TRAVIS_PULL_REQUEST_BRANCH}" ]; then
    BRANCHTOPUSH=${TRAVIS_BRANCH}
  else
    BRANCHTOPUSH=${TRAVIS_PULL_REQUEST_BRANCH}
  fi
  echo "Travis branch is \"${TRAVIS_BRANCH}\""
  echo "Travis pull request branch is \"${TRAVIS_PULL_REQUEST_BRANCH}\""
  echo "Checking out ${BRANCHTOPUSH} that is the source here"
  git checkout -b ${BRANCHTOPUSH}
  git add ./\*.html
  git add ./\*.css
  git add ./\*.js
  git add ./\*package-list
  git commit --message "Travis built JavaDoc. ]ci skip["
}

upload_files() {
  git push --set-upstream origin-pages
}

setup_git
commit_website_files
upload_files
