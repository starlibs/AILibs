package autofe.db.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Path;
import autofe.db.model.database.Table;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.sql.DatabaseConnector;
import autofe.db.sql.DatabaseConnectorImpl;
import autofe.db.sql.RetrieveInstancesFromDatabaseFailedException;
import autofe.db.util.DBUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class DatabaseConnectorTest {

	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	@Test
	public void testGetInstances() throws RetrieveInstancesFromDatabaseFailedException {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		Table customer = DBUtils.getTableByName("Customer", db);
		Table product = DBUtils.getTableByName("Product", db);

		// Select two features (one forward, one backward)
		List<AbstractFeature> selectedFeatures = new ArrayList<>();
		ForwardFeature firstName = new ForwardFeature(DBUtils.getAttributeByName("FirstName", customer));
		BackwardFeature price = new BackwardFeature(DBUtils.getAttributeByName("Price", product));
		Path path = price.getPath();
		path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.MAX);
		path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), AggregationFunction.AVG);
		selectedFeatures.add(firstName);
		selectedFeatures.add(price);

		// Get instances
		DatabaseConnector dbCon = new DatabaseConnectorImpl(db);
		Instances instances = dbCon.getInstances(selectedFeatures);

		// Cleanup in any case
		dbCon.cleanup();

		// Check correctness for first instance
		Instance i = instances.get(0);
		Attribute a = i.attribute(0);
		assertEquals("Alina", a.value((int) i.value(0)));
		assertEquals(15000, i.value(1), 0);
	}

}
