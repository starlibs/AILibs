package ai.libs.reduction.single.heterogeneous.bestofkrandom;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.db.sql.DatabaseAdapterFactory;
import ai.libs.reduction.single.ABestOfKReductionStumpExperimentRunnerWrapper;
import ai.libs.reduction.single.BestOfKAtRandomExperiment;
import ai.libs.reduction.single.MySQLReductionExperiment;

public class BestOfKHeterogeneousReductionStumpExperimentRunnerWrapper extends ABestOfKReductionStumpExperimentRunnerWrapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(BestOfKHeterogeneousReductionStumpExperimentRunnerWrapper.class);

	private static final String TABLE_NAME = "reductionstumps_heterogeneous_random_bestofk";

	private final Collection<MySQLReductionExperiment> knownExperiments = new HashSet<>();

	public BestOfKHeterogeneousReductionStumpExperimentRunnerWrapper(final IDatabaseConfig config, final int k, final int mccvRepeats) {
		super(DatabaseAdapterFactory.get(config), TABLE_NAME, k, mccvRepeats);
		try {
			this.knownExperiments.addAll(this.getConductedExperiments());
		} catch (SQLException e) {
			LOGGER.error("Could not get the already conducted experiments from the database.", e);
		}
	}

	public Collection<MySQLReductionExperiment> getConductedExperiments() throws SQLException {
		Collection<MySQLReductionExperiment> experiments = new HashSet<>();
		List<IKVStore> rslist = this.getAdapter().getRowsOfTable(TABLE_NAME);
		for (IKVStore rs : rslist) {
			experiments.add(new MySQLReductionExperiment(rs.getAsInt("evaluation_id"), new BestOfKAtRandomExperiment(rs.getAsInt("seed"), rs.getAsString("dataset"), rs.getAsString("left_classifier"), rs.getAsString("inner_classifier"),
					rs.getAsString("right_classifier"), rs.getAsInt("k"), rs.getAsInt("mccvrepeats"))));
		}
		return experiments;
	}

	public MySQLReductionExperiment createAndGetExperimentIfNotConducted(final int seed, final File dataFile, final String nameOfLeftClassifier, final String nameOfInnerClassifier, final String nameOfRightClassifier) {
		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		BestOfKAtRandomExperiment exp = new BestOfKAtRandomExperiment(seed, dataFile.getAbsolutePath(), nameOfLeftClassifier, nameOfInnerClassifier, nameOfRightClassifier, this.getK(), this.getMCCVRepeats());
		Optional<MySQLReductionExperiment> existingExperiment = this.knownExperiments.stream().filter(e -> e.getExperiment().equals(exp)).findAny();
		if (existingExperiment.isPresent()) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();
		map.put("seed", seed);
		map.put("dataset", dataFile.getAbsolutePath());
		map.put("left_classifier", nameOfLeftClassifier);
		map.put("inner_classifier", nameOfInnerClassifier);
		map.put("right_classifier", nameOfRightClassifier);
		map.put("k", this.getK());
		map.put("mccvrepeats", this.getMCCVRepeats());
		try {
			int id = this.getAdapter().insert(TABLE_NAME, map)[0];
			return new MySQLReductionExperiment(id, exp);
		} catch (SQLException e) {
			LOGGER.error("Could not create experiment entry", e);
			return null;
		}
	}

}
