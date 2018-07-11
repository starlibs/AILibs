package autofe.db.sql;

import weka.core.Instances;

public interface DatabaseConnector {
	
	public void prepareDatabase();
	
	public void applyOperations();
	
	public Instances getInstances();
	
	public void cleanup();

}
