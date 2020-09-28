package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.api4.java.ai.ml.core.dataset.schema.IInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.db.DBTester;
import ai.libs.jaicore.db.IDatabaseAdapter;

@TestMethodOrder(OrderAnnotation.class)
public class MySQLMapperTest extends DBTester {

	private static final String TABLE = "test_relation";
	private static final String TARGET_ATTRIBUTE_NAME = "target";

	public ILabeledDataset<?> getDataset() throws DatasetDeserializationFailedException {
		return ArffDatasetAdapter.readDataset(new File("testrsc/dataset/arff/tiny.arff"));
	}

	@ParameterizedTest
	@MethodSource("getDatabaseAdapters")
	@Order(1)
	public void testWriteMapping(final IDatabaseAdapter adapter) throws SQLException, IOException, DatasetDeserializationFailedException {
		ILabeledDataset<?> ds = this.getDataset();
		assertEquals(2, ds.size());

		assertFalse(adapter.doesTableExist(TABLE));
		MySQLDatasetMapper mapper = new MySQLDatasetMapper(adapter);
		mapper.writeDatasetToDatabase(ds, TABLE);
		assertTrue(adapter.doesTableExist(TABLE));
		assertEquals(2, adapter.getRowsOfTable(TABLE).size());
	}

	@ParameterizedTest
	@MethodSource("getDatabaseAdapters")
	@Order(2)
	public void testReadMapping(final IDatabaseAdapter adapter) throws SQLException, IOException, DatasetDeserializationFailedException {
		ILabeledDataset<?> ds = this.getDataset();
		MySQLDatasetMapper mapper = new MySQLDatasetMapper(adapter);

		/* first test unlabeled scheme */
		IInstanceSchema scheme = mapper.getInstanceSchemaOfTable(TABLE);
		assertEquals(4, scheme.getNumAttributes());

		/* test labeled scheme */
		ILabeledInstanceSchema labeledScheme = mapper.getInstanceSchemaOfTable(TABLE, TARGET_ATTRIBUTE_NAME);
		assertEquals(3, labeledScheme.getNumAttributes());
		assertEquals(TARGET_ATTRIBUTE_NAME, labeledScheme.getLabelAttribute().getName());
		assertTrue("Label attribute " + TARGET_ATTRIBUTE_NAME + " should be numeric but is " + labeledScheme.getLabelAttribute().getClass().getName(), labeledScheme.getLabelAttribute() instanceof INumericAttribute);

		/* test dataset */
		ILabeledDataset<?> dsReadFromDB = mapper.readDatasetFromTable(TABLE, TARGET_ATTRIBUTE_NAME);
		assertEquals(ds.getNumAttributes(), dsReadFromDB.getNumAttributes()); // target does not count
		assertEquals(ds.size(), dsReadFromDB.size());
		assertEquals(Double.valueOf(ds.get(0).getLabel().toString()), Double.valueOf(dsReadFromDB.get(0).getLabel().toString()), 0.0);
		assertEquals(Double.valueOf(ds.get(1).getLabel().toString()), Double.valueOf(dsReadFromDB.get(1).getLabel().toString()), 0.0);

		/* delete table */
		adapter.update("DROP TABLE " + TABLE);
	}
}
