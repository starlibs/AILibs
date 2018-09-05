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
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;

public class OptimizingFactory<P extends SoftwareConfigurationProblem<V>, T, V extends Comparable<V>> implements IAlgorithm<OptimizingFactoryProblem<P,T,V>, T>, ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(OptimizingFactory.class);
	private final OptimizingFactoryProblem<P, T, V> problem;
	private final SoftwareConfigurationAlgorithmFactory<P, ?, V> factoryForOptimizationAlgorithm;
	private T constructedObject;
	private final EventBus eventBus = new EventBus();

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
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T call() throws Exception {
		factoryForOptimizationAlgorithm.setProblemInput(problem.getConfigurationProblem());
		SoftwareConfigurationAlgorithm<P, ?, V> optimizer = factoryForOptimizationAlgorithm.getAlgorithm();
		if (optimizer instanceof ILoggingCustomizable) {
			this.logger.info("Switching the logger name of the actually used optimizer to {}", loggerName);
			((ILoggingCustomizable)optimizer).setLoggerName(loggerName + ".optimizer");
		}
		optimizer.registerListener(this);
		optimizer.call();
		ComponentInstance solutionModel = optimizer.getOptimizationResult().getResult();
		this.constructedObject = problem.getBaseFactory().getComponentInstantiation(solutionModel);
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
}
