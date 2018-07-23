#!/usr/bin/python
import sys, math
from subprocess import call
M = 'true'
S = ''
R = ''
for i in range(len(sys.argv)-1): 
	 if (sys.argv[i] == '-M'):
	 	 M = (sys.argv[i+1])
	 elif(sys.argv[i] == '-S'):
	 	 S = (sys.argv[i+1])
	 elif(sys.argv[i] == '-R'):
	 	 R = (sys.argv[i+1])
call("java TestSet M S R weka.attributeSelection.AttributeSelectionweka.classifiers.meta.VoteTestSet")
file = open("Results/weka.attributeSelection.AttributeSelectionweka.classifiers.meta.VoteTestSet.txt", "r")
yValue = float(file.read())
print "Result for SMAC: SUCCESS, 0, 0, %f, 0" % yValue
