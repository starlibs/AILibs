package jaicore.ml.dyadranking.algorithm;

import java.util.ArrayList;
import java.util.TreeMap;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A feature transformation Placket-Luce dyad ranker. By default uses bilinear
 * feature transformation.
 * 
 * @author Helena Graf
 *
 */
public class FeatureTransformPLDyadRanker extends APLDyadRanker {

	private IDyadFeatureTransform featureTransform;
	private Vector w;

	public FeatureTransformPLDyadRanker() {
		this(new BiliniearFeatureTransform());
	}

	public FeatureTransformPLDyadRanker(IDyadFeatureTransform featureTransform) {
		this.featureTransform = featureTransform;
	}

	@Override
	public IDyadRankingInstance predict(IInstance instance) throws PredictionException {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"FeatureTransformDyadRanker can only be used with IDyadRankingInstances.");
		}

		IDyadRankingInstance dyadRankingInstance = (IDyadRankingInstance) instance;
		TreeMap<Double, Dyad> ordering = new TreeMap<>();

		dyadRankingInstance.forEach(dyad -> ordering.put(computeSkillForDyad(dyad), dyad));

		return new DyadRankingInstance(new ArrayList<Dyad>(ordering.descendingMap().values()));
	}

	private double computeSkillForDyad(Dyad dyad) {
		return Math.exp(w.dotProduct(featureTransform.transform(dyad)));
	}
}
