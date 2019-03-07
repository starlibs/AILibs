import arff
import argparse
import json
import numpy as np
import os
import pickle
import sys
import warnings
from scipy.io import arff as scipy_arff
from os.path import join as path_join
{{imports}}

OUTPUT_FILE = None

"""
ArffStructure and parse implemented by Amin Faez.
"""


def parse(arff_, is_path=True, dense_mode=True):
    """ Opens and reads the file located at path.
    May also be called with the content string.
    arff_: either  arff file path or arff file content. treated based on how is_bath is assigned.
    is_path: bool, if true, arff_ is an arff-file path. If false, arff_ is treated as the content of an arff file
    as a string.
    Returns an ArffStructure object or None if there was an error.
    """
    if is_path:
        arff_data = open(arff_, 'r')
    else:
        arff_data = arff_  # here path is actually the content of an arff file.
    try:
        try:
            if dense_mode:
                mode = arff.DENSE
            else:
                mode = arff.LOD
            arff_parsed = arff.load(arff_data, return_type=mode, encode_nominal=True)
        except:  # exception is thrown when sparse data is loaded in DENSE mode.
            if dense_mode:
                arff_parsed = parse(arff_, is_path, False)  # arff may be in sparse format.

        obj = ArffStructure(arff_parsed)
        return obj
    except Exception as e:
        import traceback
        traceback.print_tb(e.__traceback__)
        return None  # return None to signify an error. (Raising Exception might be a better solution)
    finally:
        if is_path:  # close file if necessary
            arff_data.close()


class ArffStructure:
    """ Stores the arff data in a way it can be used by tensorflow.
    Args:
        arff_data: arff_data which is a dictionary returned by the arff.py module
    Instance Attributes:
        class_list: list of all the classes in this arff data
        in_size: size of the input layer of the neural network
        out_size: size of the output layer of the neural network
        entry_size: number of training entries. Its the height of the input matrix.
        input_matrix: a list of a list.  input_matrix[i][j] stores the input values of attribute j of entry i
        from the arff file.
        output_matrix: a list of a list.  output_vector[i] stores the training output of entry i
        from the arff file in a one-hot manner.
    """

    def __init__(self, arff_data):
        """ Reads and encapsulates arff_data which is a dictionary returned by the arff.py module.
        """
        # attribute list containing a tuples (x,y).
        #  x = attribute name. y = attribute type.
        attributes_list = arff_data['attributes']
        # list of data entries. data entries are lists.
        data_list = arff_data['data']

        # looking for the class attribute in the list and extracting it.
        class_index = 0  # save at which index the class attribute was found

        for tupel in attributes_list:
            if tupel[0] == 'class':
                classtupel = tupel
                self.class_list = classtupel[1]
                attributes_list.remove(tupel)  # Now attribute list only consists of input attributes
                break
            class_index += 1

        if not hasattr(self, 'class_list'):
            # class list couldn't be found. use last tuple in attributes_list instead
            self.class_list = attributes_list.pop()[1]
            class_index = len(attributes_list)

        self.in_size = len(attributes_list)
        if type(self.class_list) == str:
            # class is only a single type
            self.out_size = 1
        else:
            self.out_size = len(self.class_list)

        # Now, extract data from data_list which was taken from arff
        self.input_matrix = []
        self.output_matrix = []

        # true, if arff was in a sparse format. (Im new to python and don't know how to use duck typing here.)
        sparse_format = type(data_list[0]) == dict

        # stores the accumulated values for every entry in attribute.
        # We need this to fill missing data with the median of attribute values. *See end of method*
        acc_list = [0] * (self.in_size + 1)
        missing_list = []  # list of tuple. stores the position of every missing entry.
        entry_index = 0
        for entry in data_list:
            self.input_matrix.append([])  # append a new empty row
            # every entry contains a value for each attribute. Extract it and add it to the value_list
            # for every attribute extract value
            for attr_index in range(self.in_size + 1):
                if sparse_format:
                    # If sparse_format, entry is a dict (x,y). x is index of attribute. y is value.
                    if attr_index in entry:  # value wasn't omitted.
                        value = (entry[attr_index])  # may return None if "?" value was used
                    else:  # was omitted in the arff data. So the value is 0.
                        value = 0
                else:
                    # Not sparse_format so entry is a list of every value.
                    value = entry[attr_index]  # may return None if "?" value was used
                # value has been extracted. Now decide where to store it
                if attr_index == class_index:  # value is class number. add to output
                    # one hot the output to be used with soft max in Tensorflow
                    self.output_matrix.append([])
                    for class_index_ in range(self.out_size):
                        self.output_matrix[-1].append(1 if class_index_ == value else 0)
                else:  # add to input
                    self.input_matrix[-1].append(value)  # add value to the last row
                    if value is not None:  # value is available
                        acc_list[attr_index] += value
                    else:  # value is missing. add entry to missing_list
                        missing_list.append((entry_index, attr_index))

                    acc_list[attr_index] += value if value else 0  # accumulate value for this attribute

            entry_index += 1
        # store data set length
        self.entry_size = len(self.input_matrix)

        # substitute missing values with the average of the attribute
        for tupel in missing_list:
            self.input_matrix[tupel[0]][tupel[1]] = acc_list[tupel[1]] / len(data_list)  # mean of this attribute


def get_filename(path_of_file):
    """
    Returns the filename of a file that is referenced with its path.
    :param path_of_file: (Absolute) path to the file.
    :return: Name of the file (without file type extension).
    """
    return os.path.splitext(os.path.basename(path_of_file))[0]

def parse_args():
    """
    Parses the arguments that are given to the script and overwrites sys.argv with this parsed representation that is
    accessable as a list.
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('--mode', choices=['train','test'], required=True, help="Selecting whether a train or a test is run.")
    parser.add_argument('--arff', required=True, help="Path or ARFF to use for training/ testing.")
    parser.add_argument('--output', required=True, help="In train mode set the file where the model shall be dumped; in test mode set the file where the prediction results shall be serialized to.")
    parser.add_argument('--model', help="Path to the trained model (in .pcl format) that shall be used for testing.")
    parser.add_argument('--regression', action='store_true', help="If set, the data is assumed to be a regression problem instead of a categorical one.")
    parser.add_argument('--targets', nargs='*', type=int, help="Declare which of the columns of the ARFF to use as targets. Default is only the last column.")
    sys.argv = vars(parser.parse_args())

def load_arff_file(arff_path):
    """
    Loads an arff file from disk.
    Returns content.
    """
    # Load the arff dataset and convert the data into array.
    if sys.argv["regression"]:
        data, meta = scipy_arff.loadarff(arff_path)
        data = np.asarray(data.tolist(), dtype=np.float64)
        if len(data) <= 1:
            raise ValueError("Not enough data points in : " + arff_path)
    else:
        data = parse(arff_path)
    return data


def get_feature_target_matrices(data):
    """
    If the given map contains the key 'target_indices', then these indices are used
    to define the matrices and the entry is removed from the map. Otherwise the last column is assumed to be the target.
    Returns a target and feature matrix.
    """
    # If target indices are given, use them...
    if sys.argv["targets"]:
        target_indices = sys.argv["targets"]
        target_columns = []
        # Stitch all the target columns together
        for index in target_indices:
            target_columns.append(data[:, index])
        targets = np.transpose(np.array(target_columns))
        # The feature columns are the remaining columns that are not a target
        feature_indices = list({j for j in range(len(data[0]))} - set(target_indices))
        feature_columns = []
        # Stitch the feature columns together
        for index in feature_indices:
            feature_columns.append(data[:, index])
        features = np.transpose(np.array(feature_columns))
    # Else assume last column is target.
    else:
        targets = [row[-1] for row in data]
        features = [row[:-1] for row in data]
    return features, targets


def serialize_model(classifier_instance):
    """
    Serialize trained model.
    Returns path to serialized model.
    """
    # Safe model on disk.
    print("dump model to file ", OUTPUT_FILE)
    with open(OUTPUT_FILE, 'wb') as file:
        pickle.dump(classifier_instance, file)

def serialize_prediction(prediction):
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
    print("write prediction to file ", OUTPUT_FILE)
    with open(OUTPUT_FILE, 'w') as file:
        file.write(prediction_json)


def run_train_mode(data):
    """
    Trains a model of the demanded classifier with the given data. The classifier is instantiated with the constructor
    parameters that the were used for the template and the classifiers can run it training with either parameters that
    the script was started with or those that were given to the template.
    Returns path to serialized model.
    """
    if sys.argv["regression"]:
        features, targets = get_feature_target_matrices(data)
    else:
        features, targets = data.input_matrix, data.output_matrix
        y_train = []
        for crow in targets:
            for x in range(0, len(crow)):
                if crow[x] == 1:
                    y_train.append(x)
        targets = np.array(y_train)
        features = np.array(features)
    # Create instance of classifier with given parameters.
    classifier_instance = {{classifier_construct}}
    classifier_instance.fit(features, targets)
    serialize_model(classifier_instance)


def run_test_mode(data):
    """
    Tests the model that is referenced by the model argument with the given data.
    Returns path to prediction results.
    """
    with open(sys.argv["model"], 'rb') as file:
        classifier_instance = pickle.load(file)
    if sys.argv["regression"]:
        features, targets = get_feature_target_matrices(data)
    else:
        features = data.input_matrix
    prediction = classifier_instance.predict(features)
    serialize_prediction(prediction)


def main():
    print("load arff file from ", sys.argv["arff"])
    data = load_arff_file(sys.argv["arff"])
    print("run script in mode ", sys.argv["mode"])
    if sys.argv["mode"] == "train":
        run_train_mode(data)
    elif sys.argv["mode"] == "test":
        assert sys.argv["model"]
        run_test_mode(data)


if __name__ == "__main__":
    parse_args()
    OUTPUT_FILE = sys.argv["output"]
    if not sys.argv["regression"] and sys.argv["targets"] and len(sys.argv["targets"]) > 1:
        raise RuntimeError("Multiple targets are not supported for categorical problems.")
    main()
