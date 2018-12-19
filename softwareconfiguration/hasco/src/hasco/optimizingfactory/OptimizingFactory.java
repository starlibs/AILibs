package hasco.optimizingfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import hasco.core.SoftwareConfigurationProblem;
import hasco.model.EvaluatedSoftwareConfigurationSolution;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;

public class OptimizingFactory<P extends SoftwareConfigurationProblem<V>, T, C extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends AAlgorithm<OptimizingFactoryProblem<P, T, V>, T> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(OptimizingFactory.class);
	private String loggerName;

	private final SoftwareConfigurationAlgorithmFactory<P, ?, C, V> factoryForOptimizationAlgorithm;
	private T constructedObject;
	private V performanceOfObject;

	/* factory state */
	private SoftwareConfigurationAlgorithm<P, ?, C, V> optimizer;

	public OptimizingFactory(final OptimizingFactoryProblem<P, T, V> problem, final SoftwareConfigurationAlgorithmFactory<P, ?, C, V> factoryForOptimizationAlgorithm) {
		super(problem);
		this.factoryForOptimizationAlgorithm = factoryForOptimizationAlgorithm;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created: {
			this.factoryForOptimizationAlgorithm.setProblemInput(this.getInput().getConfigurationProblem());
			this.optimizer = this.factoryForOptimizationAlgorithm.getAlgorithm();
			if (this.optimizer instanceof ILoggingCustomizable) {
				this.logger.info("Switching the logger name of the actually used optimizer to {}", this.getLoggerName());
				this.optimizer.setLoggerName(this.getLoggerName() + ".optimizer");
			}
			this.optimizer.registerListener(this);
			while (!(this.optimizer.next() instanceof AlgorithmInitializedEvent)) {
				;
			}
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		}
		case active: {
			this.optimizer.call();
			C solutionModel = this.optimizer.getOptimizationResult().getResult();
			this.constructedObject = this.getInput().getBaseFactory().getComponentInstantiation(solutionModel.getComponentInstance());
			this.performanceOfObject = this.optimizer.getOptimizationResult().getValue();
			this.setState(AlgorithmState.inactive);
			return new AlgorithmFinishedEvent();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}
	}

	@Override
	public T call() throws Exception {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.constructedObject;
	}

	@Subscribe
	public void receiveSolutionEvent(final SolutionCandidateFoundEvent<?> event) {
		this.post(event);
	}

	/**
	 * @return the optimizer that is used for building the object
	 */
	public SoftwareConfigurationAlgorithm<P, ?, C, V> getOptimizer() {
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
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.factoryForOptimizationAlgorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.factoryForOptimizationAlgorithm).setLoggerName(name + ".optAlgoFactory");
		}
		super.setLoggerName(this.loggerName + "._algorithm");
	}
}
