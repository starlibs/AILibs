package de.upb.crc901.mlplan.examples.multiclass.weka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
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

	private static final File configFile = new File("conf/mlplan-weka-eval.properties");
	private final MLPlanWekaExperimenterConfig experimentConfig;
	private SQLAdapter adapter;
	private int experimentID;
	private final WEKAPipelineFactory factory = new WEKAPipelineFactory();

	public MLPlanWekaExperimenter(final File configFile) {
		super();
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(configFile));
		} catch (IOException e) {
			System.err.println("Could not find or access config file " + configFile);
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
		if (!experimentValues.containsKey("evaluationTimeout")) {
			throw new IllegalArgumentException("\"evaluationTimeout\" is not configured as a keyword in the experiment config");
		}

		File datasetFile = new File(this.experimentConfig.getDatasetFolder().getAbsolutePath() + File.separator + experimentValues.get("dataset") + ".arff");
		print("Load dataset file: " + datasetFile.getAbsolutePath());

		Instances data = new Instances(new FileReader(datasetFile));
		data.setClassIndex(data.numAttributes() - 1);
		print("Split instances");
		List<Instances> stratifiedSplit = WekaUtil.getStratifiedSplit(data, 0, .7);

		/* initialize ML-Plan with the same config file that has been used to specify the experiments */
		MLPlanBuilder builder = new MLPlanBuilder();
		builder.withAlgorithmConfigFile(configFile);
		builder.withAutoWEKAConfiguration();
		builder.withTimeoutForNodeEvaluation(new TimeOut(new Integer(experimentValues.get("evaluationTimeout")), TimeUnit.SECONDS));
		builder.withTimeoutForSingleSolutionEvaluation(new TimeOut(new Integer(experimentValues.get("evaluationTimeout")), TimeUnit.SECONDS));

		MLPlan mlplan = new MLPlan(builder, stratifiedSplit.get(0));
		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(new Integer(experimentValues.get("timeout")), TimeUnit.SECONDS);
		mlplan.setRandomSeed(new Integer(experimentValues.get("seed")));
		mlplan.setNumCPUs(experimentEntry.getExperiment().getNumCPUs());
		mlplan.registerListener(this);

		print("Build mlplan classifier");
		Classifier optimizedClassifier = mlplan.call();

		print("Open timeout tasks: " + TimeoutTimer.getInstance().toString());

		Evaluation eval = new Evaluation(data);
		print("Assess test performance...");
		eval.evaluateModel(optimizedClassifier, stratifiedSplit.get(1), new Object[] {});

		print("Test error was " + eval.errorRate() + ". Internally estimated error for this model was " + mlplan.getInternalValidationErrorOfSelectedClassifier());
		Map<String, Object> results = new HashMap<>();
		results.put("loss", eval.errorRate());
		results.put("classifier", WekaUtil.getClassifierDescriptor(((MLPipeline) mlplan.getSelectedClassifier()).getBaseClassifier()));
		results.put("preprocessor", ((MLPipeline) mlplan.getSelectedClassifier()).getPreprocessors().toString());

		processor.processResults(results);
		print("Experiment done.");
	}

	@Subscribe
	public void rcvHASCOSolutionEvent(final HASCOSolutionEvent<Double> e) {
		if (this.adapter != null) {
			try {
				String classifier = "";
				String preprocessor = "";
				if (e.getSolutionCandidate().getComponentInstance().getComponent().equals("pipeline")) {
					preprocessor = e.getSolutionCandidate().getComponentInstance().getSatisfactionOfRequiredInterfaces().get("preprocessor").toString();
					classifier = e.getSolutionCandidate().getComponentInstance().getSatisfactionOfRequiredInterfaces().get("classifier").toString();
				} else {
					classifier = e.getSolutionCandidate().getComponentInstance().toString();
				}
				Map<String, Object> eval = new HashMap<>();
				eval.put("experiment_id", this.experimentID);
				eval.put("preprocessor", preprocessor);
				eval.put("classifier", classifier);
				eval.put("errorRate", e.getScore());
				eval.put("time_train", e.getSolutionCandidate().getTimeToEvaluateCandidate());
				this.adapter.insert(this.experimentConfig.evaluationsTable(), eval);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void print(final String message) {
		System.out.println(new Time(System.currentTimeMillis()).toString() + ": " + message);
	}

	public static void main(final String[] args) {

		/* check config */
		print("Start experiment runner...");
		ExperimentRunner runner = new ExperimentRunner(new MLPlanWekaExperimenter(configFile));
		print("Conduct random experiment...");
		runner.randomlyConductExperiments(1, false);
		print("Experiment conducted, stop timeout timer.");
		TimeoutTimer.getInstance().stop();
		print("Timer stopped.");
	}
}
