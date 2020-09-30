#!/bin/bash
echo "Conducting tests for branch $TRAVIS_BRANCH"

if [[ "$TRAVIS_BRANCH" == "master" ]]; then
	echo "We're on the master branch. Testing everything."
	echo "./gradlew $TEST_PROJECT:test"
	./gradlew $TEST_PROJECT:test
	# exit with  the gradle exit code
 	 exit $?
fi
if [[ "$TRAVIS_BRANCH" == "dev" ]]; then
	echo "We're on the dev branch. Testing everything that does not take too long."
	echo "Running ./gradlew $TEST_PROJECT:testMedium"
	./gradlew $TEST_PROJECT:testMedium
	# exit with  the gradle exit code
 	 exit $?
fi

## if we are not on one of these two branches, just do a quick test
echo "This is no protected branch, just running a quick test"
./gradlew $TEST_PROJECT:testQuick
