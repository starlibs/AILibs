package ai.libs.automl;

import java.util.ArrayList;
import java.util.List;

import org.openml.apiconnector.io.OpenmlConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.openml.OpenMLHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class OpenMLProblemSet extends MLProblemSet {

	private static final String API_KEY = "4350e421cdc16404033ef1812ea38c01";
	private static final List<Pair<DataSource, String>> INSTANCES = new ArrayList<>();
	private static final Logger logger = LoggerFactory.getLogger(OpenMLProblemSet.class);

	private final int id;
	private final Pair<DataSource, String> problemPair;

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
	public Pair<DataSource, String> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return this.problemPair;
	}

	@Override
	public Pair<DataSource, String> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return this.problemPair;
	}

	@Override
	public Pair<DataSource, String> getDatasetSource() {
		return this.problemPair;
	}
}
