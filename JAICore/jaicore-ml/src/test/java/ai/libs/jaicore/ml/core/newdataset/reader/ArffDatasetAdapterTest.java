package ai.libs.jaicore.ml.core.newdataset.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.junit.Test;

import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.dataset.reader.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;

public class ArffDatasetAdapterTest {

	private static final String RELATION_NAME = "probing_nonan_noid";
	private static final int CLASS_INDEX = 22;
	private static final String RELATION_STRING = "@relation '" + RELATION_NAME + ": -C " + CLASS_INDEX + "'";

	private static final String ATTRIBUTE_NAME = "myAtt";
	private static final String NUMERIC_ATTRIBUTE_STRING = "@attribute " + ATTRIBUTE_NAME + " numeric";
	private static final List<String> CATEGORICAL_VALUES = Arrays.asList("Agresti", "Ashbacher", "Auken", "Blankenship", "Brody", "Brown", "Bukowsky", "CFH", "Calvinnme", "Chachra", "Chandler", "Chell", "Cholette", "Comdet", "Corn",
			"Cutey", "Davisson", "Dent", "Engineer", "Goonan", "Grove", "Harp", "Hayes", "Janson", "Johnson", "Koenig", "Kolln", "Lawyeraau", "Lee", "Lovitt", "Mahlers2nd", "Mark", "McKee", "Merritt", "Messick", "Mitchell", "Morrison",
			"Neal", "Nigam", "Peterson", "Power", "Riley", "Robert", "Shea", "Sherwin", "Taylor", "Vernon", "Vision", "Walters", "Wilson");
	private static final String NOMINAL_ATTRIBUTE_STRING = "@attribute '" + ATTRIBUTE_NAME + "' {" + SetUtil.implode(CATEGORICAL_VALUES, ",") + "}";

	private static final double TEST_NUMERIC_VAL = 231.0;
	private static final String TEST_CATEGORICAL_VAL = CATEGORICAL_VALUES.get(1);
	private static final Object[] TEST_INSTANCE = { TEST_NUMERIC_VAL, TEST_CATEGORICAL_VAL };
	private static final List<IAttribute> TEST_ATTRIBUTES = Arrays.asList(new NumericAttribute("numAtt"), new CategoricalAttribute("catAtt", CATEGORICAL_VALUES));

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
		assertEquals("Number of categories extracted does not match the real number of categories", CATEGORICAL_VALUES.size(), catAtt.getValues().size());
		assertTrue("Extracted list of categories does not contain all ground truth values", CATEGORICAL_VALUES.containsAll(catAtt.getValues()));
	}

	@Test
	public void testParseDenseInstance() {
		String testInstanceLine = generateDenseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE);
		System.out.println(testInstanceLine);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(false, TEST_ATTRIBUTES, testInstanceLine);
		assertTrue("The returned instance is not in the expected dense instance format", parsedInstance instanceof Object[]);
		Object[] parsedDenseInstance = (Object[]) parsedInstance;
		assertEquals("The size of the array is varying.", TEST_INSTANCE.length, parsedDenseInstance.length);
		for (int i = 0; i < TEST_INSTANCE.length; i++) {
			assertEquals("Attribute value at position " + i + " " + parsedDenseInstance[i] + " is not equal to the expected value " + TEST_INSTANCE[i], TEST_INSTANCE[i], parsedDenseInstance[i]);
		}
	}

	@Test
	public void testParseSparseInstance() {
		String testInstanceLine = generateSparseInstanceString(TEST_ATTRIBUTES, TEST_INSTANCE);
		Object parsedInstance = ArffDatasetAdapter.parseInstance(true, TEST_ATTRIBUTES, testInstanceLine);
		assertTrue("The returned instance is not in the expected sparse instance format", parsedInstance instanceof Map);
		@SuppressWarnings("unchecked")
		Map<Integer, Object> parsedSparseInstance = (Map<Integer, Object>) parsedInstance;
		for (int i = 0; i < TEST_INSTANCE.length; i++) {
			assertEquals("Attribute value at position " + i + " " + parsedSparseInstance.get(i) + " is not equal to the expected value " + TEST_INSTANCE[i], TEST_INSTANCE[i], parsedSparseInstance.get(i));
		}
	}

	@Test
	public void testReadingADatasetFromFile() {
		File datasetFile = new File("classifier-rank.arff");
		ArffDatasetAdapter.deserializeDataset(false, datasetFile);
	}

}
