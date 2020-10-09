#!/bin/bash
echo "Conducting tests for branch $TRAVIS_BRANCH, project $TEST_PROJECT, and task $TEST_TASK"

if [[ -z "${TEST_TASK}" ]]; then
	if [[ "$TRAVIS_BRANCH" == "master" ]]; then
		echo "We're on the master branch. Testing everything."
		TEST_TASK=test
	else
		if [[ "$TRAVIS_BRANCH" == "dev" ]]; then
			echo "We're on the dev branch. Testing everything that does not take too long."
			TEST_TASK=testMedium
		else
			## if we are not on one of these two branches, just do a quick test
			echo "This is no protected branch, just running a quick test"
			TEST_TASK=testQuick
		fi
	fi
else
	echo "A test task is already defined. We use this indepedent of the branch."
	if [[ ${TEST_TASK} == "testInterruptibility" ||  ${TEST_TASK} == "testCancelability" ||  ${TEST_TASK} == "testTimeoutability" ||  ${TEST_TASK} == "testInterruptibilityMCTS" ||  ${TEST_TASK} == "testInterruptibilityDefault" ]]; then
		if [[ ${TRAVIS_BRANCH} != "master" && ${TRAVIS_BRANCH} != "dev" ]]; then
			echo "Skipping time intense tests for branch build."
			exit 0
		fi
	fi
fi

echo "Test task is: ${TEST_TASK}"
./gradlew $TEST_PROJECT:$TEST_TASK
echo "Test finished. This is the report folder:"
find ./ -maxdepth 10 -path "*/build/reports/tests/${TEST_TASK}" -exec echo {} \; -exec ls -lh {} \;
exit $?
