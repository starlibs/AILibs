package ai.libs.mlplan.cli.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.regression.learner.WekaRegressor;
import weka.classifiers.trees.RandomForest;

public class StatisticsReportTest {

	private static final ResourceFile resFile = new ResourceFile("automl/searchmodels/weka/weka-full.json");
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String LEARNER = "weka.classifiers.trees.RandomForest";

	private static IComponentRepository compRepo;
	private static ComponentInstance ci;

	private static final File BASE_DIR = new File("testrsc/report/statistics-report/");
	private static final File EXP_BINARY = new File(BASE_DIR, "binary.txt");
	private static final File EXP_MULTINOMIAL = new File(BASE_DIR, "multinomial.txt");
	private static final File EXP_REGRESSION = new File(BASE_DIR, "regression.txt");

	@BeforeAll
	public static void setup() throws IOException {
		compRepo = new ComponentSerialization().deserializeRepository(resFile);
		ci = ComponentUtil.getDefaultParameterizationOfComponent(compRepo.getComponent(LEARNER));
	}

	@Test
	public void testBinaryClassification() throws LearnerExecutionFailedException, SplitFailedException, InterruptedException, DatasetDeserializationFailedException, IOException {
		this.testReportOutput(EXP_BINARY, 31, new WekaClassifier(new RandomForest()));
	}

	@Test
	public void testMultinomialClassification() throws LearnerExecutionFailedException, SplitFailedException, InterruptedException, DatasetDeserializationFailedException, IOException {
		this.testReportOutput(EXP_MULTINOMIAL, 307, new WekaClassifier(new RandomForest()));
	}

	@Test
	public void testRegression() throws LearnerExecutionFailedException, SplitFailedException, InterruptedException, DatasetDeserializationFailedException, IOException {
		this.testReportOutput(EXP_REGRESSION, 42364, new WekaRegressor(new RandomForest()));
	}

	private void testReportOutput(final File expectedOutput, final int datasetID, final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner)
			throws IOException, SplitFailedException, InterruptedException, DatasetDeserializationFailedException, LearnerExecutionFailedException {
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(new OpenMLDatasetReader().deserializeDataset(datasetID), 0, .7);
		ILearnerRunReport runReport = new SupervisedLearnerExecutor().execute(learner, split.get(0), split.get(1));
		StatisticsReport analysisReport = new StatisticsReport(new StatisticsListener(), ci, runReport);
		ObjectNode expected = (ObjectNode) mapper.readTree(expectedOutput);
		JsonNode actual = mapper.readTree(analysisReport.toString());

		Iterator<Entry<String, JsonNode>> fieldsIt = expected.fields();
		while (fieldsIt.hasNext()) {
			Entry<String, JsonNode> field = fieldsIt.next();
			assertTrue(actual.has(field.getKey()), "Statistics report is missing field with name " + field.getKey());
		}
	}

}
