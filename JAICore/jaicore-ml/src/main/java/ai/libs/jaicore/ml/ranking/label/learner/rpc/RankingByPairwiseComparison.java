package ai.libs.jaicore.ml.ranking.label.learner.rpc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.IRankingPredictionBatch;
import org.api4.java.ai.ml.ranking.label.dataset.ILabelRankingDataset;
import org.api4.java.ai.ml.ranking.label.dataset.ILabelRankingInstance;
import org.api4.java.ai.ml.ranking.label.learner.ILabelRanker;

import ai.libs.jaicore.basic.Maps;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

public class RankingByPairwiseComparison extends ASupervisedLearner<ILabelRankingInstance, ILabelRankingDataset, IRanking<String>, IRankingPredictionBatch> implements ILabelRanker {

	// Parameter configuration of RankingByPairwiseComparison
	private IRPCConfig config;

	private Instances plainPWDataset = null;
	private List<Integer> labelIndices;
	private Set<String> labelSet = new HashSet<>();

	class PairWiseClassifier {
		String a;
		String b;
		ISingleLabelClassifier c;
	}

	List<PairWiseClassifier> pwClassifiers = new LinkedList<>();

	public RankingByPairwiseComparison(final IRPCConfig config) throws Exception {
		this.config = config;
	}

	@Override
	public void fit(final ILabelRankingDataset dTrain) throws TrainingException, InterruptedException {
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
	public IRanking<String> predict(final ILabelRankingInstance xTest) throws PredictionException, InterruptedException {
		try {

			Map<String, Double> vote = new HashMap<>();
			this.labelSet.stream().forEach(x -> vote.put(x, 0.0));

			for (PairWiseClassifier pwc : this.pwClassifiers) {
				double[] dist = pwc.c.predict(xTest);

				switch (this.config.getVotingStrategy()) {
				case IRPCConfig.V_VOTING_STRATEGY_CLASSIFY:
					if (dist[0] > dist[1]) {
						Maps.increaseCounterInDoubleMap(vote, pwc.a);
					} else {
						Maps.increaseCounterInDoubleMap(vote, pwc.b);
					}
					break;
				default:
				case IRPCConfig.V_VOTING_STRATEGY_PROBABILITY:
					Maps.increaseCounterInDoubleMap(vote, pwc.a, dist[0]);
					Maps.increaseCounterInDoubleMap(vote, pwc.b, dist[1]);
					break;
				}
			}

			List<String> ranking = new LinkedList<>(vote.keySet());
			ranking.sort((arg0, arg1) -> vote.get(arg1).compareTo(vote.get(arg0)));

			return new Ranking<>(ranking);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IRankingPredictionBatch predict(final ILabelRankingInstance[] dTest) throws PredictionException, InterruptedException {
		return null;
	}

}
