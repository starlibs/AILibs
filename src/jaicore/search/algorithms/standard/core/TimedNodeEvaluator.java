package jaicore.search.algorithms.standard.core;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ITimeoutNodeEvaluator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.structure.core.Node;

public class TimedNodeEvaluator<T, V extends Comparable<V>> implements SerializableNodeEvaluator<T, V> {

	private final static Logger logger = LoggerFactory.getLogger(TimedNodeEvaluator.class);
	private NodeEvaluator<T, V> evaluator;
	private static Timer timer = new Timer();
	private int timeout;
	private final ITimeoutNodeEvaluator<T, V> timeoutValueGenerator;

	public TimedNodeEvaluator(NodeEvaluator<T, V> evaluator, ITimeoutNodeEvaluator<T, V> timeoutValue, int timeout) {
		super();
		this.evaluator = evaluator;
		this.timeout = timeout;
		this.timeoutValueGenerator = timeoutValue;
	}

	@Override
	public V f(Node<T, V> node) throws Exception {
		Thread t = Thread.currentThread();
		final TimerTask tt = new TimerTask() {
			
			@Override
			public void run() {
				logger.warn("Sending interrupt!");
				System.out.println("INTERRUPTING in TIME NODE EVALUATOR");
				t.interrupt();
			}
		};
		timer.schedule(tt, timeout);
		
		/* compute f (if interrupted, it should throw an exception) */
		try {
			V score = evaluator.f(node);
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

}
