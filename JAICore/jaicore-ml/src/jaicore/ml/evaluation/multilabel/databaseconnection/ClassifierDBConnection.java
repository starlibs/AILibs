package jaicore.ml.evaluation.multilabel.databaseconnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;
import jaicore.ml.evaluation.multilabel.ClassifierMetricGetter;
import meka.core.Result;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

/**
 * Class that encapsulates an SQLAdapter to get pre-trained classifiers from the
 * db and write them and their metrics to the db.
 *
 * @author Helena Graf
 *
 */
public class ClassifierDBConnection implements Serializable {

	/** Logger for controllable outputs. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassifierDBConnection.class);

	/**
	 * Default generated serial version UID.
	 */
	private static final long serialVersionUID = -603897780567191971L;

	/**
	 * The adapter to access the SQL database.
	 */
	private SQLAdapter adapter;

	/**
	 * Flag which type of evaluation we are dealing with.
	 */
	private EvaluationMode mode = EvaluationMode.Validation;

	/**
	 * The id of the setting which the classifiers and measurements belong to.
	 */
	private int settingId;

	/**
	 * The id of the classifier obtained from the DB at last.
	 */
	private int lastClassifierId;

	/**
	 * The metric id for which this connection can get optimized classifiers.
	 */
	private int metricID;

	/**
	 * If a metric is given to this connection, one can get classifiers optimized
	 * according to this metric. Since "optimal" is determined by the metric
	 * (higher/ lower is better), in this case, the connection will get and remember
	 * the according sort order of the metric (ASC/DESC).
	 */
	private String metricSortOrder;

	private static final String COLUMN_SETTING_ID = "setting_id";
	private static final String COLUMN_METRIC_ID = "metric_id";
	private static final String COLUMN_METRIC_NAME = "metric_name";
	private static final String COLUMN_CLASSIFIER_ID = "classifier_id";
	private static final String COLUMN_CLASSIFIER_NAME = "classifier_name";
	private static final String COLUMN_CLASSIFIER_OBJECT = "classifier_object";
	private static final String COLUMN_CLASSIFIER_TYPE = "classifier_type";
	private static final String COLUMN_MEASUREMENT_TYPE = "measurement_type";
	private static final String COLUMN_SORT_ORDER = "sort_order";
	private static final String COLUMN_MEASURED_VALUE = "measured_value";
	private static final String COLUMN_LABEL_INDEX = "label_index";
	private static final String COLUMN_UPDATED_AT = "column_updated_at";

	private static final String TABLE_CLASSIFIER = "classifier";
	private static final String TABLE_METRIC = "metric";
	private static final String TABLE_MEASUREMENT = "measurement";

	private static final String MEASUREMENT_TYPE_TEST = "test";
	private static final String MEASUREMENT_TYPE_VAL = "val";
	private static final String CLASSIFIER_TYPE_MULTILABEL = "multilabel";
	private static final String CLASSIFIER_TYPE_SINGLELABEL = "singlelabel";

	/**
	 * Creates a new ClassifierDBConnection based on the given setting. It will use
	 * the given adapter to communicate with the db.
	 *
	 * @param adapter
	 *            The adapter used to communicate with the db
	 * @param dataset
	 *            The dataset this setting is evaluated on
	 * @param test_split_tech
	 *            The test split technique
	 * @param test_fold
	 *            The test fold
	 * @param val_split_tech
	 *            The validation split technique
	 * @param val_fold
	 *            The validation fold
	 * @param testSeed
	 *            The seed used to make the test split
	 * @param valSeed
	 *            The seed used to make the validation split
	 * @throws SQLException
	 *             If something goes wrong while connecting to the db
	 */
	public ClassifierDBConnection(final SQLAdapter adapter, final String dataset, final String test_split_tech,
			final String test_fold, final String val_split_tech, final String val_fold, final String testSeed,
			final String valSeed) throws SQLException {
		// Set adapter
		this.adapter = adapter;

		// Check for setting id
		String query = "SELECT setting_id FROM setting WHERE dataset=? AND test_split_tech=? AND test_fold=? AND val_split_tech=? AND val_fold=? AND test_seed = ? AND val_seed=?";
		List<String> values = Arrays.asList(dataset, test_split_tech, test_fold, val_split_tech, val_fold, testSeed,
				valSeed);
		ResultSet resultSet = adapter.getResultsOfQuery(query, values);

		if (resultSet.next()) {
			// Get setting
			this.settingId = resultSet.getInt(COLUMN_SETTING_ID);
		} else {
			// Add setting
			HashMap<String, Object> map = new HashMap<>();
			map.put("dataset", dataset);
			map.put("test_split_tech", test_split_tech);
			map.put("test_fold", test_fold);
			map.put("val_split_tech", val_split_tech);
			map.put("val_fold", val_fold);
			map.put("test_seed", testSeed);
			map.put("val_seed", valSeed);
			this.settingId = adapter.insert("setting", map);
		}
	}

	/**
	 * Creates a new ClassifierDBConnection based on the given setting. It will use
	 * the given adapter to communicate with the db. This constructor additionally
	 * sets the id of a metric for which optimized classifiers can then be obtained.
	 *
	 * @param adapter
	 *            The adapter used to communicate with the db
	 * @param dataset
	 *            The dataset this setting is evaluated on
	 * @param test_split_tech
	 *            The test split technique
	 * @param test_fold
	 *            The test fold
	 * @param val_split_tech
	 *            The validation split technique
	 * @param val_fold
	 *            The validation fold
	 * @param testSeed
	 *            The seed used to make the test split
	 * @param valSeed
	 *            The seed used to make the validation split
	 * @param metricID
	 *            The metric id that shall be used for getting optimized classifiers
	 * @throws SQLException
	 *             If something goes wrong while connecting to the db
	 */
	public ClassifierDBConnection(final SQLAdapter adapter, final String dataset, final String test_split_tech,
			final String test_fold, final String val_split_tech, final String val_fold, final String testSeed,
			final String valSeed, final int metricID) throws SQLException {
		this(adapter, dataset, test_split_tech, test_fold, val_split_tech, val_fold, testSeed, valSeed);

		// Get the sort order of the metric
		String query = String.format("SELECT %s FROM %s WHERE metric_id = ?", COLUMN_SORT_ORDER, TABLE_METRIC);
		List<String> values = Arrays.asList(String.valueOf(metricID));

		ResultSet resultSet = adapter.getResultsOfQuery(query, values);
		if (resultSet.next()) {
			this.metricSortOrder = resultSet.getString(COLUMN_SORT_ORDER);
		} else {
			throw new NoSuchElementException("Given metric is not in data base!");
		}
		this.metricID = metricID;
	}

	public ClassifierDBConnection(final SQLAdapter adapter, final int settingId, final int metricId)
			throws SQLException {
		this(adapter, settingId);

		// Get the sort order of the metric
		String query = String.format("SELECT %s FROM %s WHERE metric_id = ?", COLUMN_SORT_ORDER, TABLE_METRIC);
		List<String> values = Arrays.asList(String.valueOf(metricId));

		ResultSet resultSet = adapter.getResultsOfQuery(query, values);
		if (resultSet.next()) {
			this.metricSortOrder = resultSet.getString(COLUMN_SORT_ORDER);
		} else {
			throw new NoSuchElementException("Given metric is not in data base!");
		}
		this.metricID = metricId;
	}

	public ClassifierDBConnection(final SQLAdapter adapter, final int settingId) {
		this.adapter = adapter;
		this.settingId = settingId;
	}

	/**
	 * Get a pre-trained single label classifier for the setting this connection is
	 * in by the classifier name.
	 *
	 * @param classifierName
	 *            The single label classifier for which to search
	 * @param label
	 *            The label for which the classifier is trained
	 * @return A pre-trained single label classifier or null if not present in
	 *         database
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 * @throws IOException
	 *             If the classifier stored in the database cannot be converted to a
	 *             classifier object
	 * @throws ClassNotFoundException
	 *             If the classifier stored in the database cannot be converted to a
	 *             classifier object
	 */
	public Classifier getPreTrainedSingleLabelClassifierForLabel(final String classifierName, final int label)
			throws SQLException, ClassNotFoundException, IOException {
		// Get fitting classifier from database
		String query = String.format("SELECT * FROM %s WHERE %s=\"%s\" AND %s=? AND %s=? AND %s=?", //
				TABLE_CLASSIFIER, //
				COLUMN_CLASSIFIER_TYPE, //
				CLASSIFIER_TYPE_SINGLELABEL, //
				COLUMN_LABEL_INDEX, //
				COLUMN_CLASSIFIER_NAME, //
				COLUMN_SETTING_ID);
		List<String> values = Arrays.asList(String.valueOf(label), classifierName, String.valueOf(this.settingId));
		return this.getSingleLabelClassifier(label, query, values);
	}

	/**
	 * Gets the best pre-trained classifier for the given label and setting that
	 * this connection is in (for the metric that is to be optimized).
	 *
	 * @param label
	 *            The label for which the classifier should be trained
	 * @return A pre-trained classifier for the given setting and label or null if
	 *         no fitting pre-trained classifier exists in the database
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 * @throws ClassNotFoundException
	 *             If the classifier object cannot be recreated from the database
	 * @throws IOException
	 *             If the classifier object cannot be recreated from the database
	 */
	public Classifier getOptimalPreTrainedSingleLabelClassifierForLabel(final int label)
			throws SQLException, ClassNotFoundException, IOException {
		// Find the best classifier for the metric
		String query = "SELECT A.classifier_id, classifier_object FROM (SELECT classifier_object, classifier_id FROM classifier WHERE setting_id=? AND label_index=? AND classifier_type=\"singlelabel\") AS A INNER JOIN (SELECT classifier_id, measured_value FROM measurement WHERE metric_id=? AND measurement_type=?) AS B ON A.classifier_id=B.classifier_id ORDER BY measured_value "
				+ this.metricSortOrder;
		List<String> values = Arrays.asList(String.valueOf(this.settingId), String.valueOf(label),
				String.valueOf(this.metricID));
		values = new ArrayList<>(values);
		this.addModeToValuesList(values);

		return this.getSingleLabelClassifier(label, query, values);
	}

	/**
	 * Get a classifier from the database according to the specified query
	 *
	 * @param label
	 *            The label for which the classifier should be trained
	 * @param query
	 *            The query
	 * @param values
	 *            The values that shall be used in the query
	 * @return A pre-trained classifier for the given setting and label or null if
	 *         no fitting pre-trained classifier exists in the database
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 * @throws IOException
	 *             If the classifier object cannot be recreated from the database
	 * @throws ClassNotFoundException
	 *             If the classifier object cannot be recreated from the database
	 */
	protected Classifier getSingleLabelClassifier(final int label, final String query, final List<String> values)
			throws SQLException, IOException, ClassNotFoundException {
		ResultSet resultSet = this.adapter.getResultsOfQuery(query, values);

		// If fitting classifiers in db, just take the first one
		if (resultSet.next()) {
			this.lastClassifierId = resultSet.getInt(COLUMN_CLASSIFIER_ID);
			Object obj = this.readBytesToObject(resultSet.getBytes(COLUMN_CLASSIFIER_OBJECT));
			return (Classifier) obj;
		}

		// Empty resultSet, return null (no fitting classifier in db)
		LOGGER.warn("No fitting pre-trained classifier in database for label {} in setting {}", label, this.settingId);
		return null;
	}

	/**
	 * Get a pre-trained multi label classifier for the setting this connection is
	 * in by the classifier name.
	 *
	 * @param classifierName
	 *            The multi label classifier for which to search
	 * @return A pre-trained multi label classifier or null if not present in db
	 * @throws SQLException
	 *             If something goes wrong while connecting to the db
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object getPreTrainedMultilabelClassifier(final String classifierName)
			throws SQLException, ClassNotFoundException, IOException {
		// Get fitting classifier from db
		String query = "SELECT * FROM classifier WHERE classifier_type=\"multilabel\" AND classifier_name=? AND setting_id=?";
		List<String> values = Arrays.asList(classifierName, String.valueOf(this.settingId));
		ResultSet resultSet = this.adapter.getResultsOfQuery(query, values);

		// If fitting classifiers in db, just take the first one
		if (resultSet.next()) {
			this.lastClassifierId = resultSet.getInt(COLUMN_CLASSIFIER_ID);
			return this.readBytesToObject(resultSet.getBytes(COLUMN_CLASSIFIER_OBJECT));
		} else {
			this.lastClassifierId = -1;
		}

		// Empty resultSet, return null (no fitting classifier in db)
		return null;
	}

	/**
	 * Gets an optimal pre trained multi label classifier for the setting this
	 * connection is in and metric it has been initialized with. If there exists a
	 * multi label classifier in the database which could be considered optimal, it
	 * is first checked if since the creation of that multi label classifier, a
	 * single label classifier has been updated which would indicate the possibility
	 * for a better multi label classifier, and thus, null is returned in this case.
	 *
	 * @param classifierName
	 *            The multi label classifier for which to search
	 * @return A pre-trained multi label classifier or null if not present in
	 *         database
	 * @throws SQLException
	 *             If something goes wrong while connecting to the databaseF
	 * @throws ClassNotFoundException
	 *             If the classifier object cannot be converted from its
	 *             serialization
	 * @throws IOException
	 *             If the classifier object cannot be converted from its
	 *             serialization
	 */
	public Object getOptimalPreTrainedMultilabelClassifier(final String classifierName)
			throws SQLException, ClassNotFoundException, IOException {
		// Get fitting classifier from database
		String query = "SELECT A.classifier_id, classifier_object, updated_at FROM (SELECT classifier_object, classifier_id, updated_at FROM classifier WHERE setting_id=? AND label_index=? AND classifier_type=\"multilabel\" AND classifier_name=?) AS A INNER JOIN (SELECT classifier_id, measured_value FROM measurement WHERE metric_id=? AND measurement_type=?) AS B ON A.classifier_id=B.classifier_id ORDER BY measured_value "
				+ this.metricSortOrder;
		List<String> values = Arrays.asList(String.valueOf(this.settingId), String.valueOf(-1), classifierName,
				String.valueOf(this.metricID));
		ResultSet resultSet = this.adapter.getResultsOfQuery(query, values);

		if (resultSet.next()) {
			// If exists, check if it might be outdated (check if there exists a later
			// version of a single label classifier in the same setting -> this means there
			// might be a better multilabel classifier possible now.)
			query = String.format("SELECT %s FROM %s WHERE %s=? AND %s=\"%s\"", //
					COLUMN_UPDATED_AT, //
					TABLE_CLASSIFIER, //
					COLUMN_SETTING_ID, //
					COLUMN_CLASSIFIER_TYPE, //
					CLASSIFIER_TYPE_SINGLELABEL);
			values = Arrays.asList(String.valueOf(this.settingId));
			ResultSet singlelableResultSet = this.adapter.getResultsOfQuery(query, values);

			Timestamp singleLabelTimestamp = singlelableResultSet.getTimestamp(COLUMN_UPDATED_AT);
			Timestamp multiLabelTimestamp = resultSet.getTimestamp(COLUMN_UPDATED_AT);
			if (multiLabelTimestamp.after(singleLabelTimestamp)) {
				this.lastClassifierId = resultSet.getInt(COLUMN_CLASSIFIER_ID);
				return this.readBytesToObject(resultSet.getBytes(COLUMN_CLASSIFIER_OBJECT));
			}
			LOGGER.debug("Multilabel Classifier {} is old.", classifierName);
		}
		return null;
	}

	/**
	 * Update the pre-trained multi label classifier with the given id to the new
	 * given object.
	 *
	 * @param id
	 *            The id of the classifier to update
	 * @param classifier
	 *            The new object for the classifier
	 * @throws IOException
	 *             If the object cannot be serialized
	 * @throws SQLException
	 *             If something goes wrong while connecting to the db
	 */
	public void updatePreTrainedMultilabelClassifier(final int id, final Object classifier)
			throws IOException, SQLException {
		/* Set the new classifier object */
		Map<String, Object> updateValues = new HashMap<>();
		updateValues.put(COLUMN_CLASSIFIER_OBJECT, this.objectToByteArray(classifier));

		/* Take the id as a unique identifier for the respective classifier */
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(COLUMN_CLASSIFIER_ID, id);

		/* Send the update to the DB */
		this.adapter.update(TABLE_CLASSIFIER, updateValues, conditions);
	}

	/**
	 * Adds the given pre-trained multilabelclassifier to the database.
	 *
	 * @param classifier
	 *            The classifier to add to the database
	 * @return The id of the newly added classifier in the database
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 * @throws IOException
	 *             Thrown if the classifier could not be converted into an byte
	 *             array.
	 */
	public int addPreTrainedMultilabelClassifier(final String classifierName, final Object classifier)
			throws SQLException, IOException {
		HashMap<String, Object> map = new HashMap<>();
		map.put(COLUMN_SETTING_ID, this.settingId);
		map.put(COLUMN_LABEL_INDEX, -1);
		map.put(COLUMN_CLASSIFIER_NAME, classifierName);
		map.put(COLUMN_CLASSIFIER_OBJECT, this.objectToByteArray(classifier));
		map.put(COLUMN_CLASSIFIER_TYPE, CLASSIFIER_TYPE_MULTILABEL);
		return this.adapter.insert(TABLE_CLASSIFIER, map);
	}

	/**
	 * Add the given pre-trained single label classifier to the database.
	 *
	 * @param classifier
	 *            The pre-trained classifier object
	 * @param label_index
	 *            The label index it was trained for
	 * @return The id of the newly created entry for the classifier
	 * @throws IOException
	 *             If the classifier cannot be serialized to the database
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 *
	 */
	public int addPreTrainedSingleLabelClassifier(final Classifier classifier, final int label_index)
			throws IOException, SQLException {
		Map<String, Object> map = new HashMap<>();
		map.put(COLUMN_SETTING_ID, this.settingId);
		map.put(COLUMN_LABEL_INDEX, label_index);
		map.put(COLUMN_CLASSIFIER_NAME, classifier.getClass().getName());
		map.put(COLUMN_CLASSIFIER_TYPE, CLASSIFIER_TYPE_SINGLELABEL);

		// If the classifier is not lazy, add a classifier object
		if (!classifier.getClass().getName().contains("lazy")) {
			map.put(COLUMN_CLASSIFIER_OBJECT, this.objectToByteArray(classifier));
		} else {

			// If the classifier is lazy, don't add it if one already exists for this
			// setting

			String queryString = String.format("SELECT * FROM %s WHERE %s=? AND %s=? AND %s=? AND %s='%s'", //
					TABLE_CLASSIFIER, //
					COLUMN_SETTING_ID, //
					COLUMN_LABEL_INDEX, //
					COLUMN_CLASSIFIER_NAME, //
					COLUMN_CLASSIFIER_TYPE, //
					CLASSIFIER_TYPE_SINGLELABEL);
			List<String> values = Arrays.asList(String.valueOf(this.settingId), String.valueOf(label_index),
					classifier.getClass().getName());
			ResultSet res = this.adapter.getResultsOfQuery(queryString, values);

			if (res.next()) {
				return res.getInt(COLUMN_CLASSIFIER_ID);
			} else {
				map.put(COLUMN_CLASSIFIER_OBJECT, this.objectToByteArray(null));
			}
		}
		return this.adapter.insert(TABLE_CLASSIFIER, map);
	}

	/**
	 * Adds an entry for the classifier but doesn't add an object for it. For
	 * classifiers that aren't loaded from the db but for which we want to record
	 * measures. Only adds the entry if the classifier didn't exist previously. If
	 * it did exist previously, then returns the existing id.
	 * 
	 * @param classifierName
	 *            the name of the classifier to be reorded
	 * @return the id of the newly created entry
	 * @throws IOException
	 * @throws SQLException
	 */
	public int getOrCreateIdForClassifier(final String classifierName) throws IOException, SQLException {

		Integer id = getExistingIdOfClassifier(classifierName);

		if (id == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put(COLUMN_SETTING_ID, this.settingId);
			map.put(COLUMN_LABEL_INDEX, -1);
			map.put(COLUMN_CLASSIFIER_NAME, classifierName);
			map.put(COLUMN_CLASSIFIER_OBJECT, this.objectToByteArray(null));
			map.put(COLUMN_CLASSIFIER_TYPE, CLASSIFIER_TYPE_MULTILABEL);
			return this.adapter.insert(TABLE_CLASSIFIER, map);
		} else {
			return id;
		}
	}

	/**
	 * Get the id of the classifier identified by classifierName, null if it doesn't
	 * exist in the db.
	 * 
	 * @param classifierName
	 *            the classifier for which to get the id
	 * @return the id of the classifier
	 * @throws SQLException
	 *             if something goes wrong while connecting to the db
	 */
	public Integer getExistingIdOfClassifier(final String classifierName) throws SQLException {
		// Get fitting classifier from db
		String query = "SELECT * FROM classifier WHERE classifier_type=\"multilabel\" AND classifier_name=? AND setting_id=?";
		List<String> values = Arrays.asList(classifierName, String.valueOf(this.settingId));
		ResultSet resultSet = this.adapter.getResultsOfQuery(query, values);

		// If fitting classifiers in db, just take the first one
		if (resultSet.next()) {
			this.lastClassifierId = resultSet.getInt(COLUMN_CLASSIFIER_ID);
			return lastClassifierId;
		} else {
			this.lastClassifierId = -1;
		}

		// Empty resultSet, return null (no fitting classifier in db)
		return null;
	}

	/**
	 * Reads a byte array and converts it into a Java object.
	 *
	 * @param buf
	 *            The byte array that is to be converted into a Java object.
	 * @return The Java object resulting from the byte array.
	 *
	 * @throws IOException
	 *             May throw an IOException if the byte array is malformed.
	 * @throws ClassNotFoundException
	 *             Throws an ClassNotFoundException if the ObjectInputStream cannot
	 *             convert the bytes into an Object instance.
	 */
	private Object readBytesToObject(final byte[] buf) throws IOException, ClassNotFoundException {
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf))) {
			return in.readObject();
		}
	}

	/**
	 * Convert the given Object into a byte array.
	 *
	 * @param objectToConvert
	 *            The object that needs to be converted into a byte array.
	 * @return The byte array encoding the provided object.
	 * @throws IOException
	 *             This exception is thrown if the output streams cannot be closed.
	 */
	private byte[] objectToByteArray(final Object objectToConvert) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(baos)) {
			out.writeObject(objectToConvert);
			return baos.toByteArray();
		}
	}

	/**
	 * Adds the given measurements to the database if they are not present yet.
	 *
	 * @param classifierId
	 *            The classifier for which to add measurements
	 * @param result
	 *            The {@link Result} containing the evaluation results
	 * @param metricIdsWithNames
	 *            The ids and names of the metrics that should be added
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 */
	public void addMeasurementsForMultilabelClassifierIfNotExists(final int classifierId, final Result result,
			final Map<Integer, String> metricIdsWithNames) throws SQLException {
		LOGGER.debug("Get missing classifier metrics...");
		Map<Integer, String> selectedMetrics = this.getMissingClassifierMetrics(classifierId, metricIdsWithNames);
		LOGGER.debug("Add measurements for the multilabel classifier...");
		this.addMeasurementsForMultilabelClassifier(classifierId, result, selectedMetrics);
	}

	/**
	 * Adds the measurements made for each metric given in the metric ids with
	 * parameters to the db. Only adds measurements that are not NaN.
	 *
	 * @param classifierId
	 *            The classifier for which to add measurements
	 * @param result
	 *            The {@link Result} containing the evaluation result
	 * @param metricIdsWithNames
	 *            The ids and names of the metrics that should be added
	 * @throws SQLException
	 *             If something goes wrong while connecting to the db
	 */
	public void addMeasurementsForMultilabelClassifier(final int classifierId, final Result result,
			final Map<Integer, String> metricIdsWithNames) throws SQLException {
		for (Entry<Integer, String> metricWithId : metricIdsWithNames.entrySet()) {
			double measuredValue;
			try {
				measuredValue = ClassifierMetricGetter.getValueOfMultilabelClassifier(result, metricWithId.getValue());
			} catch (Exception e) {
				LOGGER.warn("Exception while trying to get the value of metric {} for classifier {}",
						metricWithId.getKey(), classifierId);
				continue;
			}

			LOGGER.trace("Add measured value {} for metric with id {} for classifier with id {}", measuredValue,
					metricWithId.getKey(), classifierId);
			this.addMeasurementForClassifier(classifierId, metricWithId.getKey(), measuredValue);
		}
	}

	/**
	 * Adds the given measurements to the database if they are not present yet.
	 *
	 * @param classifierId
	 *            The classifier for which to add measurements
	 * @param evaluation
	 *            The {@link Evaluation} containing the evaluation results
	 * @param metricIdsWithNames
	 *            The ids and names of the metrics that should be added
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 */
	public void addMeasurementsForSingleLableClassifierIfNotExists(final int classifierId, final Evaluation evaluation,
			final int classIndex, final Map<Integer, String> metricNames) throws SQLException {
		Map<Integer, String> selectedMetric = this.getMissingClassifierMetrics(classifierId, metricNames);
		this.addMeasurementsForSinglelabelClassifier(classifierId, evaluation, classIndex, selectedMetric);
	}

	/**
	 * Adds the measurements made for each metric given in the metric ids with
	 * parameters to the database. Only adds measurements that are not NaN, infinite
	 * or null.
	 *
	 * @param classifierId
	 *            The classifier for which to add measurements
	 * @param evaluation
	 *            The {@link Evaluation} containing the evaluation result
	 * @param metricIdsWithNames
	 *            The ids and names of the metrics that should be added
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 */
	public void addMeasurementsForSinglelabelClassifier(final int classifierId, final Evaluation evaluation,
			final int classIndex, final Map<Integer, String> namedMetrics) throws SQLException {
		for (Entry<Integer, String> namedMetric : namedMetrics.entrySet()) {
			Double measuredValue;
			try {
				measuredValue = ClassifierMetricGetter.getValueOfMetricForSingleLabelClassifier(evaluation,
						namedMetric.getValue(), classIndex);
			} catch (Exception e) {
				LOGGER.warn("Exception while trying to get metric {} for classifier {}.", namedMetric.getKey(),
						classifierId);
				continue;
			}
			this.addMeasurementForClassifier(classifierId, namedMetric.getKey(), measuredValue);
		}
	}

	/**
	 * Add the given measurement to the database unless the measured value is null,
	 * NaN or infinite.
	 *
	 * @param classifierId
	 *            The id of the classifier for which the value was measured
	 * @param metricId
	 *            The id of the metric for which the value was measured
	 * @param measuredValue
	 *            The measured value
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 */
	protected void addMeasurementForClassifier(final int classifierId, final int metricId, final double measuredValue)
			throws SQLException {
		if (this.isDoubleValueInvalid(measuredValue)) {
			LOGGER.warn("Did not insert value for metric {} as the value was invalid (NaN or infinite)", metricId);
			return;
		}

		Map<String, Object> map = new HashMap<>();
		map.put(COLUMN_MEASURED_VALUE, measuredValue);
		map.put(COLUMN_METRIC_ID, metricId);
		map.put(COLUMN_CLASSIFIER_ID, classifierId);

		if (this.mode == EvaluationMode.Test) {
			map.put(COLUMN_MEASUREMENT_TYPE, MEASUREMENT_TYPE_TEST);
		} else {
			map.put(COLUMN_MEASUREMENT_TYPE, MEASUREMENT_TYPE_VAL);
		}
		this.adapter.insert(TABLE_MEASUREMENT, map);
	}

	/**
	 * Reduces the list of the given metrics to only the metrics that so far are not
	 * presents in the database regarding measurements in the current setting for
	 * the current classifier to avoid inserting duplicate values.
	 *
	 * @param classifierId
	 *            The id of the classifier for which measurements shall be inserted
	 * @param metricNames
	 *            The ids of the metrics for which measurements were made
	 * @return A reduced list of metrics only including metrics that are not present
	 *         in the database
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 */
	protected Map<Integer, String> getMissingClassifierMetrics(final int classifierId,
			final Map<Integer, String> metricNames) throws SQLException {
		// Build query
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("SELECT * FROM %s WHERE %s IN (", TABLE_MEASUREMENT, COLUMN_METRIC_ID));

		int[] metricIds = metricNames.keySet().stream().mapToInt(value -> value).toArray();
		for (int i = 0; i < metricIds.length; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(metricIds[i]);
		}
		builder.append(String.format(") AND %s=? AND %s=? ", COLUMN_CLASSIFIER_ID, COLUMN_MEASUREMENT_TYPE));

		ArrayList<String> values = new ArrayList<>();
		values.add(String.valueOf(classifierId));

		this.addModeToValuesList(values);

		// Remove all metrics that are already in the database from the map
		ResultSet resultSet = this.adapter.getResultsOfQuery(builder.toString(), values);
		Map<Integer, String> selectedMetric = new HashMap<>();
		selectedMetric.putAll(metricNames);

		while (resultSet.next()) {
			selectedMetric.remove(resultSet.getInt(COLUMN_METRIC_ID));
		}
		return selectedMetric;
	}

	/**
	 * Add the current mode to the list of values
	 *
	 * @param values
	 *            The list of values to which to append the current mode
	 */
	protected void addModeToValuesList(final List<String> values) {
		if (this.mode == EvaluationMode.Test) {
			values.add(MEASUREMENT_TYPE_TEST);
		} else {
			values.add(MEASUREMENT_TYPE_VAL);
		}
	}

	/**
	 * Checks if the given double would be fit to be written to the db
	 *
	 * @param d
	 *            The double to check
	 * @return True if the double is null, NaN or infinite (+/- infinity)
	 */
	protected boolean isDoubleValueInvalid(final Double d) {
		return d == null || Double.isNaN(d) || Double.isInfinite(d);
	}

	/**
	 * Gets the id of the latest version of the metric with the given name from the
	 * database.
	 *
	 * @param metricName
	 *            The metric for which to get the id
	 * @return The id of the latest version of the metric, null if not present
	 * @throws SQLException
	 *             If something goes wrong while connecting to the db
	 */
	public Integer getLatestIdForMetric(final String metricName) throws SQLException {
		// Get metric
		String query = String.format("SELECT %s FROM %s WHERE %s=? ORDER BY %s DESC", COLUMN_METRIC_ID, TABLE_METRIC,
				COLUMN_METRIC_NAME, COLUMN_UPDATED_AT);
		List<String> values = Arrays.asList(metricName);
		ResultSet resultSet = this.adapter.getResultsOfQuery(query, values);

		// Return latest
		if (resultSet.next()) {
			return resultSet.getInt(COLUMN_METRIC_ID);
		} else {
			LOGGER.warn("Requested metric with name {} which is not present in the db.", metricName);
			return null;
		}
	}

	/**
	 * Get the id of the last classifier that has been retrieved from the database
	 * (does not include created entries).
	 *
	 * @return The id of the last retrieved classifier through this class
	 */
	public int getLastClassifierId() {
		return this.lastClassifierId;
	}

	/**
	 * Get the evaluation mode this connection is in (influences if the metrics add
	 * to the database are in validation or test setting).
	 *
	 * @return The mode this connection is in
	 */
	public EvaluationMode getMode() {
		return this.mode;
	}

	/**
	 * Set the evaluation mode for this connection (influences if the metrics add to
	 * the database are in validation or test setting).
	 *
	 * @param mode
	 *            The mode this connection will be in
	 */
	public void setMode(final EvaluationMode mode) {
		this.mode = mode;
	}

}
