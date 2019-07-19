package ai.libs.jaicore.ml.ranking;

import ai.libs.jaicore.ml.ILearningAlgorithm;

/**
 *
 * @author mwever
 *
 * @param <X>
 * @param <S>
 * @param <C>
 */
public interface IRanker<X, S, C extends IRankerConfig> extends ILearningAlgorithm<X, IRanking<S>, C> {

}
