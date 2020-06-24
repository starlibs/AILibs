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

import resource 

import arff
import argparse
import json
import numpy as np

import pickle
import sys
import warnings
import pandas as pd

from scipy.io import arff as scipy_arff
from os.path import join as path_join

from sklearn.base import BaseEstimator, TransformerMixin
from sklearn.feature_selection import f_regression
from sklearn.pipeline import Pipeline, make_pipeline

from tsfresh.feature_extraction.settings import ComprehensiveFCParameters

{{imports}}

OUTPUT_FILE = None

class InvalidNumberReplacementTransformer(BaseEstimator, TransformerMixin):
    def fit(self, X, y):
        return self

    def transform(self, X):
        extracted_features = np.float32(X.to_numpy())
        features = np.nan_to_num(extracted_features)
        return features

class LowComputationTimeFCParameters(dict):
    def __init__(self):
        initial_map = ComprehensiveFCParameters()
        initial_map.pop("sample_entropy")
        initial_map.pop("change_quantiles")
        initial_map.pop("approximate_entropy")
        initial_map.pop("number_cwt_peaks")
        initial_map.pop("augmented_dickey_fuller")
        initial_map.pop("quantile")
        initial_map.pop("agg_linear_trend")
        initial_map.pop("max_langevin_fixed_point")
        initial_map.pop("friedrich_coefficients")
        initial_map.pop("fft_coefficient")
        initial_map.pop("large_standard_deviation")
        initial_map.pop("autocorrelation")
        initial_map.pop("cwt_coefficients")
        initial_map.pop("percentage_of_reoccurring_values_to_all_values")
        initial_map.pop("ar_coefficient")
        initial_map.pop("ratio_beyond_r_sigma")
        initial_map.pop("number_peaks")
        initial_map.pop("linear_trend_timewise") #broken
        super().__init__(initial_map)

class MidComputationTimeFCParameters(dict):
    def __init__(self):
        initial_map = ComprehensiveFCParameters()
        initial_map.pop("sample_entropy")
        initial_map.pop("change_quantiles")
        initial_map.pop("linear_trend_timewise")  # broken
        super().__init__(initial_map)

class Tsfresh(dict):
    def __init__(self, has_duplicate_max, binned_entropy, last_location_of_maximum, abs_energy, c3, value_count, mean_second_derivative_central, first_location_of_minimum, standard_deviation, length, mean_abs_change, has_duplicate_min, mean_change, sum_values, percentage_of_reoccurring_datapoints_to_all_datapoints, range_count, absolute_sum_of_changes, energy_ratio_by_chunks, last_location_of_minimum, linear_trend, variance_larger_than_standard_deviation, spkt_welch_density, cid_ce, symmetry_looking, has_duplicate, skewness, count_above_mean, longest_strike_below_mean, mean, agg_autocorrelation, ratio_value_number_to_time_series_length, fft_aggregated, first_location_of_maximum, partial_autocorrelation, sum_of_reoccurring_data_points, count_below_mean, variance, longest_strike_above_mean, median, kurtosis, minimum, time_reversal_asymmetry_statistic, number_crossing_m, sum_of_reoccurring_values, maximum, approximate_entropy, number_cwt_peaks, augmented_dickey_fuller, quantile, agg_linear_trend, max_langevin_fixed_point, friedrich_coefficients, fft_coefficient, large_standard_deviation, autocorrelation, cwt_coefficients, percentage_of_reoccurring_values_to_all_values, ar_coefficient, ratio_beyond_r_sigma, number_peaks, sample_entropy, change_quantiles):
        initial_map = ComprehensiveFCParameters()
        initial_map.pop("linear_trend_timewise")  # broken
        if not has_duplicate_max:
            initial_map.pop("has_duplicate_max")
        if not binned_entropy:
            initial_map.pop("binned_entropy")
        if not last_location_of_maximum:
            initial_map.pop("last_location_of_maximum")
        if not abs_energy:
            initial_map.pop("abs_energy")
        if not c3:
            initial_map.pop("c3")
        if not value_count:
            initial_map.pop("value_count")
        if not mean_second_derivative_central:
            initial_map.pop("mean_second_derivative_central")
        if not first_location_of_minimum:
            initial_map.pop("first_location_of_minimum")
        if not standard_deviation:
            initial_map.pop("standard_deviation")
        if not length:
            initial_map.pop("length")
        if not mean_abs_change:
            initial_map.pop("mean_abs_change")
        if not has_duplicate_min:
            initial_map.pop("has_duplicate_min")
        if not mean_change:
            initial_map.pop("mean_change")
        if not sum_values:
            initial_map.pop("sum_values")
        if not percentage_of_reoccurring_datapoints_to_all_datapoints:
            initial_map.pop("percentage_of_reoccurring_datapoints_to_all_datapoints")
        if not range_count:
            initial_map.pop("range_count")
        if not absolute_sum_of_changes:
            initial_map.pop("absolute_sum_of_changes")
        if not energy_ratio_by_chunks:
            initial_map.pop("energy_ratio_by_chunks")
        if not last_location_of_minimum:
            initial_map.pop("last_location_of_minimum")
        if not linear_trend:
            initial_map.pop("linear_trend")
        if not variance_larger_than_standard_deviation:
            initial_map.pop("variance_larger_than_standard_deviation")
        if not spkt_welch_density:
            initial_map.pop("spkt_welch_density")
        if not cid_ce:
            initial_map.pop("cid_ce")
        if not symmetry_looking:
            initial_map.pop("symmetry_looking")
        if not has_duplicate:
            initial_map.pop("has_duplicate")
        if not skewness:
            initial_map.pop("skewness")
        if not count_above_mean:
            initial_map.pop("count_above_mean")
        if not longest_strike_below_mean:
            initial_map.pop("longest_strike_below_mean")
        if not mean:
            initial_map.pop("mean")
        if not agg_autocorrelation:
            initial_map.pop("agg_autocorrelation")
        if not ratio_value_number_to_time_series_length:
            initial_map.pop("ratio_value_number_to_time_series_length")
        if not fft_aggregated:
            initial_map.pop("fft_aggregated")
        if not first_location_of_maximum:
            initial_map.pop("first_location_of_maximum")
        if not partial_autocorrelation:
            initial_map.pop("partial_autocorrelation")
        if not sum_of_reoccurring_data_points:
            initial_map.pop("sum_of_reoccurring_data_points")
        if not count_below_mean:
            initial_map.pop("count_below_mean")
        if not variance:
            initial_map.pop("variance")
        if not longest_strike_above_mean:
            initial_map.pop("longest_strike_above_mean")
        if not median:
            initial_map.pop("median")
        if not kurtosis:
            initial_map.pop("kurtosis")
        if not minimum:
            initial_map.pop("minimum")
        if not time_reversal_asymmetry_statistic:
            initial_map.pop("time_reversal_asymmetry_statistic")
        if not number_crossing_m:
            initial_map.pop("number_crossing_m")
        if not sum_of_reoccurring_values:
            initial_map.pop("sum_of_reoccurring_values")
        if not maximum:
            initial_map.pop("maximum")
        if not approximate_entropy:
            initial_map.pop("approximate_entropy")
        if not number_cwt_peaks:
            initial_map.pop("number_cwt_peaks")
        if not augmented_dickey_fuller:
            initial_map.pop("augmented_dickey_fuller")
        if not quantile:
            initial_map.pop("quantile")
        if not agg_linear_trend:
            initial_map.pop("agg_linear_trend")
        if not max_langevin_fixed_point:
            initial_map.pop("max_langevin_fixed_point")
        if not friedrich_coefficients:
            initial_map.pop("friedrich_coefficients")
        if not fft_coefficient:
            initial_map.pop("fft_coefficient")
        if not large_standard_deviation:
            initial_map.pop("large_standard_deviation")
        if not autocorrelation:
            initial_map.pop("autocorrelation")
        if not cwt_coefficients:
            initial_map.pop("cwt_coefficients")
        if not percentage_of_reoccurring_values_to_all_values:
            initial_map.pop("percentage_of_reoccurring_values_to_all_values")
        if not ar_coefficient:
            initial_map.pop("ar_coefficient")
        if not ratio_beyond_r_sigma:
            initial_map.pop("ratio_beyond_r_sigma")
        if not number_peaks:
            initial_map.pop("number_peaks")
        if not sample_entropy:
            initial_map.pop("sample_entropy")
        if not change_quantiles:
            initial_map.pop("change_quantiles")
        super().__init__(initial_map)

"""
ArffStructure and parse implemented by Amin Faez.
"""

class PandasDataFrameWrapper:
    def __init__(self, arff_data):
        self.X, self.y = create_pandas_dataframe(arff_data)

def create_pandas_dataframe(arff_parsed):
    dataset = list()
    y = list()
    for instance_id,instance in enumerate(arff_parsed['data']):
        for attribute_id, attribute_value in enumerate(instance):
            #no the target, but a sensor series
            if isinstance(attribute_value, str):
                split_attribute_value = attribute_value.split()
                for time_step_value_pair in split_attribute_value:
                    if not '#' in time_step_value_pair:
                        print("INVALID FORMAT!") #TODO raise exception
                    else:
                        split_pair = time_step_value_pair.split('#')
                        timestep = pd.to_numeric(split_pair[0]) #TODO robustness
                        value = pd.to_numeric(split_pair[1]) #TODO robustness
                        row = [instance_id, timestep, str(arff_parsed['attributes'][attribute_id][0]), value]
                        dataset.append(row)
            # target
            else:
                y.append(pd.to_numeric(attribute_value)) #TODO robustness
    return pd.DataFrame(dataset, columns=['instance_id','timestep','sensor','value']).astype({'instance_id': 'int32','timestep': 'int32', 'sensor': 'string', 'value': 'float32'}), pd.Series(y)


def parse(arff_, as_rul=False, is_path=True, dense_mode=True):
    """ Opens and reads the file located at path.
    May also be called with the content string.
    arff_: either  arff file path or arff file content. treated based on how is_bath is assigned.
    is_path: bool, if true, arff_ is an arff-file path. If false, arff_ is treated as the content of an arff file
    as a string.
    Returns an ArffStructure object or None if there was an error.
    """
    if is_path:
        arff_data = open(arff_, 'r')
        if as_rul:
            arff_data = arff_data.read().replace("timeseries", "string") #TODO ROBUSTNESS
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
                arff_parsed = parse(arff_, as_rul, is_path, False)  # arff may be in sparse format.

        if not as_rul:
            obj = ArffStructure(arff_parsed)
        else:
            obj = PandasDataFrameWrapper(arff_parsed)
        return obj
    except Exception as e:
        import traceback
        traceback.print_tb(e.__traceback__)
        return None  # return None to signify an error. (Raising Exception might be a better solution)
    finally:
        if is_path and not as_rul:  # close file if necessary
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
    parser.add_argument('--mode', choices=['train','test','traintest'], required=True, help="Selecting whether a train or a test is run.")
    parser.add_argument('--arff', required=True, help="Path or ARFF to use for training/ testing.")
    parser.add_argument('--testarff', required=False, help="Path or ARFF to use for testing when running with traintest mode.")
    parser.add_argument('--output', required=True, help="In train mode set the file where the model shall be dumped; in test mode set the file where the prediction results shall be serialized to.")
    parser.add_argument('--model', help="Path to the trained model (in .pcl format) that shall be used for testing.")
    parser.add_argument('--rul', action='store_true', help="If set, converts the data for use by tsfresh.")
    parser.add_argument('--regression', action='store_true', help="If set, the data is assumed to be a regression problem instead of a categorical one.")
    parser.add_argument('--targets', nargs='*', type=int, help="Declare which of the columns of the ARFF to use as targets. Default is only the last column.")
    parser.add_argument('--seed', required=True, help="Sets the seed.")
    sys.argv = vars(parser.parse_args())

def load_arff_file(arff_path, as_rul:bool):
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
        data = parse(arff_path, as_rul)
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
    if isinstance(data, PandasDataFrameWrapper):
        timeseries = data.X
        targets = data.y
        features = pd.DataFrame(index = targets.index)
        # augm = FeatureAugmenter(column_id='instance_id', column_sort='timestep', column_kind='sensor', column_value='value')
        # augm.set_params(timeseries_container=timeseries)
        # X = pd.DataFrame(index=data.y.index)
        # t = augm.fit(X, data.y)
        # print(t)
        # augm.set_params(timeseries_container=timeseries)
        # print(augm.transform(X))
        # print("tt")

        # extracted_features = extract_features(timeseries, column_id='instance_id', column_sort='timestep',
        #                                       column_kind='sensor', column_value='value')
        # extracted_features = np.float32(extracted_features.to_numpy())
        # features = np.nan_to_num(extracted_features)

    elif sys.argv["regression"]:
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

    # classifier_instance = RandomForestRegressor()
    # rf.fit(extracted_features, targets)
    # print("prediction:" + str(rf.predict(extracted_features)))
    # print("actual:" + str(targets))

    classifier_instance = {{classifier_construct}}
    # classifier_instance = Pipeline([('augmenter', FeatureAugmenter(column_id='instance_id', column_sort='timestep', column_kind='sensor', column_value='value')),
    #                                 ('replacer', InvalidNumberReplacemenTransformer()),
    #                                 ('regressor', RandomForestRegressor())])
    if isinstance(data, PandasDataFrameWrapper):
        classifier_instance.set_params(featureaugmenter__timeseries_container=timeseries)
    classifier_instance.fit(features, targets)
    serialize_model(classifier_instance)

def run_train_test_mode(data, testdata):
    """
    Trains a model of the demanded classifier with the given data. The classifier is instantiated with the constructor
    parameters that the were used for the template and the classifiers can run it training with either parameters that
    the script was started with or those that were given to the template.
    Returns path to serialized model.
    """
    if isinstance(data, PandasDataFrameWrapper):
        timeseries = data.X
        targets = data.y
        features = pd.DataFrame(index=targets.index)
    elif sys.argv["regression"]:
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
    if isinstance(data, PandasDataFrameWrapper):
        classifier_instance.set_params(featureaugmenter__timeseries_container=timeseries)
    classifier_instance.fit(features, targets)

    if isinstance(testdata, PandasDataFrameWrapper):
        test_timeseries = testdata.X
        test_targets = testdata.y
        test_features = pd.DataFrame(index=test_targets.index)
        classifier_instance.set_params(featureaugmenter__timeseries_container=test_timeseries)
    elif sys.argv["regression"]:
        test_features, test_targets = get_feature_target_matrices(testdata)
    else:
        test_features = testdata.input_matrix
    prediction = classifier_instance.predict(test_features)
    serialize_prediction(prediction)

def run_test_mode(data):
    """
    Tests the model that is referenced by the model argument with the given data.
    Returns path to prediction results.
    """
    with open(sys.argv["model"], 'rb') as file:
        classifier_instance = pickle.load(file)
    if isinstance(data, PandasDataFrameWrapper):
        features = data.X
        targets = data.y
    elif sys.argv["regression"]:
        features, targets = get_feature_target_matrices(data)
    else:
        features = data.input_matrix
    prediction = classifier_instance.predict(features)
    serialize_prediction(prediction)

def main():
    print("using seed ", sys.argv["seed"])
    np.random.seed(int(sys.argv["seed"]))
    print("load arff file from ", sys.argv["arff"], sys.argv["rul"])
    data = load_arff_file(sys.argv["arff"], sys.argv["rul"])
    print("run script in mode ", sys.argv["mode"])
    if sys.argv["mode"] == "train":
        run_train_mode(data)
    elif sys.argv["mode"] == "test":
        assert sys.argv["model"]
        run_test_mode(data)
    elif sys.argv["mode"] == "traintest":
        testdata = load_arff_file(sys.argv["testarff"], sys.argv["rul"]);
        run_train_test_mode(data, testdata)

def print_pid():
    print("CURRENT_PID:" + str(os.getpid()))


def limit_memory(maxsize): 
	soft, hard = resource.getrlimit(resource.RLIMIT_AS) 
	resource.setrlimit(resource.RLIMIT_AS, (maxsize, hard)) 

def limit_subprocesses(maxsubprocesses): 
	soft, hard = resource.getrlimit(resource.RLIMIT_NPROC) 
	resource.setrlimit(resource.RLIMIT_NPROC, (maxsubprocesses, hard)) 


if __name__ == "__main__":
    parse_args()
    print_pid()
    OUTPUT_FILE = sys.argv["output"]
    if not sys.argv["regression"] and sys.argv["targets"] and len(sys.argv["targets"]) > 1:
        raise RuntimeError("Multiple targets are not supported for categorical problems.")
    limit_memory(4294967296) # 4GB RAM
    #limit_subprocesses(1) # no parallelization of sklearn
    main()
