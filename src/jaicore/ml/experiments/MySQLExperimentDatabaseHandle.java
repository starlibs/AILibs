package jaicore.ml.experiments;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jaicore.basic.SQLAdapter;
import weka.classifiers.Classifier;

@SuppressWarnings("serial")
public class MySQLExperimentDatabaseHandle extends SQLAdapter implements IMultiClassClassificationExperimentDatabase {
	
	private Map<Integer,Classifier> run2classifier = new HashMap<>();
	private Map<Classifier,Integer> classifier2run = new HashMap<>();

	public MySQLExperimentDatabaseHandle(String host, String user, String password, String database) {
		super(host, user, password, database);
	}
	
	protected void beforeCreateRun(MLExperiment e) {}
	protected void afterCreateRun(MLExperiment e, int jobId) {}
	
	@Override
	public int createRunIfDoesNotExist(MLExperiment e) {
		try {
			String[] values = { InetAddress.getLocalHost().toString(), e.getDataset(), e.getAlgorithm(), e.getAlgorithmMode(), String.valueOf(e.getSeed()), String.valueOf(e.getTimeoutInSeconds()), String.valueOf(e.getCpus()), String.valueOf(e.getMemoryInMB()), e.getPerformanceMeasure() };
			beforeCreateRun(e);
			int id = insert("INSERT INTO `runs` (machine, dataset, algorithm, algorithmmode, seed, timeout, CPUs, memoryInMB, performancemeasure) VALUES (?,?,?,?,?,?,?,?,?)", values);
			afterCreateRun(e, id);
			return id;
		} catch (SQLException | UnknownHostException exc) {
			if (!exc.getMessage().startsWith("Duplicate entry"))
				exc.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public void associatedRunWithClassifier(int runId, Classifier c) {
		run2classifier.put(runId, c);
		classifier2run.put(c, runId);
	}
	
	public int getRunIdOfClassifier(Classifier c) {
		return classifier2run.get(c);
	}
	
	public Classifier getClassifierOfRun(int jobId) {
		return run2classifier.get(jobId);
	}
	

	@Override
	public Collection<MLExperiment> getExperimentsForWhichARunExists() throws Exception {
		ResultSet rs = getResultsOfQuery("SELECT dataset, algorithm, algorithmmode, seed, timeout, cpus, memoryinmb, performancemeasure FROM `runs`");
		Collection<MLExperiment> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new MLExperiment(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getString(8)));
		}
		return list;
	}
	
	@Override
	public void updateExperiment(MLExperiment e, Map<String, String> data) throws Exception {
		Map<String,String> whereClause = new HashMap<>();
		whereClause.put("dataset", e.getDataset());
		whereClause.put("algorithm", e.getAlgorithm());
		whereClause.put("algorithmmode", e.getAlgorithmMode());
		whereClause.put("seed", String.valueOf(e.getSeed()));
		whereClause.put("timeout", String.valueOf(e.getTimeoutInSeconds()));
		whereClause.put("cpus", String.valueOf(e.getCpus()));
		whereClause.put("memoryinmb", String.valueOf(e.getMemoryInMB()));
		whereClause.put("performancemeasure", String.valueOf(e.getPerformanceMeasure()));
		update("runs", data, whereClause);
	}

	@Override
	public void addResultEntry(int runId, double score) throws Exception {
		Map<String,String> whereClause = new HashMap<>();
		whereClause.put("run_id", String.valueOf(runId));
		Map<String,String> data = new HashMap<>();
		data.put("errorRate", String.valueOf(score));
		update("runs", data, whereClause);
	}
}
