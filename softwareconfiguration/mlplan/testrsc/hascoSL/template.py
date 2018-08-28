import sys
import arffcontainer
import numpy as np
import sklearn.metrics
from sklearn.externals import joblib
#import#

mode=sys.argv[1]

if mode == "trainAndPredict":
	trainFile=sys.argv[2]
	testFile=sys.argv[3]
	objFile=sys.argv[4]
	
	trainData = arffcontainer.parse(trainFile)
	X_train = trainData.input_matrix
	y_train = []
	for crow in trainData.output_matrix:
	    for x in range(0, len(crow)):
		    if crow[x] == 1:
			    y_train.append(x)
	
	testData = arffcontainer.parse(testFile)
	X_test = testData.input_matrix
	y_test = []
	for crow in testData.output_matrix:
	    for x in range(0, len(crow)):
		    if crow[x] == 1:
			    y_test.append(x)
	    
	mlpipeline = #pipeline#
	
	mlpipeline.fit(X_train, y_train)
	joblib.dump(mlpipeline, objFile)
	
	y_hat = mlpipeline.predict(X_test)
	print(y_hat)
elif mode == "train":
	trainFile=sys.argv[2]
	objFile=sys.argv[3]
	
	trainData = arffcontainer.parse(trainFile)
	X_train = trainData.input_matrix
	y_train = []
	for crow in trainData.output_matrix:
	    for x in range(0, len(crow)):
		    if crow[x] == 1:
			    y_train.append(x)
		    
	mlpipeline = #pipeline#
	
	mlpipeline.fit(X_train, y_train)
	joblib.dump(mlpipeline, objFile)
elif mode == "predict":
	testFile=sys.argv[2]
	objFile=sys.argv[3]
	
	testData = arffcontainer.parse(testFile)
	X_test = testData.input_matrix
	y_test = []
	for crow in testData.output_matrix:
	    for x in range(0, len(crow)):
		    if crow[x] == 1:
			    y_test.append(x)
	
	mlpipeline = joblib.load(objFile)	    
	y_hat = mlpipeline.predict(X_test)
	print(y_hat)
	
	errorRate = 1 - sklearn.metrics.accuracy_score(y_test, y_hat)
	print(errorRate)