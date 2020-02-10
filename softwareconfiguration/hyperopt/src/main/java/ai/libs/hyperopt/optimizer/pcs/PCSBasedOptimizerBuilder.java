package ai.libs.hyperopt.optimizer.pcs;

import java.util.Collection;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;

public class PCSBasedOptimizerBuilder {

	private Double minBudget;
	private Double maxBudget;
	private Integer nIterations;
	private String executionPath;
	private Integer numThreads = 1;

	// task input
	private Collection<Component> components;
	private String requestedInterface;
	private IObjectEvaluator<ComponentInstance, Double> evaluator;

	public PCSBasedOptimizerBuilder(final Collection<Component> components, final String requestedInterface, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
		this.components = components;
		this.requestedInterface = requestedInterface;
		this.evaluator = evaluator;
	}

	public PCSBasedOptimizerBuilder withComponents(final Collection<Component> components) {
		this.components = components;
		return this;
	}

	public PCSBasedOptimizerBuilder withRequestedInterface(final String requestedInterface) {
		this.requestedInterface = requestedInterface;
		return this;
	}

	/**
	 * executionPath is the folder that BOHBOptimizer script runs, pcs files will
	 * also be created here
	 *
	 * @param executionPath
	 * @return
	 */
	public PCSBasedOptimizerBuilder withExecutionPath(final String executionPath) {
		this.executionPath = executionPath;
		return this;
	}

	/**
	 * The smallest budget to consider. Needs to be positive!
	 *
	 * @param minBudget
	 * @return
	 */
	public PCSBasedOptimizerBuilder minBudget(final Double minBudget) {
		this.minBudget = minBudget;
		return this;
	}

	/**
	 * The largest budget to consider. Needs to be larger than min_budget!
	 *
	 * @param maxBudget
	 * @return
	 */
	public PCSBasedOptimizerBuilder maxBudget(final Double maxBudget) {
		this.maxBudget = maxBudget;
		return this;
	}

	/**
	 * Number of iterations
	 * @param nIterations
	 * @return
	 */
	public PCSBasedOptimizerBuilder nIterations(final Integer nIterations) {
		this.nIterations = nIterations;
		return this;
	}

	/**
	 * number of threads when running in parallel
	 * @param numThreads
	 * @return
	 */
	public PCSBasedOptimizerBuilder numThreads(final Integer numThreads) {
		this.numThreads = numThreads;
		return this;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	public String getRequestedInterface() {
		return this.requestedInterface;
	}

	public IObjectEvaluator<ComponentInstance, Double> getEvaluator() {
		return this.evaluator;
	}

	public Double getMinBudget() {
		return this.minBudget;
	}

	public Double getMaxBudget() {
		return this.maxBudget;
	}

	public Integer getnIterations() {
		return this.nIterations;
	}

	public String getExecutionPath() {
		return this.executionPath;
	}

	public Integer getNumThreads() {
		return this.numThreads;
	}

}
