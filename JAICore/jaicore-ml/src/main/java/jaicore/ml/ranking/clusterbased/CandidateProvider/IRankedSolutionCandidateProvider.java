package jaicore.ml.ranking.clusterbased.CandidateProvider;

import jaicore.ml.ranking.clusterbased.CustomDataTypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.CustomDataTypes.Ranking;

public interface IRankedSolutionCandidateProvider<I,S> {
	Ranking<S> getCandidate(ProblemInstance<I> instance);
}
