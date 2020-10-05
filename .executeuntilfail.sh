#!/bin/bash
echo "Executing ./gradlew $1 until fail"
./gradlew $1
while [[ $? == 0 ]]; do
	./gradlew $1
done
