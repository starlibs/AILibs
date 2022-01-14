def warn(*args, **kwargs):
    pass


import warnings

warnings.warn = warn

import os

os.environ['OMP_NUM_THREADS'] = '1'
os.environ['OPENBLAS_NUM_THREADS'] = '1'
os.environ['MKL_NUM_THREADS'] = '1'
os.environ['VECLIB_MAXIMUM_THREADS'] = '1'
os.environ['NUMEXPR_NUM_THREADS'] = '1'

import arff
import argparse
import json
#import pickle
from joblib import dump, load
import sys

from scipy.io import arff as scipy_arff
import pandas
import numpy as np

from sklearn.preprocessing import OneHotEncoder
{{import}}


class ProblemType:
    CLASSIFICATION = 'classification'
    REGRESSION = 'regression'

    @staticmethod
    def get(identifier):
        if identifier == ProblemType.CLASSIFICATION:
            return ProblemType.CLASSIFICATION
        if identifier == ProblemType.REGRESSION:
            return ProblemType.REGRESSION
        else:
            raise RuntimeError("Unsupported problem type: " + identifier)

class ArgsHandler:

    @staticmethod
    def setup():
        """
        Parses the arguments that are given to the script and overwrites sys.argv with this parsed representation that is
        accessable as a list.
        """
        parser = argparse.ArgumentParser()
        parser.add_argument('--problem', choices=[ProblemType.CLASSIFICATION, ProblemType.REGRESSION], required=True, help="Determines the type of learning problem.")
        parser.add_argument('--fit', help="Path or data to use for training.")
        parser.add_argument('--predict', help="Path or data to use for testing when running with predict mode or fitAndPredict mode.")
        parser.add_argument('--predictOutput', help="In train mode set the file where the model shall be dumped; in test mode set the file where the prediction results shall be serialized to.")
        parser.add_argument('--targets', nargs='*', type=int, help="Declare which of the columns of the ARFF to use as targets. Default is only the last column.")
        sys.argv = vars(parser.parse_args())

    @staticmethod
    def get_problem_type():
        problem_type = ProblemType.get(sys.argv["problem"])
        print("* Problem type: ", problem_type)
        return problem_type

    @staticmethod
    def get_pipeline():
        pipeline = {{pipeline}}
        print("* Pipeline: ", pipeline)
        return pipeline

    @staticmethod
    def get_fit_data_file_path():
        path = sys.argv["fit"]
        print("* Fitting on data in file: ", path)
        return path

    @staticmethod
    def get_predict_data_file_path():
        path = sys.argv["predict"]
        print("* Predicting on data in file: ", path)
        return path

    @staticmethod
    def get_predict_output_file_path():
        path = sys.argv["predictOutput"]
        print("* Predict output to file: ", path)
        return path

    @staticmethod
    def get_target_indices():
        target_indices = list(map(int, sys.argv["targets"].split()))
        print("* Target Indices: ", str(target_indices))
        return target_indices

def serialize_prediction(prediction, output_file):
    """
    Serialize prediction results.
    Returns path to serialized predictions.
    """
    # Make sure the predictions are in a list
    prediction = prediction.tolist()
    prediction_json = json.dumps(prediction)
    # Safe prediction on disk.
    print("write prediction to file ", output_file)
    with open(output_file, 'w') as file:
        file.write(prediction_json)


class SingleTargetLearningModel:

    def __init__(self):
        print("Executing model with the following configuration:")
        pipeline = ArgsHandler.get_pipeline()
        ohe = OneHotEncoder(handle_unknown='ignore')

        train_data = self.read_data(ArgsHandler.get_fit_data_file_path())
        ohe.fit(train_data.input_matrix)
        train_data.input_matrix = ohe.transform(train_data.input_matrix)
        
        self.fit(pipeline, train_data)
        test_data = self.read_data(ArgsHandler.get_predict_data_file_path())
        test_data.input_matrix = ohe.transform(test_data.input_matrix)
        self.predict(pipeline, test_data)

    def read_data(self, data_path):
        if problem_type is ProblemType.CLASSIFICATION:
            return ArffData(data_path, False)
        if problem_type is ProblemType.REGRESSION:
            return ArffData(data_path, True)

    def fit(self, pipeline, train_data):
        print('\tStart training ...')
        train_data, train_targets = train_data.input_matrix, train_data.output_matrix
        if train_targets.shape[1] != 1:
            raise RuntimeError("Can currently only work with single targets.")
        if problem_type == ProblemType.CLASSIFICATION:
            train_targets = train_targets[:, 0].astype("str")
        else:
            train_targets = train_targets[:, 0]
        pipeline.fit(X=train_data.toarray(), y=train_targets)

    def predict(self, pipeline, test_data):
        print('\tStart testing ...')
        test_features, test_targets = test_data.input_matrix, test_data.output_matrix
        
        try:
            print("Predict probabilities")
            predictions = pipeline.predict_proba(test_features.toarray())
        except:
            print("Cannot predict probabilities, thus, predict labels")
            predictions = pipeline.predict(test_features.toarray())
        serialize_prediction(predictions, ArgsHandler.get_predict_output_file_path())


class ArffData:
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

    def __init__(self, data_path, assume_numeric_targets):
        """ Reads and encapsulates arff_data which is a dictionary returned by the arff.py module.
        """
        # TODO MultiTarget
        df, class_attribute = self.parse(data_path)
        self.input_df = df.drop(columns=[class_attribute])
        # if not assume_numeric_targets:
        #    print("Computing dummy-representation of data")
        #    self.input_df = pandas.get_dummies(self.input_df)
        self.input_matrix = self.input_df.values
        self.output_df = df[[class_attribute]]
        self.output_matrix = self.output_df.values
        self.class_attribute = class_attribute

    def parse(self, data_path):
        """ Opens and reads the file located at path.
        May also be called with the content string.
        arff_: either  arff file path or arff file content. treated based on how is_bath is assigned.
        is_path: bool, if true, arff_ is an arff-file path. If false, arff_ is treated as the content of an arff file
        as a string.
        Returns an ArffStructure object or None if there was an error.
        """
        ## automatically find out whether the file is a sparse file
        seen_data = False
        dense_mode = True
        with open(data_path, 'r') as file:
            for x in file:
                if x.isspace():
                    continue
                if "@data" in x:
                    seen_data = True
                    continue
                if not seen_data:
                    continue
                dense_mode = not ("{" in x and "}" in x)
                break

        with open(data_path, 'r') as file:
            try:
                if dense_mode:
                    data, meta = scipy_arff.loadarff(file)
                    df_arff = pandas.DataFrame(data)
                else:
                    arff_parsed = arff.load(file, return_type=arff.LOD, encode_nominal=True)
                    list_attributes = arff_parsed["attributes"]
                    list_data = arff_parsed["data"]
                    rows = np.zeros((len(list_data), len(list_attributes)))
                    for i, entry in enumerate(list_data):
                        for attr_index in entry:
                            rows[i][attr_index] = entry[attr_index]
                    df_arff = pandas.DataFrame(rows, columns=[a[0] for a in list_attributes])

                # list_attributes must be a list of tuples (name, type)
                # list_data must be either a dictionary (sparse) or a list of lists
                class_attribute = None
                for a in df_arff.columns:
                    if a.lower() == "class":
                        class_attribute = a
                if class_attribute is None:
                    class_attribute = df_arff.columns[-1]

                # replace nan values with 0
                df_arff = df_arff.fillna(0)
                return df_arff, class_attribute

            except Exception as e:
                import traceback
                traceback.print_tb(e.__traceback__)
                raise RuntimeError("Something went wrong")  # return None to signify an error. (Raising Exception might be a better solution)
            finally:
                file.close()

if __name__ == "__main__":
    print("CURRENT_PID:" + str(os.getpid()))

    ArgsHandler.setup()
    problem_type = ArgsHandler.get_problem_type()
    SingleTargetLearningModel()
