package jaicore.modifiedISAC;

import jaicore.CandidateProvider.IRankedSolutionCandidateProvider;
import jaicore.CustomDataTypes.ProblemInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class CandidateProviderModifiedISAC implements IRankedSolutionCandidateProvider<Instance, String> {

	@Override
	public ClassifierRankingForGroup getCandidate(ProblemInstance<Instance> instance) {
			double[] vectorFormInstance = instance.getInstance().toDoubleArray();
			// in welcher form kommt das object wenn instances
			
		return null;
	}

}
