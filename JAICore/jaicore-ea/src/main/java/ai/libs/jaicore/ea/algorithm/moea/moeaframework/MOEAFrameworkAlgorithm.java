package ai.libs.jaicore.ea.algorithm.moea.moeaframework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.EAlgorithmState;
import ai.libs.jaicore.ea.algorithm.AEvolutionaryAlgorithm;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.event.MOEAFrameworkAlgorithmResultEvent;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.util.MOEAFrameworkUtil;

public class MOEAFrameworkAlgorithm extends AEvolutionaryAlgorithm<Population> {

	private Logger logger = LoggerFactory.getLogger(MOEAFrameworkAlgorithm.class);
	private Algorithm algorithm;
	private int numberOfGenerationsEvolved = 0;
	private double bestFitness = 1.0;
	private int numberOfGenerationsWOChange = 0;

	public MOEAFrameworkAlgorithm(final IMOEAFrameworkAlgorithmConfig config, final IMOEAFrameworkAlgorithmInput input) {
		super(config, input);
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		this.checkAndConductTermination();

		this.logger.info("{} step1: {}", this.getClass().getName(), this.getState());

		switch (this.getState()) {
		case CREATED:
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
				this.logger.info("{} step2", this.getClass().getName());
				this.algorithm.step();
				return super.activate();
			} catch (Exception e) {
				throw new AlgorithmException("Could not create the algorithm.", e);
			}
		case ACTIVE:
			this.logger.info("{} step3", this.getClass().getName());
			this.algorithm.step();

			this.numberOfGenerationsWOChange++;
			if (this.getCurrentResult().getResult().get(0).getObjective(0) + this.getConfig().earlyTerminationEpsilon() < this.bestFitness) {
				this.bestFitness = this.getCurrentResult().getResult().get(0).getObjective(0);
				this.numberOfGenerationsWOChange = 0;
			}

			return new MOEAFrameworkAlgorithmResultEvent(this, this.getCurrentResult());
		default:
		case INACTIVE:
			throw new AlgorithmException("The current algorithm state is >inactive<.");
		}

	}

	public void reset() {
		this.setState(EAlgorithmState.CREATED);
	}

	public MOEAFrameworkAlgorithmResult getCurrentResult() throws AlgorithmException {
		this.algorithm.getResult();
		Population population = null;
		try {
			population = this.getPopulation();
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new AlgorithmException("Could not get the result!", e);
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
	public MOEAFrameworkAlgorithmResult call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while ((this.getState() == EAlgorithmState.CREATED || this.getState() == EAlgorithmState.ACTIVE) && (this.getConfig().numberOfGenerations() <= 0 || this.numberOfGenerationsEvolved < this.getConfig().numberOfGenerations())
				&& (this.getConfig().numberOfEvaluations() <= 0 || this.algorithm.getNumberOfEvaluations() < this.getConfig().numberOfEvaluations())) {
			this.nextWithException();
			if (this.logger.isInfoEnabled()) {
				this.logger.info("\n=============\nCurrent Result:\n{}\nGen: {}/{}\nEvals: {}/{}\n=============", MOEAFrameworkUtil.populationToString(this.getCurrentResult().getResult()), this.numberOfGenerationsEvolved,
						this.getConfig().numberOfGenerations(), this.getNumberOfEvaluations(), this.getConfig().numberOfEvaluations());
			}
			this.numberOfGenerationsEvolved++;
		}

		if (this.logger.isInfoEnabled()) {
			this.logger.info("State: {} ", (this.getState() == EAlgorithmState.CREATED || this.getState() == EAlgorithmState.ACTIVE));
			this.logger.info("Generations: {} {}", this.getConfig().numberOfGenerations() <= 0, this.numberOfGenerationsEvolved < this.getConfig().numberOfGenerations());
			this.logger.info("Evaluations: {}", this.getConfig().numberOfEvaluations() <= 0 || this.algorithm.getNumberOfEvaluations() < this.getConfig().numberOfEvaluations());

			this.logger.info("Gen: {}/{}", this.numberOfGenerationsEvolved, this.getConfig().numberOfGenerations());
			this.logger.info("Evals: {}/{}", this.getNumberOfEvaluations(), this.getConfig().numberOfEvaluations());
		}
		return this.getCurrentResult();
	}

	public boolean terminateEvolution() {
		boolean condition = this.getState() == EAlgorithmState.INACTIVE;
		if (this.getConfig().numberOfEvaluations() > 0) {
			condition = condition || this.getNumberOfEvaluations() >= this.getConfig().numberOfEvaluations();
		}
		if (this.getConfig().numberOfGenerations() > 0) {
			condition = condition || this.getNumberOfGenerationsEvolved() >= this.getConfig().numberOfGenerations();
		}
		if (this.getConfig().earlyTerminationGenerations() >= 0) {
			condition = condition || this.numberOfGenerationsWOChange >= this.getConfig().earlyTerminationGenerations();
		}
		return condition;
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
			this.logger.error("Encountered exception: {}", e);
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
