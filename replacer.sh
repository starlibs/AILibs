#!/bin/bash

for l in `git diff --name-only dae1de8 ff46832`;
do
	unix2dos.exe $l
done