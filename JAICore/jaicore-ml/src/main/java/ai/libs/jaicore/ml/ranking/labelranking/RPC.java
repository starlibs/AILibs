package ai.libs.jaicore.ml.ranking.labelranking;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.ml.IRanking;
import org.api4.java.ai.ml.dataset.supervised.ranking.INumericFeatureRankingDataset;
import org.api4.java.ai.ml.dataset.supervised.ranking.INumericFeatureRankingInstance;
import org.api4.java.ai.ml.learner.fit.TrainingException;
import org.api4.java.ai.ml.learner.predict.IPrediction;
import org.api4.java.ai.ml.learner.predict.PredictionException;
import org.api4.java.ai.ml.learner.ranker.IRanker;

import ai.libs.jaicore.basic.Maps;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.classification.ASupervisedLearner;
import ai.libs.jaicore.ml.dataset.Prediction;
import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;

public class RPC extends ASupervisedLearner<RPCConfig, Double, IRanking<String>, INumericFeatureRankingInstance<String>, INumericFeatureRankingDataset<String>>
		implements IRanker<RPCConfig, Double, String, INumericFeatureRankingInstance<String>, INumericFeatureRankingDataset<String>> {

	private RPCConfig config;
	private Instances dataset;
	private int labels;

	private Instances plainPWDataset = null;
	private List<Integer> labelIndices;
	private Set<String> labelSet = new HashSet<>();

	class PairWiseClassifier {
		String a;
		String b;
		Classifier c;
	}

	List<PairWiseClassifier> pwClassifiers = new LinkedList<>();

	public RPC(final RPCConfig config, final Instances dataset, final int labels) throws Exception {
		this.config = config;
		this.dataset = dataset;
		this.labels = labels;
		this.labelIndices = getLabelIndices(labels, dataset);
		this.labelIndices.stream().map(x -> dataset.attribute(x).name()).forEach(this.labelSet::add);
		this.plainPWDataset = this.applyFiltersToDataset(dataset);
	}

	private Instances applyFiltersToDataset(final Instances dataset) throws Exception {
		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(this.labelIndices.stream().mapToInt(x -> x).toArray());
		removeFilter.setInvertSelection(false);
		removeFilter.setInputFormat(dataset);
		Instances filteredDataset = Filter.useFilter(dataset, removeFilter);

		Add addTarget = new Add();
		addTarget.setAttributeIndex("last");
		addTarget.setNominalLabels("true,false");
		addTarget.setAttributeName("a>b");
		addTarget.setInputFormat(filteredDataset);
		filteredDataset = Filter.useFilter(filteredDataset, addTarget);
		filteredDataset.setClassIndex(filteredDataset.numAttributes() - 1);
		return filteredDataset;
	}

	public static void main(final String[] args) throws Exception {
		Instances dataset = new Instances(new FileReader("classifier-rank.arff"));

		int labels = -22;
		int folds = 10;
		double[] portions = new double[folds - 1];
		for (int i = 0; i < folds - 1; i++) {
			portions[i] = 1.0 / folds;
		}

		List<Instances> split = WekaUtil.realizeSplit(dataset, WekaUtil.getArbitrarySplit(dataset, new Random(0), portions));

		DescriptiveStatistics overallKendall = new DescriptiveStatistics();

		for (int i = 0; i < split.size(); i++) {
			Instances trainData = new Instances(dataset, 0);
			for (int j = 0; j < split.size(); j++) {
				if (i == j) {
					continue;
				}
				trainData.addAll(split.get(j));
			}

			RPC rpc = new RPC(ConfigFactory.create(RPCConfig.class), trainData, labels);
			rpc.fit();

			DescriptiveStatistics stats = new DescriptiveStatistics();

			for (Instance instance : split.get(i)) {
				List<String> groundTruthRanking = getGroundTruthRanking(instance, labels);

				Ranking<String> predictedRanking = rpc.getRanking(instance);
				double tau = kendallTau(groundTruthRanking, predictedRanking);
				stats.addValue(tau);

			}
			overallKendall.addValue(stats.getMean());

			System.out.println("Test Fold: " + i);
			System.out.println(stats);
		}

		System.out.println("Final Result: ");
		System.out.println(overallKendall);

	}

	private static double kendallTau(final List<String> groundTruth, final List<String> predicted) {
		double[] xArray = new double[groundTruth.size()];
		for (int i = 0; i < xArray.length; i++) {
			xArray[i] = i + 1;
		}

		double[] yArray = new double[predicted.size()];
		for (int i = 0; i < yArray.length; i++) {
			yArray[i] = predicted.indexOf(groundTruth.get(i)) + 1;
		}

		KendallsCorrelation kendall = new KendallsCorrelation();
		return kendall.correlation(xArray, yArray);
	}

	private static List<Integer> getLabelIndices(final int labels, final Instances dataset) {
		List<Integer> labelIndices = new LinkedList<>();
		if (labels < 0) {
			for (int i = dataset.numAttributes() - 1; i >= dataset.numAttributes() + labels; i--) {
				labelIndices.add(i);
			}
		} else {
			for (int i = 0; i < labels; i++) {
				labelIndices.add(i);
			}
		}
		return labelIndices;
	}

	private static List<String> getGroundTruthRanking(final Instance i, final int labels) {
		List<Integer> labelIndices = getLabelIndices(labels, i.dataset());

		List<String> labelList = new LinkedList<>();
		for (Integer index : labelIndices) {
			labelList.add(i.dataset().attribute(index).name());
		}
		List<String> labelListToSort = new LinkedList<>(labelList);
		labelListToSort.sort((arg0, arg1) -> Double.valueOf(i.value(labelIndices.get(labelList.indexOf(arg1)))).compareTo(i.value(labelIndices.get(labelList.indexOf(arg0)))));

		return labelListToSort;
	}

	@Override
	public void fit(final INumericFeatureRankingDataset<String> dTrain) throws TrainingException, InterruptedException {
		try {
			for (int i = 0; i < this.labelIndices.size() - 1; i++) {
				for (int j = i + 1; j < this.labelIndices.size(); j++) {

					PairWiseClassifier pwc = new PairWiseClassifier();
					pwc.a = this.dataset.attribute(this.labelIndices.get(i)).name();
					pwc.b = this.dataset.attribute(this.labelIndices.get(j)).name();

					pwc.c = AbstractClassifier.forName(this.config.getBaseLearner(), null);

					Instances pwDataset = new Instances(this.plainPWDataset);

					for (int k = 0; k < pwDataset.size(); k++) {
						String value;
						if (this.dataset.get(k).value(this.labelIndices.get(i)) > this.dataset.get(k).value(this.labelIndices.get(j))) {
							value = "true";
						} else {
							value = "false";
						}
						pwDataset.get(k).setValue(pwDataset.numAttributes() - 1, value);
					}
					pwDataset.setClassIndex(pwDataset.numAttributes() - 1);

					pwc.c.buildClassifier(pwDataset);
					this.pwClassifiers.add(pwc);
				}
			}
		} catch (Exception e) {
			throw new TrainingException("Could not build ranker", e);
		}
	}

	@Override
	public IPrediction<IRanking<String>> predict(final INumericFeatureRankingInstance<String> xTest) throws PredictionException, InterruptedException {
		Instances datasetCopy = new Instances(problem.dataset(), 0);
		datasetCopy.add(problem);

		try {
			datasetCopy = this.applyFiltersToDataset(datasetCopy);

			Map<String, Double> vote = new HashMap<>();
			this.labelSet.stream().forEach(x -> vote.put(x, 0.0));

			for (PairWiseClassifier pwc : this.pwClassifiers) {
				double[] dist = pwc.c.distributionForInstance(datasetCopy.get(0));

				switch (this.config.getVotingStrategy()) {
				case RPCConfig.V_VOTING_STRATEGY_CLASSIFY:
					if (dist[0] > dist[1]) {
						Maps.increaseCounterInDoubleMap(vote, pwc.a);
					} else {
						Maps.increaseCounterInDoubleMap(vote, pwc.b);
					}
					break;
				default:
				case RPCConfig.V_VOTING_STRATEGY_PROBABILITY:
					Maps.increaseCounterInDoubleMap(vote, pwc.a, dist[0]);
					Maps.increaseCounterInDoubleMap(vote, pwc.b, dist[1]);
					break;
				}
			}

			List<String> ranking = new LinkedList<>(vote.keySet());
			ranking.sort((arg0, arg1) -> vote.get(arg1).compareTo(vote.get(arg0)));

			return new Prediction<>(new Ranking<>(ranking));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
