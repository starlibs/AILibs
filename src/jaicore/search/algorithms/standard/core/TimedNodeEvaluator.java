package jaicore.search.algorithms.standard.core;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ITimeoutNodeEvaluator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.structure.core.Node;

public class TimedNodeEvaluator<T, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, V>  implements SerializableNodeEvaluator<T, V>, ICancelableNodeEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(TimedNodeEvaluator.class);
	private static Timer timer = new Timer("TimedNodeEvaluator");
	private int timeout;
	private final ITimeoutNodeEvaluator<T, V> timeoutValueGenerator;
	private TimerTask tt;
	
	public TimedNodeEvaluator(INodeEvaluator<T, V> evaluator, ITimeoutNodeEvaluator<T, V> timeoutValue, int timeout) {
		super(evaluator);
		if (!(evaluator instanceof ICancelableNodeEvaluator))
			logger.warn("The given evaluator is not cancelable. In case of a cancel, the cancel command cannot be propagated to the actual evaluator.");
		this.timeout = timeout;
		this.timeoutValueGenerator = timeoutValue;
	}
	
	@Override
	public V f(Node<T, V> node) throws Exception {
		Thread t = Thread.currentThread();
		synchronized (timer) {
			tt = new TimerTask() {
				
				@Override
				public void run() {
					logger.info("Sending interrupt.");
					t.interrupt();
				}
			};
			timer.schedule(tt, timeout);
		}
		
		/* compute f (if interrupted, it should throw an exception) */
		try {
			V score = super.f(node);
			tt.cancel();
			Thread.interrupted(); // reset interrupted status
			return score;
		}
		catch (InterruptedException e) {
			tt.cancel();
			Thread.interrupted(); // reset interrupted status
			return timeoutValueGenerator.f(node);
		}
		catch (Exception e) {
			tt.cancel();
			throw e;
		}
	}
	
	public void cancel() {
		logger.info("Received cancel signal.");
		synchronized (timer) {
			tt.run();
			timer.cancel();
		}
		if (!isDecoratedEvaluatorCancelable())
			logger.warn("The given evaluator is not cancelable. In case of a cancel, the cancel command cannot be propagated to the actual evaluator.");
		else {
			((ICancelableNodeEvaluator)getEvaluator()).cancel();
		}
	}
}
