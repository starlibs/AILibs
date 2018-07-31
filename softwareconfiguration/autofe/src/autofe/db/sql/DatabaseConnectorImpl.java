package autofe.db.sql;

import java.util.List;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.ForwardFeature;
import weka.core.Instances;

public class DatabaseConnectorImpl implements DatabaseConnector{
	
	@Override
	public Instances getInstances(List<AbstractFeature> features) {
		//1. (For each feature), check whether feature table already exists, if not, create feature table
		
		//Create feature table
		
		
		// TODO Auto-generated method stub
		return null;
	}
	
	private void createFeatureTable(AbstractFeature feature) {
		
	}
	
	private void createForwardFeatureTable(ForwardFeature feature) {
		
	}
	
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

}
