package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.StringAttribute;

public class CSVDatasetAdapterTest {

	@Test
	public void testWriteDataset() throws DatasetDeserializationFailedException, IOException {
		ILabeledDataset<ILabeledInstance> dataset = new OpenMLDatasetReader().deserializeDataset(31);
		File f = new File("test.csv");
		CSVDatasetAdapter.writeDataset(f, dataset);
		assertTrue(f.exists());
	}

	@Test
	public void testReadDataset_OpenML()
			throws DatasetDeserializationFailedException, DatasetCreationException, InterruptedException {
		File testCsvFile = new File("testrsc/dataset/csv/OpenML_42731.csv");
		List<String> categoricalColumns = Arrays.asList("waterfront", "view", "condition", "grade", "zipcode");
		List<String> ignoredColumns = Arrays.asList("id");

		ILabeledDataset<ILabeledInstance> dataset = CSVDatasetAdapter.readDataset(testCsvFile.getAbsolutePath(),
				"price", categoricalColumns, ignoredColumns);

		assertDataSetOk(dataset, categoricalColumns, ignoredColumns);
		assertDataSetOk((ILabeledDataset<ILabeledInstance>) dataset.createCopy(), categoricalColumns, ignoredColumns);
	}

	@Test
	public void testReadDataset_Custom()
			throws DatasetDeserializationFailedException, DatasetCreationException, InterruptedException {
		File testCsvFile = new File("testrsc/dataset/csv/another_housing.csv");
		List<String> categoricalColumns = Arrays.asList("waterfront", "view", "condition", "grade", "zipcode");
		List<String> ignoredColumns = Arrays.asList("id");

		ILabeledDataset<ILabeledInstance> dataset = CSVDatasetAdapter.readDataset(testCsvFile.getAbsolutePath(),
				"price", categoricalColumns, ignoredColumns);

		assertDataSetOk(dataset, categoricalColumns, ignoredColumns);
		assertDataSetOk((ILabeledDataset<ILabeledInstance>) dataset.createCopy(), categoricalColumns, ignoredColumns);

		IAttribute dateAttribute = findAttribute(dataset.getInstanceSchema(), "date");
		assertNotNull(dateAttribute);
		assertTrue(dateAttribute instanceof StringAttribute);
	}

	@Test
	public void testReadDataset_MissingValues()
			throws DatasetDeserializationFailedException, DatasetCreationException, InterruptedException {
		File testCsvFile = new File("testrsc/dataset/csv/OpenML_42731_missing_values.csv");
		List<String> categoricalColumns = Arrays.asList("waterfront", "view", "condition", "grade", "zipcode");
		List<String> ignoredColumns = Arrays.asList("id");

		ILabeledDataset<ILabeledInstance> dataset = CSVDatasetAdapter.readDataset(testCsvFile.getAbsolutePath(),
				"price", categoricalColumns, ignoredColumns);

		assertDataSetOk(dataset, categoricalColumns, ignoredColumns);

		assertTrue(dataset.getInstanceSchema().getLabelAttribute() instanceof NumericAttribute);
		assertFalse(dataset.get(0).isLabelPresent());
		assertTrue(dataset.get(1).isLabelPresent());
		assertTrue(dataset.get(2).isLabelPresent());
		assertFalse(dataset.get(3).isLabelPresent());
		assertFalse(dataset.get(4).isLabelPresent());

		int index = getIndexOfColumn("bedrooms", dataset);
		assertTrue(dataset.getInstanceSchema().getAttribute(index) instanceof NumericAttribute);
		assertNull(dataset.get(0).getAttributeValue(index));

		index = getIndexOfColumn("bathrooms", dataset);
		assertTrue(dataset.getInstanceSchema().getAttribute(index) instanceof NumericAttribute);
		assertNull(dataset.get(2).getAttributeValue(index));

		index = getIndexOfColumn("sqft_living", dataset);
		assertTrue(dataset.getInstanceSchema().getAttribute(index) instanceof NumericAttribute);
		assertNull(dataset.get(4).getAttributeValue(index));

		index = getIndexOfColumn("waterfront", dataset);
		assertTrue(dataset.getInstanceSchema().getAttribute(index) instanceof IntBasedCategoricalAttribute);
		assertNull(dataset.get(0).getAttributeValue(index));

		index = getIndexOfColumn("view", dataset);
		assertTrue(dataset.getInstanceSchema().getAttribute(index) instanceof IntBasedCategoricalAttribute);
		assertNull(dataset.get(2).getAttributeValue(index));

		index = getIndexOfColumn("condition", dataset);
		assertTrue(dataset.getInstanceSchema().getAttribute(index) instanceof IntBasedCategoricalAttribute);
		assertNull(dataset.get(4).getAttributeValue(index));

		index = getIndexOfColumn("string_1", dataset);
		assertTrue(dataset.getInstanceSchema().getAttribute(index) instanceof StringAttribute);
		assertEquals("", dataset.get(0).getAttributeValue(index));
		assertEquals("foo", dataset.get(1).getAttributeValue(index));
		assertEquals("bar", dataset.get(2).getAttributeValue(index));
		assertEquals("", dataset.get(3).getAttributeValue(index));
		assertEquals("", dataset.get(4).getAttributeValue(index));
	}

	@Test
	public void testReadDataset_InvalidLines()
			throws DatasetDeserializationFailedException, DatasetCreationException, InterruptedException {
		File testCsvFile = new File("testrsc/dataset/csv/OpenML_42731_invalid_data.csv");
		List<String> categoricalColumns = Arrays.asList("waterfront", "view", "condition", "grade", "zipcode");
		List<String> ignoredColumns = Arrays.asList("id");

		ILabeledDataset<ILabeledInstance> dataset = CSVDatasetAdapter.readDataset(testCsvFile.getAbsolutePath(),
				"price", categoricalColumns, ignoredColumns);

		assertEquals(3, dataset.size());
	}

	private void assertDataSetOk(ILabeledDataset<ILabeledInstance> dataset, List<String> categoricalColumns,
			List<String> ignoredColumns) {
		assertEquals(5, dataset.size());
		ILabeledInstanceSchema schema = dataset.getInstanceSchema();

		IAttribute labelAttribute = schema.getLabelAttribute();
		assertNotNull(labelAttribute);
		assertTrue(labelAttribute instanceof NumericAttribute);

		for (String categoricalColumn : categoricalColumns) {
			IAttribute attribute = findAttribute(schema, categoricalColumn);
			assertNotNull(attribute);
			assertTrue(attribute instanceof IntBasedCategoricalAttribute);
		}
		for (String ignoredColumn : ignoredColumns) {
			IAttribute attribute = findAttribute(schema, ignoredColumn);
			assertNull(attribute);
		}
	}

	private IAttribute findAttribute(ILabeledInstanceSchema schema, String name) {
		for (IAttribute attribute : schema.getAttributeList()) {
			if (attribute.getName().equals(name)) {
				return attribute;
			}
		}
		return null;
	}

	private int getIndexOfColumn(String name, ILabeledDataset<ILabeledInstance> dataset) {
		List<IAttribute> attributes = dataset.getInstanceSchema().getAttributeList();
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

}
