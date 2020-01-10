package ai.libs.jaicore.ml.weka;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.basic.Maps;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;

public class RankingByPairwiseComparison {

	private RPCConfig config;

	private List<Integer> labelIndices;
	private Set<String> labelSet = new HashSet<>();

	class PairWiseClassifier {
		private String a;
		private String b;
		private Classifier c;
	}

	private List<PairWiseClassifier> pwClassifiers = new LinkedList<>();

	public RankingByPairwiseComparison(final RPCConfig config) {
		this.config = config;
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

	public void fit(final Instances dataset, final int labels) throws Exception {
		this.labelIndices = getLabelIndices(labels, dataset);
		this.labelIndices.stream().map(x -> dataset.attribute(x).name()).forEach(this.labelSet::add);
		Instances plainPWDataset = this.applyFiltersToDataset(dataset);

		try {
			for (int i = 0; i < this.labelIndices.size() - 1; i++) {
				for (int j = i + 1; j < this.labelIndices.size(); j++) {

					PairWiseClassifier pwc = new PairWiseClassifier();
					pwc.a = dataset.attribute(this.labelIndices.get(i)).name();
					pwc.b = dataset.attribute(this.labelIndices.get(j)).name();

					pwc.c = AbstractClassifier.forName(this.config.getBaseLearner(), null);

					Instances pwDataset = new Instances(plainPWDataset);

					for (int k = 0; k < pwDataset.size(); k++) {
						String value;
						if (dataset.get(k).value(this.labelIndices.get(i)) > dataset.get(k).value(this.labelIndices.get(j))) {
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

	public List<String> predict(final Instance xTest) throws PredictionException {
		try {
			Instances datasetCopy = new Instances(xTest.dataset(), 0);
			datasetCopy.add(xTest);
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
			return ranking;
		} catch (Exception e) {
			throw new PredictionException("Could not create a prediction.", e);
		}
	}
}
