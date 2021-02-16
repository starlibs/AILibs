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
import resource
import argparse
import json
#import pickle
from joblib import dump, load
import sys

from scipy.io import arff as scipy_arff

import pandas

from joblib import dump, load
import numpy as np
from python_connection.datastructure.datastructure import parse_raw, PandasDataFrameWrapper

from sklearn.pipeline import make_pipeline, make_union

{{imports}}


class ProblemType:
    CLASSIFICATION = 'classification'
    REGRESSION = 'regression'
    RUL = 'rul'
    FEATURE_ENGINEERING = 'fe'

    @staticmethod
    def get(identifier):
        if identifier == ProblemType.CLASSIFICATION:
            return ProblemType.CLASSIFICATION
        if identifier == ProblemType.REGRESSION:
            return ProblemType.REGRESSION
        elif identifier == ProblemType.RUL:
            return ProblemType.RUL
        elif identifier == ProblemType.FEATURE_ENGINEERING:
            return ProblemType.FEATURE_ENGINEERING
        else:
            raise RuntimeError("Unsupported problem type: " + identifier)


class ModeType:
    FIT = 'fit'
    PREDICT = 'predict'
    FIT_AND_PREDICT = 'fitAndPredict'

    @staticmethod
    def get(identifier):
        if identifier == ModeType.FIT:
            return ModeType.FIT
        elif identifier == ModeType.PREDICT:
            return ModeType.PREDICT
        elif identifier == ModeType.FIT_AND_PREDICT:
            return ModeType.FIT_AND_PREDICT
        else:
            raise RuntimeError("Unsupported mode: " + identifier)


class ArgsHandler:

    @staticmethod
    def setup():
        """
        Parses the arguments that are given to the script and overwrites sys.argv with this parsed representation that is
        accessable as a list.
        """
        parser = argparse.ArgumentParser()
        parser.add_argument('--problem', choices=['classification', 'regression', 'rul', 'fe'], required=True, help="If set, converts the data for use by tsfresh.")
        parser.add_argument('--mode', choices=['fit', 'predict', 'fitAndPredict'], required=True, help="Selecting whether a train or a test is run.")
        parser.add_argument('--fit', help="Path or data to use for training.")
        parser.add_argument('--fitOutput', help="In train mode set the file where the model shall be dumped; in test mode set the file where the prediction results shall be serialized to.")
        parser.add_argument('--predict', help="Path or data to use for testing when running with predict mode or fitAndPredict mode.")
        parser.add_argument('--predictOutput', help="In train mode set the file where the model shall be dumped; in test mode set the file where the prediction results shall be serialized to.")
        parser.add_argument('--model', help="Path to the trained model (in .pcl format) that shall be used for testing.")
        parser.add_argument('--targets', nargs='*', type=int, help="Declare which of the columns of the ARFF to use as targets. Default is only the last column.")
        parser.add_argument('--seed', required=True, help="Sets the seed.")
        sys.argv = vars(parser.parse_args())

    @staticmethod
    def get_problem_type():
        problem_type = ProblemType.get(sys.argv["problem"])
        print("* Problem type: ", problem_type)
        return problem_type

    @staticmethod
    def get_mode():
        mode = ModeType.get(sys.argv["mode"])
        print("* Mode: ", mode)
        return mode

    @staticmethod
    def get_model_file_path():
        path = sys.argv["model"]
        print("* Serialize/Reuse trained model: ", path)
        return path

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
    def get_fit_output_file_path():
        path = sys.argv["fitOutput"]
        print("* Fit output to file: ", path)
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
    def get_seed():
        seed = sys.argv["seed"]
        print("* Seed: ", seed)
        return int(seed)


def serialize_model(pipeline, model_file):
    """
    Serialize trained model.
    Returns path to serialized model.
    """
    # Safe model on disk.
    print("\tSerialize model to file ", model_file)
    dump(pipeline, model_file)


def deserialize_model(model_file):
    """
    Serialize trained model.
    Returns path to serialized model.
    """
    # Safe model on disk.
    print("\tDeserialize model from file ", model_file)
    model = load(model_file)
    return model

def serialize_prediction(prediction, output_file):
    """
    Serialize prediction results.
    Returns path to serialized predictions.
    """
    # Make sure the predictions are in a list
    prediction = prediction.tolist()
    # Convert possible integers to floats (nescassary for Weka signature)
    if isinstance(prediction[0], int):
        prediction = [float(i) for i in prediction]
    elif isinstance(prediction[0], list):
        for sublist in prediction:
            sublist = [float(i) for i in sublist]
    if not isinstance(prediction[0], list):
        prediction = [prediction]
    prediction_json = json.dumps(prediction)
    # Safe prediction on disk.
    print("write prediction to file ", output_file)
    with open(output_file, 'w') as file:
        file.write(prediction_json)


class SingleTargetLearningModel:

    def __init__(self):
        print("Executing model with the following configuration:")

        mode = ArgsHandler.get_mode()
        pipeline = ArgsHandler.get_pipeline()
        seed = ArgsHandler.get_seed()
        np.random.seed(seed)

        if mode is ModeType.FIT or mode is ModeType.FIT_AND_PREDICT:
            train_data = self.read_data(ArgsHandler.get_fit_data_file_path())
            self.fit(pipeline, train_data)

        if mode is ModeType.FIT:
            model_file = ArgsHandler.get_model_file_path()
            if model_file is not None:
                serialize_model(pipeline, model_file)

        if mode is ModeType.PREDICT:
            model_file = ArgsHandler.get_model_file_path()
            pipeline = deserialize_model(model_file)

        if mode is ModeType.PREDICT or mode is ModeType.FIT_AND_PREDICT:
            test_data = self.read_data(ArgsHandler.get_predict_data_file_path())
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
        pipeline.fit(X=train_data, y=train_targets)

        model_file = ArgsHandler.get_model_file_path()
        if model_file is not None:
            serialize_model(pipeline, model_file)

    def predict(self, pipeline, test_data):
        print('\tStart testing ...')
        test_data, test_targets = test_data.input_matrix, test_data.output_matrix

        if problem_type is ProblemType.CLASSIFICATION:
            predictions = pipeline.predict_proba(test_data)
        if problem_type is ProblemType.REGRESSION:
            predictions = pipeline.predict(test_data)
        print("\tPredictions:" + str(predictions))
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
        df, class_attribute = self.parse(data_path)
        self.input_df = df.drop(columns=[class_attribute])
        if not assume_numeric_targets:
            print("Computing dummy-representation of data")
            self.input_df = pandas.get_dummies(self.input_df)
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
                    df_arff = pandas.DataFrame(scipy_arff.loadarff(file)[0])
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

class FeatureEngineering:

    def __init__(self):
        print("Executing feature engineering with the following configuration:")

        mode = ArgsHandler.get_mode()

        seed = ArgsHandler.get_seed()
        np.random.seed(seed)

        if mode is ModeType.FIT or mode is ModeType.FIT_AND_PREDICT:
            pipeline = ArgsHandler.get_pipeline()
            path_to_train_dataset = ArgsHandler.get_fit_data_file_path()
            raw_train_data = parse_raw(path_to_train_dataset)
            self.fit(pipeline, raw_train_data)

        if mode is ModeType.FIT:
            model_file = ArgsHandler.get_model_file_path()
            if model_file is not None:
                serialize_model(pipeline, model_file)

        if mode is ModeType.PREDICT:
            model_file = ArgsHandler.get_model_file_path()
            pipeline = deserialize_model(model_file)

        if mode is ModeType.PREDICT or mode is ModeType.FIT_AND_PREDICT:
            path_to_test_dataset = ArgsHandler.get_predict_data_file_path()
            raw_test_data = parse_raw(path_to_test_dataset)
            self.predict(pipeline, raw_test_data)

    def fit(self, pipeline, raw_train_data):
        print('\tStarting feature engineering ...')
        train_data_frame = PandasDataFrameWrapper(raw_train_data)
        train_targets = train_data_frame.y
        pipeline.fit(X=raw_train_data, y=train_targets)

        if problem_type == ProblemType.FEATURE_ENGINEERING:
            print('\tTransforming training data ...')
            relation_name = raw_train_data['relation'].replace('test', 'train')
            train_data_transformed = pipeline.transform(raw_train_data)
            self.serialize_feature_representation(relation_name, train_data_transformed, train_targets, ArgsHandler.get_fit_output_file_path())

    def predict(self, pipeline, raw_test_data):
        print('\tTransforming testing data ...')
        relation_name = raw_test_data['relation'].replace('train', 'test')
        test_data_frame = PandasDataFrameWrapper(raw_test_data)
        test_targets = test_data_frame.y

        if problem_type == ProblemType.FEATURE_ENGINEERING:
            test_data_transformed = pipeline.transform(raw_test_data)
            self.serialize_feature_representation(relation_name, test_data_transformed, test_targets, ArgsHandler.get_predict_output_file_path())

        elif problem_type == ProblemType.RUL:
            predictions = pipeline.predict(raw_test_data)
            print("\tPredictions:" + str(predictions))
            serialize_prediction(predictions, ArgsHandler.get_predict_output_file_path())


    def serialize_feature_representation(self, relation, instances, targets, output_file):
        print('\tSerializing data to: ' + output_file)
        data = dict()
        data['relation'] = relation
        data['attributes'] = list()
        for f, feature in enumerate(instances[0]):
            data['attributes'].append(['feature' + str(f), 'REAL'])
        data['attributes'].append(['RUL', 'REAL'])
        data['data'] = list()
        for i, instance in enumerate(instances):
            data['data'].append([str(f) for f in instance] + [targets[i]])

        with open(output_file, 'a') as file:
            arff.dump(data, file)


def limit_memory(maxsize):
    soft, hard = resource.getrlimit(resource.RLIMIT_AS)
    resource.setrlimit(resource.RLIMIT_AS, (maxsize, hard))


if __name__ == "__main__":
    print("CURRENT_PID:" + str(os.getpid()))
    limit_memory(4294967296)  # 4GB RAM

    ArgsHandler.setup()
    problem_type = ArgsHandler.get_problem_type()
    if problem_type == ProblemType.FEATURE_ENGINEERING or problem_type == ProblemType.RUL:
        FeatureEngineering()
    else:
        SingleTargetLearningModel()
