package jaicore.ml.evaluation;

import java.util.Timer;
import java.util.TimerTask;

import weka.classifiers.Classifier;

public class TimeoutableEvaluator implements ClassifierEvaluator {

	private ClassifierEvaluator ce;
	private int timeoutInMS;
	private Timer timer;

	public TimeoutableEvaluator(final ClassifierEvaluator ce, final int timeoutInMS) {
		this.ce = ce;
		this.timeoutInMS = timeoutInMS;
		this.timer = new Timer();
	}

	@Override
	public Double evaluate(final Classifier object) throws Exception {
		TimeoutTask timeout = new TimeoutTask(Thread.currentThread());
		this.timer.schedule(timeout, this.timeoutInMS);
		Double returnValue = 30000.0;
		try {
			returnValue = this.ce.evaluate(object);
		} finally {
			timeout.cancel();
		}
		return returnValue;
	}

	class TimeoutTask extends TimerTask {
		Thread callee;

		TimeoutTask(final Thread callee) {
			this.callee = callee;
		}

		@Override
		public void run() {
			System.out.println("Interrupt evaluator!");
			this.callee.interrupt();
		}
	}

}
