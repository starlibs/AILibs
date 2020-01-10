package ai.libs.reduction.single.homogeneous.bestofkatrandom;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.api4.java.datastructure.kvstore.IKVStore;

import ai.libs.jaicore.db.sql.SQLAdapter;
import ai.libs.reduction.single.ABestOfKReductionStumpExperimentRunnerWrapper;
import ai.libs.reduction.single.BestOfKAtRandomExperiment;
import ai.libs.reduction.single.MySQLReductionExperiment;

public class BestOfKHomogeneousReductionStumpExperimentRunnerWrapper extends ABestOfKReductionStumpExperimentRunnerWrapper {

	private static final String KEY_CLASSIFIER = "classifier";

	private static final String TABLE_NAME = "reductionstumps_homogeneous_random_bestofk";

	private final Collection<MySQLReductionExperiment> knownExperiments = new HashSet<>();

	public BestOfKHomogeneousReductionStumpExperimentRunnerWrapper(final String host, final String user, final String password, final String database, final int k, final int mccvRepeats) throws SQLException {
		super(new SQLAdapter(host, user, password, database), TABLE_NAME, k, mccvRepeats);
		this.knownExperiments.addAll(this.getConductedExperiments());
	}

	public Collection<MySQLReductionExperiment> getConductedExperiments() throws SQLException {
		Collection<MySQLReductionExperiment> experiments = new HashSet<>();
		List<IKVStore> rslist = this.getAdapter().getRowsOfTable(TABLE_NAME);
		for (IKVStore rs : rslist) {
			experiments.add(new MySQLReductionExperiment(rs.getAsInt("evaluation_id"), new BestOfKAtRandomExperiment(rs.getAsInt("seed"), rs.getAsString("dataset"), rs.getAsString(KEY_CLASSIFIER), rs.getAsString(KEY_CLASSIFIER),
					rs.getAsString(KEY_CLASSIFIER), rs.getAsInt("k"), rs.getAsInt("mccvrepeats"))));
		}
		return experiments;
	}

	public MySQLReductionExperiment createAndGetExperimentIfNotConducted(final int seed, final File dataFile, final String nameOfClassifier) throws SQLException {
		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		BestOfKAtRandomExperiment exp = new BestOfKAtRandomExperiment(seed, dataFile.getAbsolutePath(), nameOfClassifier, nameOfClassifier, nameOfClassifier, this.getK(), this.getMCCVRepeats());
		Optional<MySQLReductionExperiment> existingExperiment = this.knownExperiments.stream().filter(e -> e.getExperiment().equals(exp)).findAny();
		if (existingExperiment.isPresent()) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();
		map.put("seed", seed);
		map.put("dataset", dataFile.getAbsolutePath());
		map.put(KEY_CLASSIFIER, nameOfClassifier);
		map.put("k", this.getK());
		map.put("mccvrepeats", this.getMCCVRepeats());
		int id = this.getAdapter().insert(TABLE_NAME, map)[0];
		return new MySQLReductionExperiment(id, exp);
	}

}
