package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.ml.core.dataset.schema.IInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.db.DBTest;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.db.sql.DatabaseAdapterFactory;

@TestMethodOrder(OrderAnnotation.class)
public class MySQLMapperTest extends DBTest {

	private static final String TABLE = "test_relation";
	private static final String TARGET_ATTRIBUTE_NAME = "target";

	public ILabeledDataset<?> getDataset() throws DatasetDeserializationFailedException {
		return ArffDatasetAdapter.readDataset(new File("testrsc/dataset/arff/tiny.arff"));
	}

	@ParameterizedTest
	@MethodSource("getDatabaseConfigs")
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
	@MethodSource("getDatabaseConfigs")
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

	public static Stream<Arguments> getDatabaseConfigs() throws IOException {
		/* configure standard DB adapter */
		IDatabaseConfig configDefault = ConfigFactory.create(IDatabaseConfig.class);
		configDefault.setProperty(IDatabaseConfig.DB_DRIVER, "mysql");
		configDefault.setProperty(IDatabaseConfig.DB_HOST, System.getenv(VAR_DB_HOST));
		configDefault.setProperty(IDatabaseConfig.DB_USER, System.getenv(VAR_DB_USER));
		configDefault.setProperty(IDatabaseConfig.DB_PASS, System.getenv(VAR_DB_PASS));
		configDefault.setProperty(IDatabaseConfig.DB_NAME, System.getenv(VAR_DB_DATABASE));
		configDefault.setProperty(IDatabaseConfig.DB_SSL, "true");
		Objects.requireNonNull(configDefault.getDBHost(), "The host information read from environment variable " + VAR_DB_HOST + " is NULL!");
		Objects.requireNonNull(configDefault.getDBUsername(), "The user information read from environment variable " + VAR_DB_USER + " is NULL!");
		Objects.requireNonNull(configDefault.getDBPassword(), "The password read from environment variable " + VAR_DB_PASS + " is NULL!");
		Objects.requireNonNull(configDefault.getDBDatabaseName(), "The database name read from environment variable " + VAR_DB_DATABASE + " is NULL!");
		return Stream.of(Arguments.of(DatabaseAdapterFactory.get(configDefault)));
	}
}
