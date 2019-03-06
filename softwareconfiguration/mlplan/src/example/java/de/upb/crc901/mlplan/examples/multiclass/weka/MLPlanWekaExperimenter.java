package de.upb.crc901.mlplan.examples.multiclass.weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.events.HASCOSolutionEvent;
import jaicore.basic.SQLAdapter;
import jaicore.basic.TimeOut;
import jaicore.concurrent.TimeoutTimer;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class MLPlanWekaExperimenter implements IExperimentSetEvaluator {

	private static final String CLASSIFIER_FIELD = "classifier";
	private static final String PREPROCESSOR_FIELD = "preprocessor";
	private static final String EVALUATION_TIMEOUT_FIELD = "evaluationTimeout";

	private static final Logger L = LoggerFactory.getLogger(MLPlanWekaExperimenter.class);

	private static final File configFile = new File("conf/mlplan-weka-eval.properties");
	private final MLPlanWekaExperimenterConfig experimentConfig;
	private SQLAdapter adapter;
	private int experimentID;

	public MLPlanWekaExperimenter(final File configFile) {
		super();
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(configFile));
		} catch (IOException e) {
			L.error("Could not find or access config file {}", configFile, e);
			System.exit(1);
		}
		this.experimentConfig = ConfigFactory.create(MLPlanWekaExperimenterConfig.class, props);
		if (this.experimentConfig.getDatasetFolder() == null) {
			throw new IllegalArgumentException("No dataset folder (datasetfolder) set in config.");
		}
		if (this.experimentConfig.evaluationsTable() == null) {
			throw new IllegalArgumentException("No evaluations table (db.evalTable) set in config");
		}
	}

	@Override
	public IExperimentSetConfig getConfig() {
		return this.experimentConfig;
	}

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final SQLAdapter adapter, final IExperimentIntermediateResultProcessor processor) throws Exception {
		this.adapter = adapter;
		this.experimentID = experimentEntry.getId();
		Map<String, String> experimentValues = experimentEntry.getExperiment().getValuesOfKeyFields();

		if (!experimentValues.containsKey("dataset")) {
			throw new IllegalArgumentException("\"dataset\" is not configured as a keyword in the experiment config");
		}
		if (!experimentValues.containsKey(EVALUATION_TIMEOUT_FIELD)) {
			throw new IllegalArgumentException("\"" + EVALUATION_TIMEOUT_FIELD + "\" is not configured as a keyword in the experiment config");
		}

		File datasetFile = new File(this.experimentConfig.getDatasetFolder().getAbsolutePath() + File.separator + experimentValues.get("dataset") + ".arff");
		L.info("Load dataset file: {}", datasetFile.getAbsolutePath());

		Instances data = new Instances(new FileReader(datasetFile));
		data.setClassIndex(data.numAttributes() - 1);
		long seed = Long.parseLong(experimentValues.get("seed"));
		L.info("Split instances with seed {}", seed);
		List<Instances> stratifiedSplit = WekaUtil.getStratifiedSplit(data, seed, .7);

		/* initialize ML-Plan with the same config file that has been used to specify the experiments */
		MLPlanBuilder builder = new MLPlanBuilder();
		builder.withAutoWEKAConfiguration();
		builder.withRandomCompletionBasedBestFirstSearch();
		builder.withTimeoutForNodeEvaluation(new TimeOut(new Integer(experimentValues.get(EVALUATION_TIMEOUT_FIELD)), TimeUnit.SECONDS));
		builder.withTimeoutForSingleSolutionEvaluation(new TimeOut(new Integer(experimentValues.get(EVALUATION_TIMEOUT_FIELD)), TimeUnit.SECONDS));

		MLPlan mlplan = new MLPlan(builder, stratifiedSplit.get(0));
		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(new Integer(experimentValues.get("timeout")), TimeUnit.SECONDS);
		mlplan.setRandomSeed(new Integer(experimentValues.get("seed")));
		mlplan.setNumCPUs(experimentEntry.getExperiment().getNumCPUs());
		mlplan.registerListener(this);

		L.info("Build mlplan classifier");
		Classifier optimizedClassifier = mlplan.call();

		L.info("Open timeout tasks: {}", TimeoutTimer.getInstance());

		Evaluation eval = new Evaluation(data);
		L.info("Assess test performance...");
		eval.evaluateModel(optimizedClassifier, stratifiedSplit.get(1));

		L.info("Test error was {}. Internally estimated error for this model was {}", eval.errorRate(), mlplan.getInternalValidationErrorOfSelectedClassifier());
		Map<String, Object> results = new HashMap<>();
		results.put("loss", eval.errorRate());
		results.put(CLASSIFIER_FIELD, WekaUtil.getClassifierDescriptor(((MLPipeline) mlplan.getSelectedClassifier()).getBaseClassifier()));
		results.put(PREPROCESSOR_FIELD, ((MLPipeline) mlplan.getSelectedClassifier()).getPreprocessors().toString());

		writeFile("chosenModel." + this.experimentID + ".txt", results.get(PREPROCESSOR_FIELD) + "\n\n\n" + results.get(CLASSIFIER_FIELD));
		writeFile("result." + this.experimentID + ".txt", "intern: " + mlplan.getInternalValidationErrorOfSelectedClassifier() + "\ntest:" + results.get("loss") + "");

		processor.processResults(results);
		L.info("Experiment done.");
	}

	@Subscribe
	public void rcvHASCOSolutionEvent(final HASCOSolutionEvent<Double> e) {
		if (this.adapter != null) {
			try {
				String classifier = "";
				String preprocessor = "";
				if (e.getSolutionCandidate().getComponentInstance().getComponent().getName().equals("pipeline")) {
					preprocessor = e.getSolutionCandidate().getComponentInstance().getSatisfactionOfRequiredInterfaces().get(PREPROCESSOR_FIELD).toString();
					classifier = e.getSolutionCandidate().getComponentInstance().getSatisfactionOfRequiredInterfaces().get(CLASSIFIER_FIELD).toString();
				} else {
					classifier = e.getSolutionCandidate().getComponentInstance().toString();
				}
				Map<String, Object> eval = new HashMap<>();
				eval.put("experiment_id", this.experimentID);
				eval.put(PREPROCESSOR_FIELD, preprocessor);
				eval.put(CLASSIFIER_FIELD, classifier);
				eval.put("errorRate", e.getScore());
				eval.put("time_train", e.getSolutionCandidate().getTimeToEvaluateCandidate());
				this.adapter.insert(this.experimentConfig.evaluationsTable(), eval);
			} catch (Exception e1) {
				L.error("Could not store hasco solution in database", e1);
			}
		}
	}

	private static void writeFile(final String fileName, final String value) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)))) {
			bw.write(value);
		} catch (IOException e) {
			L.error("Could not write value to file {}: {}", fileName, value);
		}
	}

	public static void main(final String[] args) {
		/* check config */
		L.info("Start experiment runner...");
		ExperimentRunner runner = new ExperimentRunner(new MLPlanWekaExperimenter(configFile));
		L.info("Conduct random experiment...");
		runner.randomlyConductExperiments(1, false);
		L.info("Experiment conducted");
	}
}
