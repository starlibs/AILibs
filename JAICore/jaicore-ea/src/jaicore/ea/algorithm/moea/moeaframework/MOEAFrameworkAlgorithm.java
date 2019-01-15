package jaicore.ea.algorithm.moea.moeaframework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.util.TypedProperties;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.DelayedCancellationCheckException;
import jaicore.basic.algorithm.exceptions.DelayedTimeoutCheckException;
import jaicore.ea.algorithm.AEvolutionaryAlgorithm;
import jaicore.ea.algorithm.moea.moeaframework.event.MOEAFrameworkAlgorithmResultEvent;

public class MOEAFrameworkAlgorithm extends AEvolutionaryAlgorithm {

	private Algorithm algorithm;
	private int numberOfGenerationsEvolved = 0;

	public MOEAFrameworkAlgorithm(final IMOEAFrameworkAlgorithmConfig config, final IMOEAFrameworkAlgorithmInput input) {
		super(config, input);
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		try {
			this.checkTermination();
		} catch (DelayedTimeoutCheckException e) {
			e.printStackTrace();
		} catch (DelayedCancellationCheckException e) {
			throw new TimeoutException(e.getMessage());
		}

		if (this.getClass().getName().equals("ndea.core.simplend.nd.NDOptimizationEA")) {
			System.out.println(this.getClass().getName() + " step1: " + this.getState());
		}

		switch (this.getState()) {
		case created:
			try {
				this.numberOfGenerationsEvolved = 0;

				// initialize population
				TypedProperties properties = new TypedProperties();
				properties.setInt("populationSize", this.getConfig().populationSize());
				properties.setDouble("sbx.rate", this.getConfig().crossoverRate());
				properties.setDouble("pm.rate", this.getConfig().mutationRate());

				Initialization initialization = new RandomInitialization(this.getInput().getProblem(), this.getConfig().populationSize());
				NondominatedSortingPopulation population = new NondominatedSortingPopulation();
				TournamentSelection selection = new TournamentSelection(2, new ChainedComparator(new ParetoDominanceComparator(), new CrowdingComparator()));
				Variation variation = OperatorFactory.getInstance().getVariation(null, properties, this.getInput().getProblem());
				this.algorithm = new NSGAII(this.getInput().getProblem(), population, null, selection, variation, initialization);
				System.out.println(this.getClass().getName() + " step2");
				this.algorithm.step();
				return super.activate();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		case active:
			System.out.println(this.getClass().getName() + " step3");
			this.algorithm.step();
			return new MOEAFrameworkAlgorithmResultEvent(this.getCurrentResult());
		default:
		case inactive:
			throw new AlgorithmException("The current algorithm state is >inactive<.");
		}

	}

	public void reset() {
		this.setState(AlgorithmState.created);
	}

	public MOEAFrameworkAlgorithmResult getCurrentResult() {
		this.algorithm.getResult();
		Population population = null;
		try {
			population = this.getPopulation();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new MOEAFrameworkAlgorithmResult(this.algorithm.getResult(), population);
	}

	public int getNumberOfGenerationsEvolved() {
		return this.numberOfGenerationsEvolved;
	}

	public int getNumberOfEvaluations() {
		if (this.algorithm == null) {
			return 0;
		} else {
			return this.algorithm.getNumberOfEvaluations();
		}
	}

	@Override
	public MOEAFrameworkAlgorithmResult call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		while ((this.getState() == AlgorithmState.created || this.getState() == AlgorithmState.active) && (this.getConfig().numberOfGenerations() <= 0 || this.numberOfGenerationsEvolved < this.getConfig().numberOfGenerations())
				&& (this.getConfig().numberOfEvaluations() <= 0 || this.algorithm.getNumberOfEvaluations() < this.getConfig().numberOfEvaluations())) {
			this.nextWithException();
			this.numberOfGenerationsEvolved++;
		}
		return this.getCurrentResult();
	}

	@Override
	public IMOEAFrameworkAlgorithmConfig getConfig() {
		return (IMOEAFrameworkAlgorithmConfig) super.getConfig();
	}

	@Override
	public IMOEAFrameworkAlgorithmInput getInput() {
		return (IMOEAFrameworkAlgorithmInput) super.getInput();
	}

	protected Algorithm getAlgorithm() {
		return this.algorithm;
	}

	public Population getPopulation() throws IllegalAccessException, InvocationTargetException {
		if (this.algorithm instanceof NSGAII) {
			return ((NSGAII) this.algorithm).getPopulation();
		}

		Method getPopulationMethod = null;
		try {
			getPopulationMethod = this.getAlgorithm().getClass().getMethod("getPopulation", (Class<?>[]) null);
		} catch (NoSuchMethodException | SecurityException e) {

		}

		if (getPopulationMethod == null) {
			throw new UnsupportedOperationException("The method getPopulation is not available for " + this.getAlgorithm().getClass().getName());
		} else {
			return (Population) getPopulationMethod.invoke(this.getAlgorithm(), (Object[]) null);
		}
	}

	public List<Solution> getPopulationAsList() throws IllegalAccessException, InvocationTargetException {
		List<Solution> population = new LinkedList<>();
		for (Solution solution : this.getPopulation()) {
			population.add(solution);
		}
		return population;
	}
}
