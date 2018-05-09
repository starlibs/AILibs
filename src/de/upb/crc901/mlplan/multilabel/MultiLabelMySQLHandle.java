package de.upb.crc901.mlplan.multilabel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jaicore.ml.experiments.MySQLExperimentDatabaseHandle;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.OptionHandler;

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
