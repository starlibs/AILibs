package jaicore.ml.evaluation;

import jaicore.basic.IObjectEvaluator;
import jaicore.concurrent.TimeoutTimer;

public class TimeoutableEvaluator<T> implements IObjectEvaluator<T, Double> {

	/** The object evaluator that shall actually be called. */
	private IObjectEvaluator<T, Double> ce;

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
	public TimeoutableEvaluator(final IObjectEvaluator<T, Double> iSolutionEvaluator, final int timeoutInMS) {
		this.ce = iSolutionEvaluator;
		this.timeoutInMS = timeoutInMS;
	}

	@Override
	public Double evaluate(final T object) throws Exception {
		int timeoutTaskID = TimeoutTimer.getInstance().getSubmitter().interruptMeAfterMS(this.timeoutInMS);
		Double returnValue = 30000.0;
		try {
			returnValue = this.ce.evaluate(object);
		} catch (InterruptedException e) {
			// hide the interrupt exception as we simply want to return the default return value.
		} catch (Throwable e) {
			if (e.getMessage() == null || !e.getMessage().contains("Killed WEKA") && !e.getMessage().contains("Bag size needs")) {
				throw e;
			}
		} finally {
			TimeoutTimer.getInstance().getSubmitter().cancelTimeout(timeoutTaskID);
		}
		return returnValue;
	}

}
