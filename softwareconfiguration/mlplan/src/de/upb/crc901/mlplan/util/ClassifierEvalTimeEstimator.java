package de.upb.crc901.mlplan.util;

import java.util.Collection;
import java.util.HashSet;

import org.aeonbits.owner.ConfigCache;

import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML.HASCOClassificationMLSolution;
import de.upb.crc901.mlplan.AbstractMLPlanConfig;

public class ClassifierEvalTimeEstimator {

	private static final AbstractMLPlanConfig CONFIG = ConfigCache.getOrCreate(AbstractMLPlanConfig.class);

	private Collection<HASCOClassificationMLSolution> solutionSet;

	public ClassifierEvalTimeEstimator(final HASCOClassificationMLSolution solution) {
		this.solutionSet = new HashSet<>();
		this.solutionSet.add(solution);
	}

	public ClassifierEvalTimeEstimator(final Collection<HASCOClassificationMLSolution> solutionSet) {
		this.solutionSet = solutionSet;
	}

	public int getSearchWholeMCEvalTime() {
		return this.solutionSet.stream().mapToInt(x -> x.getTimeToComputeScore()).sum();
	}

	public int getSearchSingleMCIterationTime() {
		return (int) Math.round((double) this.getSearchWholeMCEvalTime() / CONFIG.searchMCIterations());
	}

	public int estimateSelectionSingleMCIterationTime() {
		return (int) Math.round(this.getSearchSingleMCIterationTime() / CONFIG.searchDataPortion());
	}

	public int estimateSelectionWholeMCEvalTime() {
		return this.estimateSelectionSingleMCIterationTime() * CONFIG.selectionMCIterations();
	}

	public int estimateFinalBuildTime() {
		return (int) Math.round(this.estimateSelectionSingleMCIterationTime() / (1 - CONFIG.selectionDataPortion()));
	}

	public int estimateTotalSelectionAndFinalBuildTime() {
		return this.estimateFinalBuildTime() + this.estimateSelectionWholeMCEvalTime();
	}

}
