package de.upb.crc901.automl.hascoml;

import hasco.core.HASCOSolutionCandidate;
import jaicore.planning.EvaluatedSearchGraphBasedPlan;

public class HASCOMLContinuousSelectionSolution<C> {

	private HASCOSolutionCandidate<EvaluatedSearchGraphBasedPlan, C, Double> hascoSolution;
	private Double selectionScore = null;
	private Double testScore = null;

	public HASCOMLContinuousSelectionSolution(final HASCOSolutionCandidate<EvaluatedSearchGraphBasedPlan, C, Double> hascoSolution) {
		super();
		this.hascoSolution = hascoSolution;
	}

	public C getSolution() {
		return this.hascoSolution.getSolution();
	}

	public int getTimeForScoreComputation() {
		return this.hascoSolution.getTimeToComputeScore();
	}

	public Double getValidationScore() {
		return this.hascoSolution.getScore();
	}

	public void setSelectionScore(final double selectionScore) {
		this.selectionScore = selectionScore;
	}

	public Double getSelectionScore() {
		return this.selectionScore;
	}

	public void setTestScore(final double testScore) {
		this.testScore = testScore;
	}

	public Double getTestScore() {
		return this.testScore;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getSolution());
		sb.append(" ");
		sb.append("Val: " + this.getValidationScore());
		sb.append(" ");
		sb.append("Sel: " + this.getSelectionScore());
		sb.append(" ");
		sb.append("Test: " + this.getTestScore());

		return sb.toString();
	}
}
