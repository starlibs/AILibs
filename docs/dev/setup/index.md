---
layout: main
title: Setup
---

# Setup
This page provides a summary of the (Java) design principles we (try to) adopt in AILibs.

## IDE

## Testing
### Environment Variables
Some components of AILibs require a database connection to be tested.
For those tests to succeed, the following environment variables *must* be defined:
* AILIBS_JAICORE_DB_DEFAULT_HOST
* AILIBS_JAICORE_DB_DEFAULT_USER
* AILIBS_JAICORE_DB_DEFAULT_PASS
* AILIBS_JAICORE_DB_DEFAULT_DATABASE
* AILIBS_JAICORE_DB_REST_DB_HOST
* AILIBS_JAICORE_DB_REST_DB_TOKEN

Needless to say, a MySQL/MariaDB server needs to run on the specified host (Rest SQL Server for the REST directive), and the given database must be accessible given the username and the password.

On a typical Linux system, this specification can be done in the `.bashrc` or another shell initialization file.
```bash
export AILIBS_JAICORE_DB_DEFAULT_HOST=localhost
export AILIBS_JAICORE_DB_DEFAULT_USER=test
export AILIBS_JAICORE_DB_DEFAULT_PASS=drowssap
export AILIBS_JAICORE_DB_DEFAULT_DATABASE=test
export AILIBS_JAICORE_DB_REST_DB_HOST=http://example.com
export AILIBS_JAICORE_DB_REST_DB_TOKEN=12345

```
For support in the IDE like *Eclipse*, you may need to add the definitions to `/etc/environment`.
