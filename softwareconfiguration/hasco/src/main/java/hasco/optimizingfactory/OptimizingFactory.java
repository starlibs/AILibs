package hasco.optimizingfactory;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import hasco.core.SoftwareConfigurationProblem;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.EvaluatedSoftwareConfigurationSolution;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

public class OptimizingFactory<P extends SoftwareConfigurationProblem<V>, T, C extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>>
		extends AAlgorithm<OptimizingFactoryProblem<P, T, V>, T> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(OptimizingFactory.class);
	private String loggerName;

	private final SoftwareConfigurationAlgorithmFactory<P, C, V> factoryForOptimizationAlgorithm;
	private T constructedObject;
	private V performanceOfObject;
	private final SoftwareConfigurationAlgorithm<P, C, V> optimizer;

	public OptimizingFactory(final OptimizingFactoryProblem<P, T, V> problem,
			final SoftwareConfigurationAlgorithmFactory<P, C, V> factoryForOptimizationAlgorithm) {
		super(problem);
		this.factoryForOptimizationAlgorithm = factoryForOptimizationAlgorithm;
		this.factoryForOptimizationAlgorithm.setProblemInput(this.getInput().getConfigurationProblem());
		this.optimizer = this.factoryForOptimizationAlgorithm.getAlgorithm();
		this.optimizer.registerListener(new Object() {
			@Subscribe
			public void receiveAlgorithmEvent(AlgorithmEvent event) {
				if (!(event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent))
					post(event);
			}
		});
	}

	@Override
	public AlgorithmEvent nextWithException()
			throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		switch (this.getState()) {
		case created: {

			/* initialize optimizer */
			if (this.loggerName != null) {
				if (this.optimizer instanceof ILoggingCustomizable) {
					logger.info("Setting logger of optimizer {} to {}", optimizer, loggerName + ".optAlgo");
					((ILoggingCustomizable) this.optimizer).setLoggerName(loggerName + ".optAlgo");
				} else
					logger.info(
							"Optimizer {} does not implement the ILoggingCustomizable interface and, hence, will not receive a customized log identifier.",
							this.optimizer);
			}

			while (!(this.optimizer.next() instanceof AlgorithmInitializedEvent)) {
				;
			}
			return activate();
		}
		case active: {
			C solutionModel = this.optimizer.call();
			try {
				this.constructedObject = this.getInput().getBaseFactory()
						.getComponentInstantiation(solutionModel.getComponentInstance());
				this.performanceOfObject = solutionModel.getScore();
				return terminate();
			} catch (ComponentInstantiationFailedException e) {
				throw new AlgorithmException(e,
						"Could not conduct next step in OptimizingFactory due to an exception in the component instantiation.");
			}
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}
	}

	@Override
	public T call()
			throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
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
}
