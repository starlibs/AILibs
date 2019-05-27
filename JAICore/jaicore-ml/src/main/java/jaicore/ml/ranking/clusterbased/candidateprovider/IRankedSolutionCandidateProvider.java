package jaicore.ml.ranking.clusterbased.candidateprovider;

import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;

public interface IRankedSolutionCandidateProvider<I,S> {
	Ranking<S> getCandidate(ProblemInstance<I> instance);
}