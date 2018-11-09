import sys
from scipy.io import arff
import numpy as np
import ast
import pickle
import os
import json
from os.path import isfile, abspath
from os.path import join as path_join
from os import listdir
from os import remove
from time import sleep
import logging
from sklearn.preprocessing import Imputer
from {{ classifier_path }} import {{ classifier_name }} as classifier
{{ imports }}

SERIALIZATION_FOLDER = "model_dumps"

def get_filename(path_of_file):
    return os.path.splitext(os.path.basename(path_of_file))[0]

def sanatize_args():
    """
    Sanatize input arguments.
    Checks whether input includes mode and arff fileself.
    Returns values if they exist, raises error otherwise.
    """
    legit_calls = ["train","test"]
    assert len(sys.argv) > 1 , "No arguments were given."
    assert sys.argv[1]  in legit_calls , "First argument must be one of: {} . It is: {}".format(str(legit_calls),sys.argv[1])
    assert len(sys.argv) >= 3 , "Second argument must be path to arff file"
    return sys.argv[1],sys.argv[2]

def load_arff_file(arff_path):
    """
    Loads an arff file from disk.
    Returns content.
    """
    #np.set_printoptions(threshold=np.nan)
	#Load the arff dataset and convert the data into regular array.
    data,meta = arff.loadarff(arff_path)
    data = np.asarray(data.tolist(), dtype=np.float64)
    if len(data) <= 1 :
        raise ValueError("Not enough data points in : " + arff_path)
        return
    return data


def get_target_feature_matrices(data,map_that_might_contains_target_indices):
    """
    If the given map contains the key 'target_indices', then these indices are used
    to define the matrices and the entry is removed from the map. Otherwise the last column is assumed to be the target.
    Returns a target and feature matrix.
    """
    targets = []
    features = []
    # If target indices are given, use them. Else assume last feature is target.
    if "target_indices" in map_that_might_contains_target_indices:
        target_indices = map_that_might_contains_target_indices["target_indices"]
        #Each iteration yields a target/feature column and glues them together
        for i in target_indices:
            targets = zip(targets,[row[i] for row in data])
        for i in (len(data[0]) - target_indices):
            features = zip(features,[row[i] for row in data])
        map_that_might_contains_target_indices.pop("target_indices")
    else:
        targets = [row[-1] for row in data]
        features = [row[:-1] for row in data]
    return targets,features

def serialize_model(classifier_instance):
    """
    Serialize trained model.
    Returns path to serialized model.
    """
    # Get name for serialization.
    classifier_name = classifier_instance.__class__.__name__
    arff_name = get_filename(sys.argv[2])
    pcl_file = arff_name + "_" + classifier_name + ".pcl"
    dump_dest = path_join(SERIALIZATION_FOLDER,pcl_file)
    # Create serialization dir if not existent.
    if not os.path.exists(SERIALIZATION_FOLDER):
        os.makedirs(SERIALIZATION_FOLDER)
    # Safe model on disk.
    with open(dump_dest, 'wb') as file:
        pickle.dump(classifier_instance, file)
        return file.name

def serialize_prediction(prediction):
    """
    Serialize prediction results.
    Returns path to serialized predictions.
    """
    prediction = prediction.tolist()
    prediction_json = json.dumps(prediction)
    # Get name for serialization.
    test_arff_path = sys.argv[2]
    model_path = sys.argv[3]
    json_file = get_filename(test_arff_path) + "_" + get_filename(model_path) + ".json"
    dump_destination = path_join(SERIALIZATION_FOLDER,json_file)
    # Create serialization dir if not existent.
    if not os.path.exists(SERIALIZATION_FOLDER):
        os.makedirs(SERIALIZATION_FOLDER)
    # Safe prediction on disk.
    with open(dump_destination, 'w') as file:
        file.write(prediction_json)
    print("Test ARFF: " + os.path.basename(test_arff_path))
    print("Trained model: " + os.path.basename(model_path))
    return file.name



def run_train_mode(data):
    """
    Trains a moodel of the demanded classifier with the given data. The classifier is instantiated with the constructor
    parameters that the were used for the template and the classifiers can run it training with either parameters that the
    script was started with or those that were given to the template.
    Returns path to serialized model.
    """
    #Parse additional parameters.
    kwargs = ast.literal_eval(sys.argv[3:]) if len(sys.argv) > 3 else {}
    #Check if feature_indices flag parameter is given
    targets,features = get_target_feature_matrices(data,kwargs)
    #Create instance of classifier with given parameters.
    if kwargs:
        classifier_instance = classifier(**kwargs)
    else:
        classifier_instance = classifier({{ constructor_parameters }})
    classifier_instance.fit(features,targets)
    return serialize_model(classifier_instance)

def run_test_mode(data):
    """
    Tests the model that is referenced by the third argument with the given data. The test is either run with
    parameters that the script was started with or those that were given to the template.
    Returns path to prediction results.
    """
    #Path of model to be deserialized for testing.
    assert len(sys.argv) == 4, "For testing, the third parameter must name the model"
    model_path = sys.argv[3]
    # Load from disk.
    with open(model_path, 'rb') as file:
        classifier_instance = pickle.load(file)
    data = data[:,:-1]
    prediction = classifier_instance.predict(data)
    return serialize_prediction(prediction)

def main():
    logging.basicConfig(level=logging.ERROR)
    logger = logging.getLogger(__name__)
    logfile_name = str(__file__)[:-2]+'log'
    if os.path.isfile(logfile_name):
        os.remove(logfile_name)
    fh = logging.FileHandler(logfile_name)
    fh.setLevel(logging.DEBUG)
    logger.addHandler(fh)
    try:
        mode,arff_path = sanatize_args()
        data = load_arff_file(arff_path)
        if mode == "train":
            model_path = run_train_mode(data)
            print("dump: %s" % model_path)
        elif mode == "test":
            prediction_path = run_test_mode(data)
            print("test_results: %s" % prediction_path)
    except Exception as e:
        logger.exception(e)

if __name__ == "__main__":
    main()
    sys.stdout.flush()
    sys.stderr.flush()
    sleep(1)
