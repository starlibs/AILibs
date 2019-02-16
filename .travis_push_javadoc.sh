#!/bin/sh

setup_git() {
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis CI"
}

commit_website_files() {  
  git remote add origin-pages https://${GH_TOKEN}@github.com/fmohr/AILibs.git > /dev/null 2>&1
  git remote update
  git checkout -b ${TRAVIS_PULL_REQUEST_BRANCH}
  git add ./\*.html
  git add ./\*.css
  git add ./\*.js
  git add ./\*package-list
  git commit --message "Travis built JavaDoc. ]ci skip["
}

upload_files() {
  echo "Pushing back to ${TRAVIS_PULL_REQUEST_BRANCH}"
  git push --set-upstream origin-pages
}

#setup_git
commit_website_files
upload_files
