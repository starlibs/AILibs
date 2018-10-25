package de.upb.crc901.mlplan.multiclass.wekamlplan.scikitlearn;
//package de.upb.crc901.mlplan.multiclass.scikitlearn;
//
//import java.io.IOException;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Random;
//
//import org.aeonbits.owner.ConfigCache;
//
//import de.upb.crc901.automl.hascoscikitlearnml.ScikitLearnBenchmark;
//import de.upb.crc901.automl.hascoscikitlearnml.ScikitLearnComposition;
//import de.upb.crc901.automl.hascoscikitlearnml.ScikitLearnCompositionFactory;
//import hasco.serialization.ComponentLoader;
//import hasco.variants.twophase.TwoPhaseHASCO;
//import jaicore.ml.WekaUtil;
//import jaicore.ml.evaluation.IInstancesClassifier;
//import weka.classifiers.Classifier;
//import weka.core.Capabilities;
//import weka.core.Instance;
//import weka.core.Instances;
//
//public class MLPlanScikitLearnClassifier extends TwoPhaseHASCO implements Classifier, IInstancesClassifier {
//
//	private static final MLPlanScikitLearnClassifierConfig CONFIG = ConfigCache.getOrCreate(MLPlanScikitLearnClassifierConfig.class);
//
//	private Instances trainingData;
//
//	public MLPlanScikitLearnClassifier() throws IOException {
//		super(new ComponentLoader());
//	}
//
//	@Override
//	public void buildClassifier(final Instances data) throws Exception {
//		this.trainingData = data;
//
//		List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(data, new Random(this.getConfig().randomSeed()), this.getConfig().selectionDataPortion());
//
//		this.setBenchmark(new ScikitLearnBenchmark(selectionSplit.get(1), this.getConfig().searchMCIterations(), this.getConfig().searchDataPortion(), -1, this.getConfig().searchMCIterations() * 2, "search_", null, null));
//
//		this.setSelectionPhaseEvaluator(new ScikitLearnBenchmark(data, this.getConfig().selectionMCIterations(), (1 - this.getConfig().selectionDataPortion()), -1, this.getConfig().selectionMCIterations() * 2, "sel_", null, null));
//
//		this.setFactory(new ScikitLearnCompositionFactory());
//
//		super.gatherSolutions(CONFIG.timeout());
//	}
//
//	@Override
//	public double classifyInstance(final Instance instance) throws Exception {
//		Instances test = new Instances(this.trainingData, 0);
//		test.add(instance);
//		return 0;
//	}
//
//	@Override
//	public double[] distributionForInstance(final Instance instance) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Capabilities getCapabilities() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public MLPlanScikitLearnClassifierConfig getConfig() {
//		return CONFIG;
//	}
//
//	@Override
//	public double[] classifyInstances(final Instances instances) throws Exception {
//		List<Instances> trainTestData = new LinkedList<>();
//		trainTestData.add(this.trainingData);
//		trainTestData.add(instances);
//		ScikitLearnBenchmark benchmark = new ScikitLearnBenchmark(trainTestData, "test_", null, null);
//		benchmark.evaluateFixedSplit((ScikitLearnComposition) super.getSelectedClassifier());
//		return null;
//	}
//
//}
