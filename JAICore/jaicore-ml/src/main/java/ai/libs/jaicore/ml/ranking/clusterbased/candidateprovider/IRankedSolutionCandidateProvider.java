package ai.libs.jaicore.ml.ranking.clusterbased.candidateprovider;

import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;

public interface IRankedSolutionCandidateProvider<I,S> {
	Ranking<S> getCandidate(ProblemInstance<I> instance);
}