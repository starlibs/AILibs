package jaicore.CandidateProvider;

import jaicore.CustomDataTypes.ProblemInstance;
import jaicore.CustomDataTypes.Ranking;

public interface IRankedSolutionCandidateProvider<I,S> {
	Ranking<S> getCandidate(ProblemInstance<I> instance);
}
