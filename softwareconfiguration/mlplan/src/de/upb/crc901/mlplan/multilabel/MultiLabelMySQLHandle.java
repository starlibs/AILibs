package de.upb.crc901.mlplan.multilabel;

import jaicore.ml.experiments.MySQLExperimentDatabaseHandle;

@SuppressWarnings("serial")
public class MultiLabelMySQLHandle extends MySQLExperimentDatabaseHandle {

	public MultiLabelMySQLHandle(String host, String user, String password, String database) {
		super(host, user, password, database);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void addResultEntry(int runId, double score) throws Exception {
		throw new UnsupportedOperationException();
	}

}
