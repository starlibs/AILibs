package autofe.db.sql;

import java.util.List;

import autofe.db.model.database.AbstractFeature;
import weka.core.Instances;

public interface DatabaseConnector {

	Instances getInstances(List<AbstractFeature> features) throws RetrieveInstancesFromDatabaseFailedException;

	void cleanup();

	void close();

}
