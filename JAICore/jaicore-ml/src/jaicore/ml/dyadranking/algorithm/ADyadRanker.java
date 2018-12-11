package jaicore.ml.dyadranking.algorithm;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.IBatchLearner;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * <p>
 * An abstract representation of a dyad ranker.
 * 
 * <p>
 * "Label ranking is a specific type of preference learning problem, namely the
 * prob- lem of learning a model that maps instances to rankings over a finite
 * set of predefined alternatives. Like in conventional classification, these
 * alternatives are identified by their name or label while not being
 * characterized in terms of any properties or features that could be
 * potentially useful for learning. In this paper, we consider a generalization
 * of the label ranking problem that we call dyad ranking. In dyad ranking, not
 * only the instances but also the alter- natives are represented in terms of
 * attributes."
 * 
 * <p>
 * Schäfer, D., & Hüllermeier, E. (2018). Dyad ranking using Plackett--Luce
 * models based on joint feature representations. Machine Learning, 107(5),
 * 903–941. https://doi.org/10.1007/s10994-017-5694-9
 * 
 * @author Helena Graf
 *
 */
public abstract class ADyadRanker implements IBatchLearner<IDyadRankingInstance> {

}
