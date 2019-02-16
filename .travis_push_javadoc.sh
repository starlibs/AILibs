#!/bin/sh

setup_git() {
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis CI"
}

commit_website_files() {  
  git remote add origin-pages https://${GH_TOKEN}@github.com/fmohr/AILibs.git > /dev/null 2>&1
  git checkout -b javadoc
  git add ./\*.html
  git add ./\*.css
  git add ./\*.js
  git add ./\*package-list
  git commit --message "Travis built Javadoc"
}

upload_files() {
  git push --set-upstream origin-pages javadoc 
}

setup_git
commit_website_files
upload_files
