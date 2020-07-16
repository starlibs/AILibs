---
layout: main
title: Contributing Documentation
---

# Adding Documentation
Documentation is essential for open source projects in order to increase usability.
Everybody is welcome to contribute documentation to augment clarity and usability!

## Documentation Sources
AILibs is documented in two ways:
* this main documentation
* the Java API reference

Java API documentation is automatically generated for each release, so contribution to the Java API documentation is to be done by adding the comments into the respective code files.

The sources for this documentation are in the `/docs` folder of the `AILibs` repository; the visible documentation is the version of this folder in the `master` branch.
There is a sub-folder `/docs/project` in which the documentations for different projects reside.
The documentation is written entirely in Markdown and parsed via Jekyll.
For details about the technical folder structure refer to the Jekyll documentation.

## Locally Testing Documentation
To locally test how your changes to the documentation look like, you can simply run a Jekyll server locally.

### Jekyll Setup
On most systems, you will want to install Jekyll via gems:

```sh
$ gem update
$ gem install jekyll
```

Then go to the `/docs` folder of the cloned repository and run the following
```sh
$ bundle install
```

### Running the Server
Once you accomplished the setup, you can simply run Jekyll with the following command:
```sh
$ bundle exec jekyll s
```
By default, the server listens to port 4000, so you can access the documentation in your browser via `http://localhost:4000/`.
