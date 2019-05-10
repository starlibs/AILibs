package de.upb.crc901.reduction.single.heterogeneous.simplerpnd;

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
import de.upb.crc901.reduction.single.MySQLReductionExperiment;
import de.upb.crc901.reduction.single.ReductionExperiment;
import jaicore.basic.SQLAdapter;

public class MySQLExperimentRunner {

	private static final String TABLE_NAME = "reductionstumps";
	private final SQLAdapter adapter;
	private final Collection<MySQLReductionExperiment> knownExperiments = new HashSet<>();

	public MySQLExperimentRunner(String host, String user, String password, String database) {
		adapter = new SQLAdapter(host, user, password, database);
		try {
			knownExperiments.addAll(getConductedExperiments());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Collection<MySQLReductionExperiment> getConductedExperiments() throws SQLException {
		Collection<MySQLReductionExperiment> experiments = new HashSet<>();
		ResultSet rs = adapter.getRowsOfTable(TABLE_NAME);
		while (rs.next()) {
			experiments.add(new MySQLReductionExperiment(rs.getInt("evaluation_id"), new ReductionExperiment(rs.getInt("seed"), rs.getString("dataset"), rs.getString("left_classifier"), rs.getString("inner_classifier"), rs.getString("right_classifier"), rs.getString("exception_left"), rs.getString("exception_inner"), rs.getString("exception_right"))));
		}
		return experiments;
	}

	public MySQLReductionExperiment createAndGetExperimentIfNotConducted(int seed, File dataFile, String nameOfLeftClassifier, String nameOfInnerClassifier,
			String nameOfRightClassifier) throws FileNotFoundException, IOException {
		
		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		ReductionExperiment exp = new ReductionExperiment(seed, dataFile.getAbsolutePath(), nameOfLeftClassifier, nameOfInnerClassifier, nameOfRightClassifier);
		Optional<MySQLReductionExperiment> existingExperiment = knownExperiments.stream().filter(e -> e.getExperiment().equals(exp)).findAny();
		if (existingExperiment.isPresent())
			return null;
		
		/* otherwise, check if the same classifier combination has been tried before */
		if (canInfeasibilityBeDerived(knownExperiments, exp))
			return null;
		
		Map<String, String> map = new HashMap<>();
		map.put("seed", String.valueOf(seed));
		map.put("dataset", dataFile.getAbsolutePath());
		map.put("rpnd_classifier", nameOfInnerClassifier);
		map.put("left_classifier", nameOfLeftClassifier);
		map.put("inner_classifier", nameOfInnerClassifier);
		map.put("right_classifier", nameOfRightClassifier);
		try {
			int id = adapter.insert(TABLE_NAME, map);
			return new MySQLReductionExperiment(id, exp);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	private void updateExperiment(MySQLReductionExperiment exp, Map<String,? extends Object> values) throws SQLException {
		Map<String,String> where = new HashMap<>();
		where.put("evaluation_id", String.valueOf(exp.getId()));
		adapter.update(TABLE_NAME, values, where);
	}
	
	public void conductExperiment(MySQLReductionExperiment exp) throws Exception {
		List<Map<String,Object>> mccvResults = Util.conductSingleOneStepReductionExperiment(exp.getExperiment());
		DescriptiveStatistics errorRate = new DescriptiveStatistics();
		DescriptiveStatistics runtime = new DescriptiveStatistics();
		for (Map<String,Object> result : mccvResults) {
			errorRate.addValue((double)result.get("errorRate"));
			runtime.addValue((long)result.get("trainTime"));
		}
		
		/* prepapre values for experiment update */
		Map<String, Object> values = new HashMap<>();
		values.put("error_rate_min", errorRate.getMin());
		values.put("error_rate_max", errorRate.getMax());
		values.put("error_rate_mean", errorRate.getMean());
		values.put("error_rate_std", errorRate.getStandardDeviation());
		values.put("runtime_min", runtime.getMin());
		values.put("runtime_max", runtime.getMax());
		values.put("runtime_mean", runtime.getMean());
		values.put("runtime_std", runtime.getStandardDeviation());
		updateExperiment(exp, values);
	}
	
	public void markExperimentAsUnsolvable(MySQLReductionExperiment exp) throws SQLException {
		Map<String, String> values = new HashMap<>();
		for (String key : new String[] {"error_rate_min", "error_rate_max", "error_rate_mean", "error_rate_std", "runtime_min", "runtime_max", "runtime_mean", "runtime_std" })
			 values.put(key, "-1");
		updateExperiment(exp, values);
	}
	
	public void associateExperimentWithException(MySQLReductionExperiment exp, String classifier, Throwable e) throws SQLException {
		Map<String, String> values = new HashMap<>();
		for (String key : new String[] {"error_rate_min", "error_rate_max", "error_rate_mean", "error_rate_std", "runtime_min", "runtime_max", "runtime_mean", "runtime_std" })
			 values.put(key, "-1");
		values.put("exception_" + classifier, e.getClass().getName() + "\n" + e.getMessage());
		updateExperiment(exp, values);
	}
	
	private boolean canInfeasibilityBeDerived(Collection<MySQLReductionExperiment> experimentsWithResults, ReductionExperiment experimentInQuestion) {
//		for (MySQLReductionExperiment knownExperiment : experimentsWithResults) {
//			if (!knownExperiment.getExperiment().getDataset().equals(experimentInQuestion.getDataset()))
//				continue;
//			ReductionExperiment re = knownExperiment.getExperiment();
//			if (re.getExceptionRPND() != null && re.getNameOfClassifierForRPNDSplit().equals(experimentInQuestion.getExceptionRPND())) {
//				System.out.println("Skipping because " + experimentInQuestion.getNameOfClassifierForRPNDSplit() + " is known to be problematic as RPND classifier on " + re.getDataset() + " due to " + re.getExceptionRPND());
//				return true;
//			}
//			else if (re.getExceptionLeft() != null && re.getNameOfLeftClassifier().equals(experimentInQuestion.getNameOfLeftClassifier())) {
//				System.out.println("Skipping because " + experimentInQuestion.getNameOfLeftClassifier() + " is known to be problematic as left classifier on " + re.getDataset() + " due to " + re.getExceptionLeft());
//				return true;
//			}
//			else if (re.getExceptionInner() != null && re.getNameOfInnerClassifier().equals(experimentInQuestion.getNameOfInnerClassifier())) {
//				System.out.println("Skipping because " + experimentInQuestion.getNameOfInnerClassifier() + " is known to be problematic as left classifier on " + re.getDataset() + " due to " + re.getExceptionInner());
//				return true;
//			}
//			else if (re.getExceptionRight() != null && re.getNameOfRightClassifier().equals(experimentInQuestion.getNameOfRightClassifier())) {
//				System.out.println("Skipping because " + experimentInQuestion.getNameOfRightClassifier() + " is known to be problematic as right classifier on " + re.getDataset() + " due to " + re.getExceptionRight());
//				return true;
//			}
//		}
		return false;
	}
}
