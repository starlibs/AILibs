def warn(*args, **kwargs):
    pass
import warnings
warnings.warn = warn

import os
os.environ['OMP_NUM_THREADS']='1'
os.environ['OPENBLAS_NUM_THREADS']='1'
os.environ['MKL_NUM_THREADS']='1'
os.environ['VECLIB_MAXIMUM_THREADS']='1'
os.environ['NUMEXPR_NUM_THREADS']='1'

import arff
import resource
import argparse
import json
import pickle
import sys

import numpy as np
from python_connection.datastructure.datastructure import parse_raw, PandasDataFrameWrapper

from sklearn.pipeline import make_pipeline
from sklearn.pipeline import make_union

{{imports}}

def run_feature_generation(pipeline, raw_train_data, raw_test_data, output_file):
    print('Starting feature engineering ...')
    train_data_frame = PandasDataFrameWrapper(raw_train_data)
    train_targets = train_data_frame.y
    pipeline.fit(X=raw_train_data, y=train_targets)

    print('Transforming training data ...')
    train_data_transformed = pipeline.transform(raw_train_data)
    serialize_feature_generation(raw_train_data['relation'], train_data_transformed, train_targets, output_file.replace('test', 'train'))

    print('Transforming testing data ...')
    test_data_frame = PandasDataFrameWrapper(raw_test_data)
    test_targets = test_data_frame.y
    test_data_transformed = pipeline.transform(raw_test_data)
    serialize_feature_generation(raw_test_data['relation'].replace('train', 'test'), test_data_transformed, test_targets, output_file)


def serialize_feature_generation(relation, instances, targets, output_file):
    """
    Serializes the generated features to a ARFF file with the given prefix.
    :param relation: The name of the dataset
    :param instances: The list of instances of fixed-size real valued features
    :param targets: The list of target values
    :param output_file: The prefix of the output file path
    :return:
    """
    print('Serializing data to: ' + output_file)
    data = dict()
    data['relation'] = relation
    data['attributes'] = list()
    for f, feature in enumerate(instances[0]):
        data['attributes'].append(['feature'+str(f), 'REAL'])
    data['attributes'].append(['RUL', 'REAL'])
    data['data'] = list()
    for i, instance in enumerate(instances):
        data['data'].append([str(f) for f in instance] + [targets[i]])

    with open(output_file, 'a') as file:
        arff.dump(data, file)


def run_train_test(raw_train_data, raw_test_data, pipeline, output_file):
    train_data_frame = PandasDataFrameWrapper(raw_train_data)
    test_data_frame = PandasDataFrameWrapper(raw_test_data)

    #train
    train_targets = train_data_frame.y
    pipeline.fit(X=raw_train_data, y=train_targets)

    #test
    test_targets = test_data_frame.y
    prediction = pipeline.predict(raw_test_data)
    serialize_prediction(prediction, output_file)
    print("Predictions:" + str(prediction))


def serialize_model(classifier_instance, output_file):
    """
    Serialize trained model.
    Returns path to serialized model.
    """
    # Safe model on disk.
    print("dump model to file ", output_file)
    with open(output_file, 'wb') as file:
        pickle.dump(classifier_instance, file)


def serialize_prediction(prediction, output_file):
    """
    Serialize prediction results.
    Returns path to serialized predictions.
    """
    # Make sure the predictions are in a list
    prediction = prediction.tolist()
    # Convert possible integers to floats (nescassary for Weka signature)
    if isinstance(prediction[0],int):
        prediction = [float(i) for i in prediction]
    elif isinstance(prediction[0],list):
        for sublist in prediction:
            sublist = [float(i) for i in sublist]
    if not isinstance(prediction[0],list):
        prediction = [prediction]
    prediction_json = json.dumps(prediction)
    # Safe prediction on disk.
    print("write prediction to file ", output_file)
    with open(output_file, 'w') as file:
        file.write(prediction_json)


def limit_memory(maxsize):
	soft, hard = resource.getrlimit(resource.RLIMIT_AS)
	resource.setrlimit(resource.RLIMIT_AS, (maxsize, hard))


def parse_args():
    """
    Parses the arguments that are given to the script and overwrites sys.argv with this parsed representation that is
    accessable as a list.
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('--mode', choices=['train','test','traintest'], help="Selecting whether a train or a test is run.")
    parser.add_argument('--arff', required=True, help="Path or ARFF to use for training/ testing.")
    parser.add_argument('--testarff', required=True, help="Path or ARFF to use for testing when running with traintest mode.")
    parser.add_argument('--output', required=True, help="In train mode set the file where the model shall be dumped; in test mode set the file where the prediction results shall be serialized to.")
    parser.add_argument('--model', help="Path to the trained model (in .pcl format) that shall be used for testing.")
    parser.add_argument('--rul', action='store_true', help="If set, converts the data for use by tsfresh.")
    parser.add_argument('--fe', action='store_true', help="If set, applies given feature generators and stores the results in a file.")
    parser.add_argument('--regression', action='store_true', help="If set, the data is assumed to be a regression problem instead of a categorical one.")
    parser.add_argument('--targets', nargs='*', type=int, help="Declare which of the columns of the ARFF to use as targets. Default is only the last column.")
    parser.add_argument('--seed', required=True, help="Sets the seed.")
    sys.argv = vars(parser.parse_args())


def main():
    print("Running configuration: ")

    path_to_train_dataset = sys.argv["arff"]
    print("* training on data in file: ", path_to_train_dataset)

    if sys.argv["testarff"]:
        path_to_test_dataset = sys.argv["testarff"]
        print("* testing on data in file: ", path_to_test_dataset)

    output_file = sys.argv["output"]
    print("* writing output to: ", output_file)

    seed = int(sys.argv["seed"])
    np.random.seed(seed)
    print("* using seed: ", seed)

    raw_traindata = parse_raw(path_to_train_dataset)
    if path_to_test_dataset:
        raw_testdata = parse_raw(path_to_test_dataset)

    pipeline = {{classifier_construct}}

    if sys.argv["fe"] and sys.argv["arff"]:
            run_feature_generation(pipeline, raw_traindata, raw_testdata, output_file)
    elif sys.argv["arff"] and sys.argv["testarff"]:
        run_train_test(raw_traindata, raw_testdata, pipeline, output_file)


if __name__ == "__main__":
    parse_args()
    print("CURRENT_PID:" + str(os.getpid()))
    if not sys.argv["regression"] and sys.argv["targets"] and len(sys.argv["targets"]) > 1:
        raise RuntimeError("Multiple targets are not supported for categorical problems.")
    limit_memory(4294967296) # 4GB RAM
    #limit_subprocesses(1) # no parallelization of sklearn
    main()

