package de.upb.crc901.mlplan.examples.multiclass.weka;

import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import de.upb.crc901.mlplan.multiclass.weka.MLPlanWekaClassifier;
import hasco.events.HASCOSolutionEvent;
import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.WekaUtil;
import jaicore.planning.EvaluatedSearchGraphBasedPlan;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class MLPlanWekaExperimenter implements IExperimentSetEvaluator {

	public static final MLPlanWekaExperimenterConfig CONFIG = ConfigCache.getOrCreate(MLPlanWekaExperimenterConfig.class);
	private SQLAdapter adapter;
	private int experimentID;

	@Override
	public IExperimentSetConfig getConfig() {
		return CONFIG;
	}

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final SQLAdapter adapter, final IExperimentIntermediateResultProcessor processor) throws Exception {
		this.adapter = adapter;
		this.experimentID = experimentEntry.getId();
		Map<String, String> experimentValues = experimentEntry.getExperiment().getValuesOfKeyFields();

		File datasetFile = new File(CONFIG.getDatasetFolder().getAbsolutePath() + File.separator + experimentValues.get("dataset") + ".arff");
		System.out.println(getTime() + ": Load dataset file: " + datasetFile.getAbsolutePath());

		Instances data = new Instances(new FileReader(datasetFile));
		data.setClassIndex(data.numAttributes() - 1);

		List<Instances> stratifiedSplit = WekaUtil.getStratifiedSplit(data, new Random(), .7);

		MLPlanWekaClassifier mlplan = new MLPlanWekaClassifier();
		mlplan.setTimeout(new Integer(experimentValues.get("timeout")));
		mlplan.setTimeoutForSingleFEvaluation(new Integer(experimentValues.get("evaluationTimeout")));
		mlplan.setRandom(new Integer(experimentValues.get("seed")));
		mlplan.setNumberOfCPUs(experimentEntry.getExperiment().getNumCPUs());
		mlplan.registerListenerForSolutionEvaluations(this);

		System.out.println(getTime() + ": Build mlplan classifier");
		mlplan.buildClassifier(stratifiedSplit.get(0));

		Evaluation eval = new Evaluation(data);
		System.out.println(getTime() + ": Assess test performance...");
		eval.evaluateModel(mlplan, stratifiedSplit.get(1), new Object[] {});

		System.out.println(getTime() + ": Test error was " + eval.errorRate());
		Map<String, Object> results = new HashMap<>();
		results.put("loss", eval.errorRate());
		results.put("classifier", WekaUtil.getClassifierDescriptor(((MLPipeline) mlplan.getSelectedClassifier()).getBaseClassifier()));
		results.put("preprocessor", ((MLPipeline) mlplan.getSelectedClassifier()).getPreprocessors().toString());

		processor.processResults(results);
		System.out.println(getTime() + ": Experiment done.");
	}

	@Subscribe
	public void rcvHASCOSolutionEvent(final HASCOSolutionEvent<EvaluatedSearchGraphBasedPlan, MLPipeline, Double> e) {
		if (this.adapter != null) {
			Map<String, Object> eval = new HashMap<>();
			eval.put("run_id", this.experimentID);
			eval.put("preprocessor", e.getSolution().getSolution().getPreprocessors().toString());
			eval.put("classifier", WekaUtil.getClassifierDescriptor(e.getSolution().getSolution().getBaseClassifier()));
			eval.put("errorRate", e.getSolution().getScore());
			eval.put("time_train", e.getSolution().getTimeToComputeScore());
			eval.put("time_predict", -1);
			try {
				this.adapter.insert("evaluations_mls", eval);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static String getTime() {
		return new Time(System.currentTimeMillis()).toString();
	}

	public static void main(final String[] args) {
		ExperimentRunner runner = new ExperimentRunner(new MLPlanWekaExperimenter());
		runner.randomlyConductExperiments(1, false);
	}
}
