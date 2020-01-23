package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.ml.core.dataset.schema.IInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.junit.Test;

import ai.libs.jaicore.db.DBTester;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.sql.rest.IRestDatabaseConfig;
import ai.libs.jaicore.db.sql.rest.RestSqlAdapter;

public class MySQLMapperTest extends DBTester {

	private static final String TABLE = "test_relation";
	private static final String TARGET_ATTRIBUTE_NAME = "target";

	@Test
	public void testReadMapping() throws SQLException, IOException {
		IRestDatabaseConfig config = ConfigFactory.create(IRestDatabaseConfig.class);
		setConnectionConfigIfEmpty(config);
		IDatabaseAdapter adapter = new RestSqlAdapter(config);
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
		ILabeledDataset<?> ds = mapper.readDatasetFromTable(TABLE, TARGET_ATTRIBUTE_NAME);
		assertEquals(3, ds.getNumAttributes()); // target does not count
		assertEquals(2, ds.size());
		assertEquals(0.1, ds.get(0).getLabel());
		assertEquals(0.9, ds.get(1).getLabel());
	}
}
