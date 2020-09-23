package ai.libs.hasco.twophase;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.timing.TimedComputation;

public class TwoPhaseCandidateEvaluator implements Runnable, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(TwoPhaseCandidateEvaluator.class);

	/* input variables */
	private final IObjectEvaluator<IComponentInstance, Double> evaluator;
	private final long selectionPhaseDeadline;
	private final HASCOSolutionCandidate<Double> c;

	/* derives variables */
	private final int estimatedInSelectionSingleIterationEvaluationTime;
	private final int estimatedPostProcessingTime;
	private final int estimatedTotalEffortInCaseOfSelection;
	private final int timeoutForEvaluation;

	/* result variables */
	private double selectionScore;
	private long trueEvaluationTime;

	/* shared variables */
	private final Semaphore sem;

	public TwoPhaseCandidateEvaluator(final HASCOSolutionCandidate<Double> c, final long selectionPhaseDeadline, final double timeoutTolerance, final double blowupInSelection, final double blowupInPostProcessing,
			final IObjectEvaluator<IComponentInstance, Double> evaluator, final Semaphore sem) {
		super();
		this.c = c;
		this.selectionPhaseDeadline = selectionPhaseDeadline;

		/* Time needed to compute the score of this solution in phase 1 */
		int inSearchSolutionEvaluationTime = c.getTimeToEvaluateCandidate();
		this.estimatedInSelectionSingleIterationEvaluationTime = (int) Math.round(inSearchSolutionEvaluationTime * blowupInSelection);
		this.estimatedPostProcessingTime = (int) Math.round(this.estimatedInSelectionSingleIterationEvaluationTime * blowupInPostProcessing);
		this.estimatedTotalEffortInCaseOfSelection = this.estimatedInSelectionSingleIterationEvaluationTime;
		this.timeoutForEvaluation = (int) Math.max(1000, this.estimatedInSelectionSingleIterationEvaluationTime * (1 + timeoutTolerance));

		this.evaluator = evaluator;
		this.sem = sem;
	}

	@Override
	public void run() {

		final long timestampStart = System.currentTimeMillis();
		try {

			/* We assume linear growth of the evaluation time here to estimate (A) time for
			 * selection phase, (B) time for post-processing the solution in case it gets selected. */
			this.logger.info("Estimating {}ms re-evaluation time and {}ms build time for candidate {} in case of selection (evaluation time during search was {}ms).", this.estimatedInSelectionSingleIterationEvaluationTime,
					this.estimatedPostProcessingTime, this.c.getComponentInstance(), this.c.getTimeToEvaluateCandidate());

			/* If we have a global timeout, check whether considering this model is feasible. */
			if (this.selectionPhaseDeadline > 0) {
				int remainingTime = (int) (this.selectionPhaseDeadline - System.currentTimeMillis());
				if (this.estimatedTotalEffortInCaseOfSelection >= remainingTime) {
					this.logger.info(
							"Not evaluating solution {} anymore, because its insearch evaluation time was {}, expected evaluation time for selection is {}, and expected post-processing time is {}. This adds up to {}, which exceeds the remaining time of {}!",
							this.c.getComponentInstance(), this.c.getTimeToEvaluateCandidate(), this.estimatedInSelectionSingleIterationEvaluationTime, this.estimatedPostProcessingTime, this.estimatedTotalEffortInCaseOfSelection,
							remainingTime);
					return;
				}
			}

			/* Schedule a timeout for this evaluation, which is 10% over the estimated time. Grant at least 1s */
			this.logger.info("Starting selection performance computation with timeout {}ms", this.timeoutForEvaluation);
			TimedComputation.compute(() -> {
				this.selectionScore = this.evaluator.evaluate(this.c.getComponentInstance());
				this.trueEvaluationTime = (System.currentTimeMillis() - timestampStart);
				this.logger.info("Obtained evaluation score of {} after {}ms for candidate {} (score assigned by HASCO was {}).", this.selectionScore, this.trueEvaluationTime, this.c.getComponentInstance(), this.c.getScore());
				return true;
			}, new Timeout(this.timeoutForEvaluation, TimeUnit.MILLISECONDS), "Timeout for evaluation of ensemble candidate " + this.c.getComponentInstance());
		} catch (InterruptedException e) {
			assert !Thread.currentThread().isInterrupted() : "The interrupted-flag should not be true when an InterruptedException is thrown!";
			this.logger.info("Selection eval of {} got interrupted after {}ms. Defined timeout was: {}ms", this.c.getComponentInstance(), (System.currentTimeMillis() - timestampStart), this.timeoutForEvaluation);
			Thread.currentThread().interrupt(); // no controlled interrupt needed here, because this is only a re-interrupt, and the execution will cease after this anyway
		} catch (ExecutionException e) {
			this.logger.error("Observed an exeption when trying to evaluate a candidate in the selection phase.\n{}", LoggerUtil.getExceptionInfo(e.getCause()));
		} catch (AlgorithmTimeoutedException e) {
			this.logger.info("Evaluation of candidate has timed out: {}", this.c);
		} finally {
			this.sem.release();
			this.logger.debug("Released. Sem state: {}", this.sem.availablePermits());
		}
	}

	public double getSelectionScore() {
		return this.selectionScore;
	}

	public HASCOSolutionCandidate<Double> getSolution() {
		return this.c;
	}

	public int getEstimatedInSelectionSingleIterationEvaluationTime() {
		return this.estimatedInSelectionSingleIterationEvaluationTime;
	}

	public int getEstimatedPostProcessingTime() {
		return this.estimatedPostProcessingTime;
	}

	public int getEstimatedTotalEffortInCaseOfSelection() {
		return this.estimatedTotalEffortInCaseOfSelection;
	}

	public int getTimeoutForEvaluation() {
		return this.timeoutForEvaluation;
	}

	public long getTrueEvaluationTime() {
		return this.trueEvaluationTime;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
