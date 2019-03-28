package de.upb.crc901.reduction.ensemble.simple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import de.upb.crc901.reduction.Util;
import jaicore.basic.SQLAdapter;

public class MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner {

	private static final String TABLE_NAME = "homogeneousensemblesofreductionstumps";
	private final SQLAdapter adapter;
	private final Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> knownExperiments = new HashSet<>();
//	private final static Logger logger = LoggerFactory.getLogger(MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner.class);

	public MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner(String host, String user, String password, String database) {
		adapter = new SQLAdapter(host, user, password, database);
		try {
			knownExperiments.addAll(getConductedExperiments());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> getConductedExperiments() throws SQLException {
		Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> experiments = new HashSet<>();
		ResultSet rs = adapter.getRowsOfTable(TABLE_NAME);
		while (rs.next()) {
			experiments.add(new MySQLEnsembleOfSimpleOneStepReductionsExperiment(rs.getInt("evaluation_id"), new EnsembleOfSimpleOneStepReductionsExperiment(rs.getInt("seed"), rs.getString("dataset"), rs.getString("classifier"), rs.getInt("size"), rs.getDouble("errorRate"), rs.getString("exception"))));
		}
		return experiments;
	}

	public MySQLEnsembleOfSimpleOneStepReductionsExperiment createAndGetExperimentIfNotConducted(int seed, File dataFile, String nameOfClassifier, int size) throws FileNotFoundException, IOException {
		
		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		EnsembleOfSimpleOneStepReductionsExperiment exp = new EnsembleOfSimpleOneStepReductionsExperiment(seed, dataFile.getAbsolutePath(), nameOfClassifier, size);
		Optional<MySQLEnsembleOfSimpleOneStepReductionsExperiment> existingExperiment = knownExperiments.stream().filter(e -> e.getExperiment().equals(exp)).findAny();
		if (existingExperiment.isPresent())
			return null;
		
		/* otherwise, check if the same classifier combination has been tried before */
		if (canInfeasibilityBeDerived(knownExperiments, exp))
			return null;
		
		Map<String, Object> map = new HashMap<>();
		map.put("seed", String.valueOf(seed));
		map.put("dataset", dataFile.getAbsolutePath());
		map.put("classifier", nameOfClassifier);
		map.put("size", size);
		try {
			int id = adapter.insert(TABLE_NAME, map);
			return new MySQLEnsembleOfSimpleOneStepReductionsExperiment(id, exp);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	private void updateExperiment(MySQLEnsembleOfSimpleOneStepReductionsExperiment exp, Map<String,? extends Object> values) throws SQLException {
		Map<String,String> where = new HashMap<>();
		where.put("evaluation_id", String.valueOf(exp.getId()));
		adapter.update(TABLE_NAME, values, where);
	}
	
	public void conductExperiment(MySQLEnsembleOfSimpleOneStepReductionsExperiment exp) throws Exception {
		List<Map<String,Object>> mccvResults = Util.conductEnsembleOfOneStepReductionsExperiment(exp.getExperiment());
		DescriptiveStatistics errorRate = new DescriptiveStatistics();
		DescriptiveStatistics runtime = new DescriptiveStatistics();
		for (Map<String,Object> result : mccvResults) {
			errorRate.addValue((double)result.get("errorRate"));
			runtime.addValue((long)result.get("trainTime"));
		}
		
		/* prepapre values for experiment update */
		Map<String, Object> values = new HashMap<>();
		values.put("errorRate", errorRate.getMean());
		updateExperiment(exp, values);
	}
	
	public void markExperimentAsUnsolvable(MySQLEnsembleOfSimpleOneStepReductionsExperiment exp) throws SQLException {
		Map<String, String> values = new HashMap<>();
		for (String key : new String[] {"errorRate" })
			 values.put(key, "-1");
		updateExperiment(exp, values);
	}
	
	public void associateExperimentWithException(MySQLEnsembleOfSimpleOneStepReductionsExperiment exp, String classifier, Throwable e) throws SQLException {
		Map<String, String> values = new HashMap<>();
		for (String key : new String[] {"errorRate" })
			 values.put(key, "-1");
		values.put("exception", e.getClass().getName() + "\n" + e.getMessage());
		updateExperiment(exp, values);
	}
	
	private boolean canInfeasibilityBeDerived(Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> experimentsWithResults, EnsembleOfSimpleOneStepReductionsExperiment experimentInQuestion) {
		for (MySQLEnsembleOfSimpleOneStepReductionsExperiment knownExperiment : experimentsWithResults) {
			if (!knownExperiment.getExperiment().getDataset().equals(experimentInQuestion.getDataset()))
				continue;
			EnsembleOfSimpleOneStepReductionsExperiment re = knownExperiment.getExperiment();
			if (re.getException() != null && re.getNameOfClassifier().equals(experimentInQuestion.getNameOfClassifier())) {
				System.out.println("Skipping because " + experimentInQuestion.getNameOfClassifier() + " is known to be problematic as classifier on " + re.getDataset() + " due to " + re.getException());
				return true;
			}
		}
		return false;
	}
}
