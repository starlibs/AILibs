package defaultEval.core.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;

import de.upb.crc901.automl.hascowekaml.WEKAPipelineFactory;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import defaultEval.core.DefaultOptimizer;
import defaultEval.core.HyperbandOptimizer;
import defaultEval.core.Optimizer;
import defaultEval.core.SMACOptimizer;
import defaultEval.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class DefaultEvalExperiment {
	
	// usable Components
	private static ComponentLoader classifierComponents = new ComponentLoader();
	private static ComponentLoader preProcessorComponents = new ComponentLoader();
		
		
	public static void main(String[] args) {
		DefaultEvalMCCConfig m = ConfigCache.getOrCreate(DefaultEvalMCCConfig.class);
		if (m.getDatasetFolder() == null || !m.getDatasetFolder().exists())
			throw new IllegalArgumentException("config specifies invalid dataset folder " + m.getDatasetFolder());

		try {
			Util.loadClassifierComponents(classifierComponents, m.getEnvironment().getAbsolutePath());
			Util.loadPreprocessorComponents(preProcessorComponents, m.getEnvironment().getAbsolutePath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		ExperimentRunner runner = new ExperimentRunner(new IExperimentSetEvaluator() {

			@Override
			public IExperimentSetConfig getConfig() {
				return m;
			}

			@Override
			public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
					IExperimentIntermediateResultProcessor processor) throws Exception {

				/* get experiment setup */
				Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
				String classifierName = description.get("classifier");
				String preprocessorName = description.get("preprocessor");
				String optimizerName = description.get("optimizer");
				String datasetName = description.get("dataset");
				int seed = Integer.valueOf(description.get("seed"));
				
				Map<String, Object> results = new HashMap<>();
				Optimizer optimizer = null;
				
				String searcherName = (preprocessorName.equals("null")) ? "null" : preprocessorName.split(";")[0];
				String evaluatorName = (preprocessorName.equals("null")) ? "null" : preprocessorName.split(";")[1];
				
				Component classifier = classifierComponents.getComponents().stream().filter(e -> e.getName().equals(classifierName)).findAny().get();
				Component searcher = null;
				Component evaluator = null;
				if(!preprocessorName.equals("null")) {
					searcher = preProcessorComponents.getComponents().stream().filter(e -> e.getName().equals(searcherName)).findAny().get();
					evaluator = preProcessorComponents.getComponents().stream().filter(e -> e.getName().equals(evaluatorName)).findAny().get();	
				}
				
				switch (optimizerName) {
				case "SMAC":
					optimizer = new SMACOptimizer(searcher, evaluator, classifier, datasetName, m.getEnvironment(), m.getDatasetFolder(), seed, m.getMaxRuntimeParam(), m.getMaxRuntime());
					break;
					
				case "Hyperband":
					optimizer = new HyperbandOptimizer(searcher, evaluator, classifier, datasetName, m.getEnvironment(), m.getDatasetFolder(), seed, m.getMaxRuntimeParam(), m.getMaxRuntime());
					break;
				
				case "DGGA":
					
				case "default":
					optimizer = new DefaultOptimizer(searcher, evaluator, classifier, datasetName, m.getEnvironment(), m.getDatasetFolder(), seed, m.getMaxRuntimeParam(), m.getMaxRuntime());
					break;
					
				default:
					break;
				}
				
				optimizer.optimize();
				
				WEKAPipelineFactory factory = new WEKAPipelineFactory();
				Classifier wekaClassifier = factory.getComponentInstantiation(Util.createPipeline(optimizer.getFinalSearcher(), optimizer.getFinalEvaluator(), optimizer.getFinalClassifier()));
				Instances instances = Util.loadInstances(m.getDatasetFolder().getAbsolutePath(), datasetName);
				
				List<Instances> instancesList = WekaUtil.getStratifiedSplit(instances, new Random(seed), 0.7, 0.3);
				
				Evaluation evaluation = new Evaluation(instancesList.get(0));
				wekaClassifier.buildClassifier(instancesList.get(0));
				evaluation.evaluateModel(wekaClassifier, instancesList.get(1));
				
				/* report results */
				results.put("pctIncorrect", evaluation.pctIncorrect());
				results.put("searcher_parameters", optimizer.getFinalSearcher().getParameterValues());
				results.put("evaluator_parameters", optimizer.getFinalEvaluator().getParameterValues());
				results.put("classifier_parameters", optimizer.getFinalClassifier().getParameterValues());
				results.put("timeout_output", m.getMaxRuntimeParam());
				
				processor.processResults(results);
			}
		});
		runner.randomlyConductExperiments(true);
	}

}
