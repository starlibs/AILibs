package hasco.optimizingfactory;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import hasco.core.SoftwareConfigurationProblem;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.EvaluatedSoftwareConfigurationSolution;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.logging.ToJSONStringUtil;

public class OptimizingFactory<P extends SoftwareConfigurationProblem<V>, T, C extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends AAlgorithm<OptimizingFactoryProblem<P, T, V>, T> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(OptimizingFactory.class);
	private String loggerName;

	private final SoftwareConfigurationAlgorithmFactory<P, C, V> factoryForOptimizationAlgorithm;
	private T constructedObject;
	private V performanceOfObject;
	private final SoftwareConfigurationAlgorithm<P, C, V> optimizer;

	public OptimizingFactory(final OptimizingFactoryProblem<P, T, V> problem, final SoftwareConfigurationAlgorithmFactory<P, C, V> factoryForOptimizationAlgorithm) {
		super(problem);
		this.factoryForOptimizationAlgorithm = factoryForOptimizationAlgorithm;
		this.optimizer = this.factoryForOptimizationAlgorithm.getAlgorithm(this.getInput().getConfigurationProblem());
		this.optimizer.registerListener(new Object() {
			@Subscribe
			public void receiveAlgorithmEvent(final AlgorithmEvent event) {
				if (!(event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent)) {
					OptimizingFactory.this.post(event);
				}
			}
		});
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		switch (this.getState()) {
		case created:

			/* initialize optimizer */
			if (this.loggerName != null) {
				this.logger.info("Setting logger of optimizer {} to {}.optAlgo", this.optimizer.getClass().getName(), this.loggerName);
				this.optimizer.setLoggerName(this.loggerName + ".optAlgo");
			}

			AlgorithmEvent initEvent = this.optimizer.next();
			assert initEvent instanceof AlgorithmInitializedEvent : "The first event emitted by the optimizer has not been its AlgorithmInitializationEvent";
			return this.activate();

		case active:
			C solutionModel = this.optimizer.call();
			try {
				this.constructedObject = this.getInput().getBaseFactory().getComponentInstantiation(solutionModel.getComponentInstance());
				this.performanceOfObject = solutionModel.getScore();
				return this.terminate();
			} catch (ComponentInstantiationFailedException e) {
				throw new AlgorithmException(e, "Could not conduct next step in OptimizingFactory due to an exception in the component instantiation.");
			}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}
	}

	@Override
	public T call() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.constructedObject;
	}

	/**
	 * @return the optimizer that is used for building the object
	 */
	public SoftwareConfigurationAlgorithm<P, C, V> getOptimizer() {
		return this.optimizer;
	}

	public AlgorithmInitializedEvent init() {
		AlgorithmEvent e = null;
		while (this.hasNext()) {
			e = this.next();
			if (e instanceof AlgorithmInitializedEvent) {
				return (AlgorithmInitializedEvent) e;
			}
		}
		throw new IllegalStateException("Could not complete initialization");
	}

	public V getPerformanceOfObject() {
		return this.performanceOfObject;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._algorithm");
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("factoryForOptimizationAlgorithm", this.factoryForOptimizationAlgorithm);
		fields.put("constructedObject", this.constructedObject);
		fields.put("performanceOfObject", this.performanceOfObject);
		fields.put("optimizer", this.optimizer);
		return ToJSONStringUtil.toJSONString(fields);
	}

	@Override
	public void cancel() {
		this.logger.info("Received cancel. First canceling the optimizer {}, then my own routine!", this.optimizer.getId());
		this.optimizer.cancel();
		this.logger.debug("Now canceling the OptimizingFactory itself.");
		super.cancel();
		assert this.isCanceled() : "Cancel-flag must be true at end of cancel routine!";
	}
}
