import autosklearn.classification
from sklearn import model_selection
import sklearn.datasets
import sklearn.metrics
import arffcontainer
import sys
from sklearn.datasets import load_iris
import numpy as np
import resource
from sklearn.externals import joblib

if __name__ == "__main__":
    print(sys.argv)

    if len(sys.argv) != 9:
	    print('Number of arguments invalid. Please provide the following arguments: [train.arff] [test.arff] [timeout in s] [eval_timeout in s] [output.txt] [tmpDir] [outputDir] [memLimit]')
	    sys.exit(0)

    trainFile = sys.argv[1]
    testFile = sys.argv[2]
    timeout = int(sys.argv[3])
    evalTimeout = int(sys.argv[4])
    outputFile = sys.argv[5]
    tmpDir = sys.argv[6]
    outDir = sys.argv[7]
    memLimit = int(int(sys.argv[8]) * 0.25)
    hardLimit = int(sys.argv[8]) * 1024 * 1024 - 2 * 1024 * 1024 * 1024
    softLimit = int(sys.argv[8]) * 1024 * 512
    
    resource.setrlimit(resource.RLIMIT_DATA, (softLimit, hardLimit))
    
    version = "vanilla"
    initialConfigs = 25
    
    if version == "vanilla":
	    initialConfigs = 0

    print(trainFile)
    
    trainData = arffcontainer.parse(trainFile)
    X_train = np.array(trainData.input_matrix)
    y_train = []
    for crow in trainData.output_matrix:
	    for x in range(0, len(crow)):
		    if crow[x] == 1:
			    y_train.append(x)
    y_train = np.array(y_train)


    testData = arffcontainer.parse(testFile)
    X_test = np.array(testData.input_matrix)
    y_test = []
    for crow in testData.output_matrix:
	    for x in range(0, len(crow)):
		    if crow[x] == 1:
			    y_test.append(x)
    y_test = np.array(y_test)
    
    #X, Y = sklearn.datasets.make_multilabel_classification(n_samples=2000,n_features=50, n_classes=30, n_labels=4, allow_unlabeled=False)

    print("Created training data for multilabel classification")

    #X_train, X_test, Y_train, Y_test = model_selection.train_test_split(X,Y)

    print("Split the data into train test")

    automl = autosklearn.classification.AutoSklearnClassifier(time_left_for_this_task=int(timeout),
		per_run_time_limit=int(evalTimeout),
		ensemble_size=1,
		ml_memory_limit=memLimit,
		tmp_folder=tmpDir,
		output_folder=outDir,
		initial_configurations_via_metalearning=initialConfigs,
		delete_tmp_folder_after_terminate=False,
		delete_output_folder_after_terminate=False
	    )
    print("Start fitting autosklearn classifier")
    automl.fit(X_train, y_train)
    print("Fitted autosklearn classifier successfully")

    print("Assess quality of returned solution")
    y_hat = automl.predict(X_test)
    #accuracy = sklearn.metrics.label_ranking_average_precision_score(y_test, y_hat)
    accuracy = sklearn.metrics.accuracy_score(y_test, y_hat)
    
    outputFileObj = open(outputFile, "w")
    outputFileObj.write(str(accuracy))
    outputFileObj.write("\n");
    outputFileObj.close()
    
    print("Accuracy score", str(accuracy))
    
    print("Store model on disk")
    joblib.dump(automl, outDir + "model.pkl")
