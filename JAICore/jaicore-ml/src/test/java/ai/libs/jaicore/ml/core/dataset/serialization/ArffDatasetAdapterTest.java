package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import ai.libs.jaicore.ml.core.dataset.schema.attribute.ThreeDimensionalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.TwoDimensionalAttribute;
import ai.libs.jaicore.ml.pdm.dataset.SensorTimeSeries;
import ai.libs.jaicore.ml.pdm.dataset.SensorTimeSeriesAttribute;
import ai.libs.jaicore.test.LongTest;

public class ArffDatasetAdapterTest {

	private static final String RELATION_NAME = "probing_nonan_noid";
	private static final int CLASS_INDEX = 4;
	private static final String RELATION_STRING = "@relation '" + RELATION_NAME + ": -C " + CLASS_INDEX + "'";

	private static final String ATTRIBUTE_NAME = "myAtt";
	private static final String NUMERIC_ATTRIBUTE_STRING = "@attribute " + ATTRIBUTE_NAME + " numeric";
	private static final List<String> CATEGORICAL_VALUES = Arrays.asList("Agresti", "Ashbacher", "Auken", "Blankenship", "Brody", "Brown", "Bukowsky", "CFH", "Calvinnme", "Chachra", "Chandler", "Chell", "Cholette", "Comdet", "Corn",
			"Cutey", "Davisson", "Dent", "Engineer", "Goonan", "Grove", "Harp", "Hayes", "Janson", "Johnson", "Koenig", "Kolln", "Lawyeraau", "Lee", "Lovitt", "Mahlers2nd", "Mark", "McKee", "Merritt", "Messick", "Mitchell", "Morrison",
			"Neal", "Nigam", "Peterson", "Power", "Riley", "Robert", "Shea", "Sherwin", "Taylor", "Vernon", "Vision", "Walters", "Wilson");
	private static final String NOMINAL_ATTRIBUTE_STRING = "@attribute '" + ATTRIBUTE_NAME + "' {" + SetUtil.implode(CATEGORICAL_VALUES, ",") + "}";
	private static final String SENSOR_TIME_SERIES_ATTRIBUTE_STRING = "@attribute " + ATTRIBUTE_NAME + " timeseries";
	private static final String MULTI_DIMENSIONAL_ATTRIBUTE2D_STRING = "@attribute " + ATTRIBUTE_NAME + " Multidimensional(3,2)";
	private static final String MULTI_DIMENSIONAL_ATTRIBUTE3D_STRING = "@attribute " + ATTRIBUTE_NAME + " Multidimensional(4,3,2)";

	private static final IAttribute TEST_NUM_ATT = new NumericAttribute("numAtt");
	private static final IAttribute TEST_CAT_ATT = new IntBasedCategoricalAttribute("catAtt", CATEGORICAL_VALUES);
	private static final IAttribute TEST_STS_ATT = new SensorTimeSeriesAttribute("sensorTimeSeriesAttibute");
	private static final IAttribute TEST_MUL2D_ATT = new TwoDimensionalAttribute("multidimensionalAttribute", 3, 2);
	private static final IAttribute TEST_MUL3D_ATT = new ThreeDimensionalAttribute(ATTRIBUTE_NAME, 4, 3, 2);

	private static final double TEST_NUMERIC_VAL = 231.0;
	private static final int TEST_CATEGORICAL_VAL = (int) TEST_CAT_ATT.deserializeAttributeValue(CATEGORICAL_VALUES.get(1));
	private static final SensorTimeSeries TEST_STS_VAL = (SensorTimeSeries) TEST_STS_ATT.deserializeAttributeValue("1#0.5 2#0.34 5#93.4");
	private static final double[][] TEST_MUL2D_VAL = { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, 6.0 } };
	private static final double[][][] TEST_MUL3D_VAL = { { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, 6.0 } }, { { 1.1, 2.1 }, { 3.1, 4.1 }, { 5.1, 6.1 } }, { { 1.2, 2.2 }, { 3.2, 4.2 }, { 5.2, 6.2 } },
			{ { 1.3, 2.3 }, { 3.3, 4.3 }, { 5.3, 6.3 } } };

	private static final List<IAttribute> TEST_ATTRIBUTES = Arrays.asList(TEST_NUM_ATT, TEST_STS_ATT, TEST_MUL2D_ATT, TEST_MUL3D_ATT, TEST_CAT_ATT);
	private static final Object[] TEST_INSTANCE = { TEST_NUMERIC_VAL, TEST_STS_VAL, TEST_MUL2D_VAL, TEST_MUL3D_VAL, TEST_CATEGORICAL_VAL };
	private static final Object[] TEST_INSTANCE_WITH_MISSING_NUMERIC_VAL = { null, TEST_STS_VAL, TEST_MUL2D_VAL, TEST_MUL3D_VAL, TEST_CATEGORICAL_VAL };
	private static final Object[] TEST_INSTANCE_WITH_MISSING_CATEGORICAL_VAL = { TEST_NUMERIC_VAL, TEST_STS_VAL, TEST_MUL2D_VAL, TEST_MUL3D_VAL, null };

	private static String generateDenseInstanceString(final List<IAttribute> attributes, final Object[] instanceArray) {
		return IntStream.range(0, instanceArray.length).mapToObj(x -> attributes.get(x).serializeAttributeValue(instanceArray[x])).reduce((a, b) -> a + "," + b).get();
	}

	private static String generateSparseInstanceString(final List<IAttribute> attributes, final Object[] instanceArray) {
		return IntStream.range(0, instanceArray.length).mapToObj(x -> x + " " + attributes.get(x).serializeAttributeValue(instanceArray[x])).reduce((a, b) -> a + "," + b).get();
	}

	@Test
	public void testParseRelation() {
		KVStore store = ArffDatasetAdapter.parseRelation(RELATION_STRING);
		assertEquals(CLASS_INDEX, (int) store.getAsInt(ArffDatasetAdapter.K_CLASS_INDEX), "The class index could not be read correctly");
		assertEquals(RELATION_NAME, store.getAsString(ArffDatasetAdapter.K_RELATION_NAME), "The relation name could not be extracted correctly");
	}

	@Test
	public void testParseNumericAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute = ArffDatasetAdapter.parseAttribute(NUMERIC_ATTRIBUTE_STRING);
		assertTrue(attribute instanceof INumericAttribute, "Returned attribute is not of type INumericAttribtue");
		assertEquals(ATTRIBUTE_NAME, attribute.getName(), "Name of attribute could not be extracted correctly");
	}

	@Test
	public void testTwoDimensionalAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute2d = ArffDatasetAdapter.parseAttribute(MULTI_DIMENSIONAL_ATTRIBUTE2D_STRING);
		assertTrue(attribute2d instanceof TwoDimensionalAttribute, "Returned attribute is not of type {@link TwoDimensionalAttribute} ");
		assertEquals(attribute2d.getName(), (ATTRIBUTE_NAME), "Name of attrtibute could not be extracted correctly");
	}

	@Test
	public void testThreDimensionalAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute3d = ArffDatasetAdapter.parseAttribute(MULTI_DIMENSIONAL_ATTRIBUTE3D_STRING);

		assertTrue(attribute3d instanceof ThreeDimensionalAttribute, "Returned attribute is not of type {@link ThreeDimensionalAttribute}");

		assertEquals(attribute3d.getName(), (attribute3d.getName()), "Name of attrtibute could not be extracted correctly");

	}

	@Test
	public void testParseCategoricalAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute = ArffDatasetAdapter.parseAttribute(NOMINAL_ATTRIBUTE_STRING);
		assertTrue(attribute instanceof ICategoricalAttribute, "Returned attribute is not of type INumericAttribtue");
		assertEquals(ATTRIBUTE_NAME, attribute.getName(), "Name of attribute could not be extracted correctly");
		ICategoricalAttribute catAtt = (ICategoricalAttribute) attribute;
		assertEquals(CATEGORICAL_VALUES.size(), catAtt.getLabels().size(), "Number of categories extracted does not match the real number of categories");
		assertTrue(CATEGORICAL_VALUES.containsAll(catAtt.getLabels()), "Extracted list of categories does not contain all ground truth values");
	}

	@Test
	public void testParseSensorTimeSeriesAttribute() throws UnsupportedAttributeTypeException {
		IAttribute attribute = ArffDatasetAdapter.parseAttribute(SENSOR_TIME_SERIES_ATTRIBUTE_STRING);
		assertTrue(attribute instanceof IObjectAttribute, "Returned attribute is not of type IObjectAttribute");
		assertEquals(ATTRIBUTE_NAME, attribute.getName(), "Name of attribute could not be extracted correctly");
	}

	@Test
	public void testParseDenseInstance() {
		String testInstanceLine = generateDenseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(false, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue(parsedInstance instanceof List<?>, "The returned instance is not in the expected dense instance format");
		Object[] parsedDenseInstance = (Object[]) ((List<?>) parsedInstance).get(0);
		Object label = ((List<?>) parsedInstance).get(1);
		assertEquals(TEST_INSTANCE.length, parsedDenseInstance.length + 1, "The size of the array is varying.");
		for (int i = 0; i < TEST_INSTANCE.length; i++) {
			if (i == CLASS_INDEX) {
				assertEquals(TEST_INSTANCE[i], label, "Attribute value at position " + i + " " + label + " is not equal to the expected value " + TEST_INSTANCE[i]);
			} else {
				if (parsedDenseInstance[i] instanceof double[][]) {
					assertTrue(Arrays.deepEquals((double[][]) parsedDenseInstance[i], (double[][]) TEST_INSTANCE[i]), "Attribute value at position " + i + " " + parsedDenseInstance[i] + " is not equal to the expected value ");
				} else if (parsedDenseInstance[i] instanceof double[][][]) {
					assertTrue(Arrays.deepEquals((double[][][]) parsedDenseInstance[i], (double[][][]) TEST_INSTANCE[i]), "Attribute value at position " + i + " " + parsedDenseInstance[i] + " is not equal to the expected value ");
				} else {
					assertEquals(TEST_INSTANCE[i], parsedDenseInstance[i], "Attribute value at position " + i + " " + parsedDenseInstance[i] + " is not equal to the expected value " + TEST_INSTANCE[i]);
				}
			}
		}
		assertTrue(parsedDenseInstance[0] instanceof Number, "Numeric attribute is not a Number object but of type " + parsedDenseInstance[0].getClass().getName());
		assertNotNull(((List<?>) parsedInstance).get(1), "No instance label for dense instance " + parsedInstance);
	}

	@Test
	public void testParseSparseInstance() {
		String testInstanceLine = generateSparseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE);
		List<Object> parsedInstance = ArffDatasetAdapter.parseInstance(true, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue(parsedInstance.get(0) instanceof Map, "The returned instance is not in the expected sparse instance format");
		@SuppressWarnings("unchecked")
		Map<Integer, Object> parsedSparseInstance = (Map<Integer, Object>) parsedInstance.get(0);
		for (int i = 0; i < TEST_INSTANCE.length; i++) {
			if (i == CLASS_INDEX) {
				continue;
			}
			if (parsedSparseInstance.get(i) instanceof double[][]) {
				assertTrue(Arrays.deepEquals((double[][]) parsedSparseInstance.get(i), (double[][]) TEST_INSTANCE[i]), "Attribute value at position " + i + " " + parsedSparseInstance.get(i) + " is not equal to the expected value ");
			} else if (parsedSparseInstance.get(i) instanceof double[][][]) {
				assertTrue(Arrays.deepEquals((double[][][]) parsedSparseInstance.get(i), (double[][][]) TEST_INSTANCE[i]), "Attribute value at position " + i + " " + parsedSparseInstance.get(i) + " is not equal to the expected value ");
			} else {
				assertEquals(TEST_INSTANCE[i], parsedSparseInstance.get(i), "Attribute value at position " + i + " " + parsedSparseInstance.get(i) + " is not equal to the expected value " + TEST_INSTANCE[i]);
			}
		}
		assertEquals(TEST_INSTANCE[CLASS_INDEX], parsedInstance.get(1), "Target has not been parsed correctly.");

		assertTrue(parsedSparseInstance.get(0) instanceof Number, "Numeric attribute is not a Number object but of type " + parsedSparseInstance.get(0).getClass().getName());
		assertNotNull(parsedInstance.get(1), "No instance label for sparse instance " + parsedSparseInstance);
	}

	@Test
	public void testThatMissingNumericValuesAreNullInDenseInstances() {
		String testInstanceLine = generateDenseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE_WITH_MISSING_NUMERIC_VAL);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(false, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue(parsedInstance instanceof List<?>, "The returned instance is not in the expected dense instance format");
		Object[] parsedDenseInstance = (Object[]) ((List<?>) parsedInstance).get(0);
		Object label = ((List<?>) parsedInstance).get(1);
		assertNull(parsedDenseInstance[0]);
		assertNotNull(label);
	}

	@Test
	public void testThatMissingCategoricalValuesAreNullInDenseInstances() {
		String testInstanceLine = generateDenseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE_WITH_MISSING_CATEGORICAL_VAL);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(false, TEST_ATTRIBUTES, CLASS_INDEX, testInstanceLine);
		assertTrue(parsedInstance instanceof List<?>, "The returned instance is not in the expected dense instance format");
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
		assertNotNull(parsedSparseInstance.get(2));
		assertNotNull(parsedSparseInstance.get(3));
		assertNull(parsedSparseInstance.get(4));
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
	public void testSplitDentInstanceLine() {
		String testLine = "a,\"a\",\"a,a\",'a,b',a,a'a,a\"a,'a'";
		String[] values = ArffDatasetAdapter.splitDenseInstanceLine(testLine);
		assertEquals(8, values.length, "The number of expected entries does not match.");
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
