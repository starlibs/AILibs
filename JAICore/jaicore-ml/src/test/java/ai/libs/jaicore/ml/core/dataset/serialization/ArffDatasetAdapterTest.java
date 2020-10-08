package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IObjectAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.dataset.DatasetTestUtil;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.pdm.dataset.SensorTimeSeries;
import ai.libs.jaicore.ml.pdm.dataset.SensorTimeSeriesAttribute;
import ai.libs.jaicore.test.LongTest;

public class ArffDatasetAdapterTest {

	private static final String RELATION_NAME = "probing_nonan_noid";
	private static final int CLASS_INDEX = 2;
	private static final String RELATION_STRING = "@relation '" + RELATION_NAME + ": -C " + CLASS_INDEX + "'";

	private static final String ATTRIBUTE_NAME = "myAtt";
	private static final String NUMERIC_ATTRIBUTE_STRING = "@attribute " + ATTRIBUTE_NAME + " numeric";
	private static final List<String> CATEGORICAL_VALUES = Arrays.asList("Agresti", "Ashbacher", "Auken", "Blankenship", "Brody", "Brown", "Bukowsky", "CFH", "Calvinnme", "Chachra", "Chandler", "Chell", "Cholette", "Comdet", "Corn",
			"Cutey", "Davisson", "Dent", "Engineer", "Goonan", "Grove", "Harp", "Hayes", "Janson", "Johnson", "Koenig", "Kolln", "Lawyeraau", "Lee", "Lovitt", "Mahlers2nd", "Mark", "McKee", "Merritt", "Messick", "Mitchell", "Morrison",
			"Neal", "Nigam", "Peterson", "Power", "Riley", "Robert", "Shea", "Sherwin", "Taylor", "Vernon", "Vision", "Walters", "Wilson");
	private static final String NOMINAL_ATTRIBUTE_STRING = "@attribute '" + ATTRIBUTE_NAME + "' {" + SetUtil.implode(CATEGORICAL_VALUES, ",") + "}";
	private static final String SENSOR_TIME_SERIES_ATTRIBUTE_STRING = "@attribute " + ATTRIBUTE_NAME + " timeseries";

	private static final IAttribute TEST_NUM_ATT = new NumericAttribute("numAtt");
	private static final IAttribute TEST_CAT_ATT = new IntBasedCategoricalAttribute("catAtt", CATEGORICAL_VALUES);
	private static final IAttribute TEST_STS_ATT = new SensorTimeSeriesAttribute("sensorTimeSeriesAttibute");

	private static final double TEST_NUMERIC_VAL = 231.0;
	private static final int TEST_CATEGORICAL_VAL = (int) TEST_CAT_ATT.deserializeAttributeValue(CATEGORICAL_VALUES.get(1));
	private static final SensorTimeSeries TEST_STS_VAL = (SensorTimeSeries) TEST_STS_ATT.deserializeAttributeValue("1#0.5 2#0.34 5#93.4");
	private static final Object[] TEST_INSTANCE = { TEST_NUMERIC_VAL, TEST_STS_VAL, TEST_CATEGORICAL_VAL };
	private static final Object[] TEST_INSTANCE_WITH_MISSING_NUMERIC_VAL = { null, TEST_STS_VAL, TEST_CATEGORICAL_VAL };
	private static final Object[] TEST_INSTANCE_WITH_MISSING_CATEGORICAL_VAL = { TEST_NUMERIC_VAL, TEST_STS_VAL, null };
	private static final List<IAttribute> TEST_ATTRIBUTES = Arrays.asList(TEST_NUM_ATT, TEST_STS_ATT, TEST_CAT_ATT);

	private static String generateDenseInstanceString(final List<IAttribute> attributes, final Object[] instanceArray) {
		return IntStream.range(0, instanceArray.length).mapToObj(x -> attributes.get(x).serializeAttributeValue(instanceArray[x])).reduce((a, b) -> a + "," + b).get();
	}

	private static String generateSparseInstanceString(final List<IAttribute> attributes, final Object[] instanceArray) {
		return IntStream.range(0, instanceArray.length).mapToObj(x -> x + " " + attributes.get(x).serializeAttributeValue(instanceArray[x])).reduce((a, b) -> a + "," + b).get();
	}

	@Test
	public void testParseRelation() {
		KVStore store = ArffDatasetAdapter.parseRelation(RELATION_STRING);
		assertEquals("The class index could not be read correctly", CLASS_INDEX, (int) store.getAsInt(ArffDatasetAdapter.K_CLASS_INDEX));
		assertEquals("The relation name could not be extracted correctly", RELATION_NAME, store.getAsString(ArffDatasetAdapter.K_RELATION_NAME));
	}

	@Test
	public void testParseNumericAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute = ArffDatasetAdapter.parseAttribute(NUMERIC_ATTRIBUTE_STRING);
		assertTrue("Returned attribute is not of type INumericAttribtue", attribute instanceof INumericAttribute);
		assertEquals("Name of attribute could not be extracted correctly", ATTRIBUTE_NAME, attribute.getName());
	}

	@Test
	public void testParseCategoricalAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute = ArffDatasetAdapter.parseAttribute(NOMINAL_ATTRIBUTE_STRING);
		assertTrue("Returned attribute is not of type INumericAttribtue", attribute instanceof ICategoricalAttribute);
		assertEquals("Name of attribute could not be extracted correctly", ATTRIBUTE_NAME, attribute.getName());
		ICategoricalAttribute catAtt = (ICategoricalAttribute) attribute;
		assertEquals("Number of categories extracted does not match the real number of categories", CATEGORICAL_VALUES.size(), catAtt.getLabels().size());
		assertTrue("Extracted list of categories does not contain all ground truth values", CATEGORICAL_VALUES.containsAll(catAtt.getLabels()));
	}

	@Test
	public void testParseSensorTimeSeriesAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute = ArffDatasetAdapter.parseAttribute(SENSOR_TIME_SERIES_ATTRIBUTE_STRING);
		assertTrue("Returned attribute is not of type IObjectAttribute", attribute instanceof IObjectAttribute);
		assertEquals("Name of attribute could not be extracted correctly", ATTRIBUTE_NAME, attribute.getName());
	}

	@Test
	public void testParseDenseInstance() {
		String testInstanceLine = generateDenseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(false, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue("The returned instance is not in the expected dense instance format", parsedInstance instanceof List<?>);
		Object[] parsedDenseInstance = (Object[]) ((List<?>) parsedInstance).get(0);
		Object label = ((List<?>) parsedInstance).get(1);
		assertEquals("The size of the array is varying.", TEST_INSTANCE.length, parsedDenseInstance.length + 1);
		for (int i = 0; i < TEST_INSTANCE.length; i++) {
			if (i == CLASS_INDEX) {
				assertEquals("Attribute value at position " + i + " " + label + " is not equal to the expected value " + TEST_INSTANCE[i], TEST_INSTANCE[i], label);
			} else {
				assertEquals("Attribute value at position " + i + " " + parsedDenseInstance[i] + " is not equal to the expected value " + TEST_INSTANCE[i], TEST_INSTANCE[i], parsedDenseInstance[i]);
			}
		}
		assertTrue("Numeric attribute is not a Number object but of type " + parsedDenseInstance[0].getClass().getName(), parsedDenseInstance[0] instanceof Number);
		assertNotNull("No instance label for dense instance " + parsedInstance, ((List<?>) parsedInstance).get(1));
	}

	@Test
	public void testParseSparseInstance() {
		String testInstanceLine = generateSparseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE);
		List<Object> parsedInstance = ArffDatasetAdapter.parseInstance(true, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue("The returned instance is not in the expected sparse instance format", parsedInstance.get(0) instanceof Map);
		@SuppressWarnings("unchecked")
		Map<Integer, Object> parsedSparseInstance = (Map<Integer, Object>) parsedInstance.get(0);
		for (int i = 0; i < TEST_INSTANCE.length; i++) {
			if (i == CLASS_INDEX) {
				continue;
			}
			assertEquals("Attribute value at position " + i + " " + parsedSparseInstance.get(i) + " is not equal to the expected value " + TEST_INSTANCE[i], TEST_INSTANCE[i], parsedSparseInstance.get(i));
		}
		assertEquals("Target has not been parsed correctly.", TEST_INSTANCE[CLASS_INDEX], parsedInstance.get(1));

		assertTrue("Numeric attribute is not a Number object but of type " + parsedSparseInstance.get(0).getClass().getName(), parsedSparseInstance.get(0) instanceof Number);
		assertNotNull("No instance label for sparse instance " + parsedSparseInstance, parsedInstance.get(1));
	}

	@Test
	public void testThatMissingNumericValuesAreNullInDenseInstances() {
		String testInstanceLine = generateDenseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE_WITH_MISSING_NUMERIC_VAL);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(false, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue("The returned instance is not in the expected dense instance format", parsedInstance instanceof List<?>);
		Object[] parsedDenseInstance = (Object[]) ((List<?>) parsedInstance).get(0);
		Object label = ((List<?>) parsedInstance).get(1);
		assertNull(parsedDenseInstance[0]);
		assertNotNull(label);
	}

	@Test
	public void testThatMissingCategoricalValuesAreNullInDenseInstances() {
		String testInstanceLine = generateDenseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE_WITH_MISSING_CATEGORICAL_VAL);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(false, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue("The returned instance is not in the expected dense instance format", parsedInstance instanceof List<?>);
		Object[] parsedDenseInstance = (Object[]) ((List<?>) parsedInstance).get(0);
		Object label = ((List<?>) parsedInstance).get(1);
		assertNull(label);
		assertNotNull(parsedDenseInstance[0]);
	}

	@Test
	public void testThatMissingNumericValuesAreNullInSparseInstances() {
		String testInstanceLine = generateSparseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE_WITH_MISSING_NUMERIC_VAL);
		List<Object> parsedInstance = ArffDatasetAdapter.parseInstance(true, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		@SuppressWarnings("unchecked")
		Map<Integer, Object> parsedSparseInstance = (Map<Integer, Object>) parsedInstance.get(0);
		assertNull(parsedSparseInstance.get(0));
		assertNotNull(parsedSparseInstance.get(1));
		assertNotNull(parsedInstance.get(1));
	}

	@Test
	public void testThatMissingCategoricalValuesAreNullInSparseInstances() {
		String testInstanceLine = generateSparseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE_WITH_MISSING_CATEGORICAL_VAL);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(true, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		@SuppressWarnings("unchecked")
		Map<Integer, Object> parsedSparseInstance = (Map<Integer, Object>) ((List<Object>) parsedInstance).get(0);
		assertNotNull(parsedSparseInstance.get(0));
		assertNotNull(parsedSparseInstance.get(1));
		assertNull(parsedSparseInstance.get(2));
	}

	@Test
	public void testReadingSingleLabelDenseDatasetFromFile() throws DatasetDeserializationFailedException, InterruptedException {
		this.testReadingDatasetFromFile(new File("testrsc/dataset/arff/krvskp.arff"));
	}

	@Test
	@LongTest
	public void testReadingSingleLabelSparseDatasetFromFile() throws DatasetDeserializationFailedException, InterruptedException {
		this.testReadingDatasetFromFile(new File("testrsc/dataset/arff/dexter.arff"));
	}

	private void testReadingDatasetFromFile(final File datasetFile) throws DatasetDeserializationFailedException, InterruptedException {
		ILabeledDataset<?> ds = ArffDatasetAdapter.readDataset(datasetFile);
		DatasetTestUtil.checkDatasetCoherence(ds);
	}

	@Test
	public void testWritingASingleLabelDatasetToFile() throws DatasetDeserializationFailedException, IOException {
		File datasetFile = new File("testrsc/dataset/arff/krvskp.arff");
		ILabeledDataset<?> ds = ArffDatasetAdapter.readDataset(datasetFile);
		File datasetCopyFile = new File("testrsc/dataset/arff/krvskp.arff.copy");
		ArffDatasetAdapter.serializeDataset(datasetCopyFile, ds);
		assertEquals(ds, ArffDatasetAdapter.readDataset(datasetCopyFile));
		Files.delete(datasetCopyFile.toPath());
	}
}
