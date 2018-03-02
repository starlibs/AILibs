package de.upb.crc901.mlplan.icml2018;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.upb.crc901.mlplan.core.SupervisedFilterSelector;
import de.upb.crc901.mlplan.pipeline.FeaturePreprocessor;
import de.upb.crc901.mlplan.pipeline.MLSophisticatedPipeline;
import de.upb.crc901.mlplan.pipeline.featuregen.FeatureGenerator;
import de.upb.crc901.mlplan.pipeline.featuregen.FeatureGeneratorTree;
import de.upb.crc901.mlplan.pipeline.featuregen.PCA;
import de.upb.crc901.mlplan.pipeline.featuregen.PolynomialFeatures;
import jaicore.ml.experiments.MultiClassClassificationExperimentRunner;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;

public class ICML2018Evaluation extends MultiClassClassificationExperimentRunner {

	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println(
					"Benchmark must receive 2 inputs: 1) the folder with the datasets, 2) the run id; from this, we compute the dataset, the random seed, and the algorithm id");
		}
		File folder = new File(args[0]);
		int k = Integer.parseInt(args[1]);
		ICML2018Evaluation experimentRunner = new ICML2018Evaluation(folder);
		experimentRunner.run(k);
	}

	public ICML2018Evaluation(File datasetFolder) {
		super(datasetFolder);
	}

	@Override
	protected String[] getClassifierNames() {
		return new String[] { "ML-Plan" };
	}

	@Override
	protected Classifier getConfiguredClassifier(int seed, String setup, String algo, int timeout) {
		Classifier c = null;
		switch (algo) {
		case "ML-Plan":
			try {
				List<FeatureGenerator> pl = new ArrayList<>();
				for (int i = 2; i < 4; i++) {
					PolynomialFeatures f = new PolynomialFeatures();
					f.setPotence(i);
					pl.add(f);
				}
				
				List<FeatureGenerator> fg = new ArrayList<>();
				FeatureGeneratorTree tree = new FeatureGeneratorTree(new PCA());
				FeatureGenerator probing = new MLSophisticatedPipeline(pl, new ArrayList<>(), new ArrayList<>(), new SMO());
				tree.addChild(probing);
				FeatureGenerator probing2 = new MLSophisticatedPipeline(pl, new ArrayList<>(), new ArrayList<>(), new IBk());
				tree.addChild(probing2);
				FeatureGenerator probing3 = new MLSophisticatedPipeline(pl, new ArrayList<>(), new ArrayList<>(), new NaiveBayes());
				tree.addChild(probing3);
				FeatureGenerator probing4 = new MLSophisticatedPipeline(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new AdaBoostM1());
				tree.addChild(probing4);
				FeatureGenerator probing5 = new MLSophisticatedPipeline(pl, new ArrayList<>(), new ArrayList<>(), new RandomForest());
				tree.addChild(probing5);
				FeatureGenerator probing6 = new MLSophisticatedPipeline(pl, new ArrayList<>(), new ArrayList<>(), new Logistic());
//				tree.addChild(probing6);
//				FeatureGenerator probing7 = new MLSophisticatedPipeline(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new RandomCommittee());
//				List<FeatureGenerator> probing7Pl = new ArrayList<>();
//				probing7Pl.add(probing7);
//				fg.add(probing7Pl);
				fg.add(tree);
				
//				pl = new ArrayList<>();
//				InteractingFeatures ia = new InteractingFeatures();
//				pl.add(ia);
//				pl.add(new PCA(new BestFirst()));
//				fg.add(pl);
				
				List<FeaturePreprocessor> pre = new ArrayList<>();
//				pre.add(new Normalization());
				
				List<FeaturePreprocessor> select = new ArrayList<>();
//				select.add(new SuvervisedFilterSelector(new Ranker(), new InfoGainAttributeEval()));
//				select.add(new SuvervisedFilterSelector(new Ranker(), new SymmetricalUncertAttributeEval()));
				select.add(new SupervisedFilterSelector(new BestFirst(), new CfsSubsetEval()));
				Classifier baseClassifier = AbstractClassifier.forName("weka.classifiers.functions.MultilayerPerceptron", new String[] {"-H", "a", "-N", "200"});
				return new MLSophisticatedPipeline(fg, pre, select, baseClassifier);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		default:
			break;
		}
		return c;
	}

	@Override
	protected int getNumberOfRunsPerExperiment() {
		return 25;
	}

	@Override
	protected String[] getSetupNames() {
		return new String[] { "60" };
	}

	@Override
	protected float getTrainingPortion() {
		return 0.7f;
	}

	@Override
	protected int[] getTimeouts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getNumberOfCPUS() {
		return 4;
	}
}
