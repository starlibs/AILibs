package jaicore.ml.evaluation.evaluators.weka;

import java.util.TimerTask;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.concurrent.TimeoutTimer;
import jaicore.interrupt.Interrupter;
import weka.classifiers.Classifier;

public class TimeoutableEvaluator implements IClassifierEvaluator {

	/** The object evaluator that shall actually be called. */
	private IObjectEvaluator<Classifier, Double> ce;

	/** The timeout in milliseconds, i.e. after what time the evaluator shall be interrupted. */
	private int timeoutInMS;

	/**
	 * C'tor create a timeoutable evaluator out of any other IObjectEvaluator. Note that these evaluators need to check whether their thread has been interrupted to actually react on the timeout.
	 *
	 * @param iSolutionEvaluator
	 *            The evaluator which shall actually be called and maybe needs to be interrupted after the given timeout.
	 * @param timeoutInMS
	 *            The timeout in milliseconds to interrupt an evaluation with the {iSolutionEvaluator}
	 */
	public TimeoutableEvaluator(final IObjectEvaluator<Classifier, Double> iSolutionEvaluator, final int timeoutInMS) {
		this.ce = iSolutionEvaluator;
		this.timeoutInMS = timeoutInMS;
	}

	@Override
	public Double evaluate(final Classifier object) throws ObjectEvaluationFailedException, InterruptedException {
		TimerTask timeoutTask = TimeoutTimer.getInstance().getSubmitter().interruptMeAfterMS(this.timeoutInMS, "Evaluation of classifier " + object + " has timeouted (" + TimeoutableEvaluator.class.getName() + ")");
		Double returnValue = 30000.0;
		try {
			returnValue = this.ce.evaluate(object);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Interrupter.get().markInterruptOnCurrentThreadAsResolved(timeoutTask);
		} catch (Throwable e) {
			if (!e.getMessage().contains("Killed WEKA") && !e.getMessage().contains("Bag size needs")) {
				throw new ObjectEvaluationFailedException(e, "Error");
			}
		} finally {
			timeoutTask.cancel();
		}
		return returnValue;
	}

}
