package jaicore.ml.experiments;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jaicore.basic.MySQLAdapter;
import weka.classifiers.Classifier;

@SuppressWarnings("serial")
public abstract class MySQLExperimentDatabaseHandle extends MySQLAdapter implements IMultiClassClassificationExperimentDatabase {
	
	private Map<Integer,Classifier> run2classifier = new HashMap<>();
	private Map<Classifier,Integer> classifier2run = new HashMap<>();

	public MySQLExperimentDatabaseHandle(String host, String user, String password, String database) {
		super(host, user, password, database);
	}
	
	@Override
	public int createRunIfDoesNotExist(Experiment e) {
		try {
			String[] values = { InetAddress.getLocalHost().toString(), e.getDataset(), e.getAlgorithm(), e.getAlgorithmMode(), String.valueOf(e.getSeed()), String.valueOf(e.getTimeoutInSeconds()), String.valueOf(e.getCpus()), String.valueOf(e.getMemoryInMB()), e.getPerformanceMeasure() };
			return insert("INSERT INTO `runs` (machine, dataset, algorithm, algorithmmode, seed, timeout, CPUs, memoryInMB, performancemeasure) VALUES (?,?,?,?,?,?,?,?,?)", values);
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
	public Collection<Experiment> getExperimentsForWhichARunExists() throws Exception {
		ResultSet rs = getResultsOfQuery("SELECT dataset, algorithm, algorithmmode, seed, timeout, cpus, memoryinmb, performancemeasure FROM `runs`");
		Collection<Experiment> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new Experiment(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getString(8)));
		}
		return list;
	}
	
	@Override
	public void updateExperiment(Experiment e, Map<String, String> data) throws Exception {
		String[] values = { data.get("rows_for_training"), e.getDataset(), e.getAlgorithm(), e.getAlgorithmMode(), String.valueOf(e.getSeed()), String.valueOf(e.getTimeoutInSeconds()), String.valueOf(e.getCpus()), String.valueOf(e.getMemoryInMB()), e.getPerformanceMeasure() };
		update("UPDATE runs SET rows_for_training = ? WHERE dataset = ? AND algorithm = ? AND algorithmmode = ? AND seed = ? AND timeout = ? AND cpus = ? AND memoryinmb = ? AND performancemeasure = ?", values);
	}
}
