package hasco.optimizingfactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import hasco.core.SoftwareConfigurationProblem;
import hasco.model.ComponentInstance;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;

public class OptimizingFactory<P extends SoftwareConfigurationProblem<V>, T, V extends Comparable<V>> implements IAlgorithm<OptimizingFactoryProblem<P, T, V>, T>, ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(OptimizingFactory.class);
	private final OptimizingFactoryProblem<P, T, V> problem;
	private final SoftwareConfigurationAlgorithmFactory<P, ?, V> factoryForOptimizationAlgorithm;
	private T constructedObject;
	private V performanceOfObject;
	private final EventBus eventBus = new EventBus();

	/* factory state */
	private SoftwareConfigurationAlgorithm<P, ?, V> optimizer;
	private AlgorithmState state = AlgorithmState.created;

	public OptimizingFactory(OptimizingFactoryProblem<P, T, V> problem, SoftwareConfigurationAlgorithmFactory<P, ?, V> factoryForOptimizationAlgorithm) {
		super();
		this.problem = problem;
		this.factoryForOptimizationAlgorithm = factoryForOptimizationAlgorithm;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return nextWithException();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {

		switch (state) {
		case created: {
			factoryForOptimizationAlgorithm.setProblemInput(problem.getConfigurationProblem());
			optimizer = factoryForOptimizationAlgorithm.getAlgorithm();
			if (optimizer instanceof ILoggingCustomizable) {
				this.logger.info("Switching the logger name of the actually used optimizer to {}", loggerName);
				((ILoggingCustomizable) optimizer).setLoggerName(loggerName + ".optimizer");
			}
			optimizer.registerListener(this);
			while (!(optimizer.next() instanceof AlgorithmInitializedEvent))
				;
			state = AlgorithmState.active;
			return new AlgorithmInitializedEvent();
		}
		case active: {
			optimizer.call();
			ComponentInstance solutionModel = optimizer.getOptimizationResult().getResult();
			this.constructedObject = problem.getBaseFactory().getComponentInstantiation(solutionModel);
			this.performanceOfObject = optimizer.getOptimizationResult().getValue();
			state = AlgorithmState.inactive;
			return new AlgorithmFinishedEvent();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + state);
		}
	}

	@Override
	public T call() throws Exception {
		while (this.hasNext())
			this.nextWithException();
		return this.constructedObject;
	}

	@Override
	public OptimizingFactoryProblem<P, T, V> getInput() {
		return problem;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Subscribe
	public void receiveSolutionEvent(SolutionCandidateFoundEvent<?> event) {
		eventBus.post(event);
	}

	@Override
	public void registerListener(Object listener) {
		eventBus.register(listener);
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggerName(String name) {
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger of optimizing factory to {}", loggerName);
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	/**
	 * @return the optimizer that is used for building the object
	 */
	public SoftwareConfigurationAlgorithm<P, ?, V> getOptimizer() {
		return optimizer;
	}

	public AlgorithmInitializedEvent init() {
		AlgorithmEvent e = null;
		while (hasNext()) {
			e = next();
			if (e instanceof AlgorithmInitializedEvent)
				return (AlgorithmInitializedEvent) e;
		}
		throw new IllegalStateException("Could not complete initialization");
	}

	public V getPerformanceOfObject() {
		return performanceOfObject;
	}
}
