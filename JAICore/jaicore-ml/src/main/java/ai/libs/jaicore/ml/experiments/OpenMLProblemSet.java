package ai.libs.jaicore.ml.experiments;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.openml.apiconnector.io.OpenmlConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.olddataset.loader.OpenMLHelper;

public class OpenMLProblemSet extends MLProblemSet {

	private static final String API_KEY = "4350e421cdc16404033ef1812ea38c01";
	private static final List<Pair<IDataset<?>, String>> INSTANCES = new ArrayList<>();
	private static final Logger logger = LoggerFactory.getLogger(OpenMLProblemSet.class);

	private final int id;
	private final Pair<IDataset<?>, String> problemPair;

	public OpenMLProblemSet(final int id) throws Exception {
		super("OpenML-" + id + " (" + new OpenmlConnector().dataGet(id).getName() + ")");
		this.id = id;
		OpenMLHelper.setApiKey(API_KEY);
		OpenmlConnector connector = new OpenmlConnector();
		this.problemPair = new Pair<>(OpenMLHelper.getDataSourceById(id), connector.dataGet(id).getDefault_target_attribute());
	}

	public int getId() {
		return this.id;
	}

	@Override
	public Pair<IDataset<?>, String> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return this.problemPair;
	}

	@Override
	public Pair<IDataset<?>, String> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return this.problemPair;
	}

	@Override
	public Pair<IDataset<?>, String> getDataset() {
		return this.problemPair;
	}
}
