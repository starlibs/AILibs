package ai.libs.jaicore.ml.hpo.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.nd4j.linalg.primitives.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IEvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.ml.hpo.IComponentInstanceHPO;

public class SimpleGeneticAlgorithm extends AOptimizer<IComponentInstanceHPOGAInput, IEvaluatedSoftwareConfigurationSolution<Double>, Double> implements IComponentInstanceHPO<Double> {

	private Logger logger = LoggerFactory.getLogger(SimpleGeneticAlgorithm.class);
	private static final ISimpleGeneticAlgorithmConfig DEF_CONFIG = ConfigFactory.create(ISimpleGeneticAlgorithmConfig.class);

	private List<IIndividual> population;
	private Random rand;

	private int generation;
	private int generationsSinceLastBestUpdate;
	private AtomicInteger numEvaluations;

	private IGeneticOperator crossover;
	private IGeneticOperator mutation;

	public SimpleGeneticAlgorithm(final IComponentInstanceHPOGAInput input) {
		this(DEF_CONFIG, input);
	}

	public SimpleGeneticAlgorithm(final ISimpleGeneticAlgorithmConfig config, final IComponentInstanceHPOGAInput input) {
		super(config, input);
	}

	private boolean isEvolutionaryProcessTerminated() {
		if (this.getConfig().getMaxGenerations() > 0 && this.generation + 1 >= this.getConfig().getMaxGenerations()) {
			return true;
		}
		if (this.getConfig().getMaxEvaluations() > 0 && this.numEvaluations.get() >= this.getConfig().getMaxEvaluations()) {
			return true;
		}
		if (this.getConfig().getMaxEvaluations() > 0 && this.generationsSinceLastBestUpdate >= this.getConfig().getEarlyTermination()) {
			return true;
		}
		return false;
	}

	private void evaluateAll(final List<IIndividual> population) throws EvaluationBudgetExhaustedException {
		List<Runnable> populationEval = new ArrayList<>(population.size());
		AtomicBoolean budgetExhausted = new AtomicBoolean(false);
		Semaphore syncSemaphore = new Semaphore(0);

		for (IIndividual ind : population) {
			populationEval.add(() -> {
				try {
					if (this.numEvaluations.incrementAndGet() > this.getConfig().getMaxEvaluations() && this.getConfig().getMaxEvaluations() > 0) {
						budgetExhausted.set(true);
						return;
					}
					IComponentInstance ci = this.getInput().convertIndividualToComponentInstance(ind);

					double score = this.getInput().getEvaluator().evaluate(ci);
					ind.setObjective(0, score);

					// check whether we have a new best solution
					if (this.updateBestSeenSolution(new EvaluatedGASolution<>(ci, score))) {
						// reset generations since last best update counter
						this.generationsSinceLastBestUpdate = 0;
					}
				} catch (ObjectEvaluationFailedException e) {
					ind.setObjective(0, this.getConfig().getFailedEvalObjectiveValue());
				} catch (InterruptedException e) {
					ind.setObjective(0, this.getConfig().getFailedEvalObjectiveValue());
				} finally {
					syncSemaphore.release();
				}
			});
		}

		ExecutorService pool = Executors.newFixedThreadPool(this.getNumCPUs());
		populationEval.stream().forEach(pool::submit);
		pool.shutdown();

		try {
			syncSemaphore.acquire(population.size());
		} catch (InterruptedException e) {
			pool.shutdownNow();
		}

		if (budgetExhausted.get()) {
			throw new EvaluationBudgetExhaustedException();
		}
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			this.rand = new Random(this.getConfig().getSeed());
			this.crossover = new SimpleCrossover(this.rand);
			this.mutation = new SimpleMutation(this.rand, 0.1);
			this.generation = 0;
			this.generationsSinceLastBestUpdate = 0;

			// initialize population
			this.population = new ArrayList<>(this.getConfig().getPopulationSize());
			IntStream.range(0, this.getConfig().getPopulationSize()).mapToObj(x -> this.getInput().newRandomIndividual(this.rand)).forEach(this.population::add);

			// num evaluations counter
			this.numEvaluations = new AtomicInteger(0);

			return this.activate();
		case ACTIVE:
			Semaphore evoSem = new Semaphore(0);
			Thread evoThread = new Thread(() -> {
				try {
					while (!this.isEvolutionaryProcessTerminated()) {
						this.generation++;
						this.generationsSinceLastBestUpdate++;

						// evaluate population
						this.evaluateAll(this.population);

						// sort population
						Collections.sort(this.population);

						// start production of offspring
						List<IIndividual> offspring = new ArrayList<>();

						// maintain elite
						for (int i = 0; i < this.getConfig().getEliteSize(); i++) {
							offspring.add(this.population.get(i));
						}

						// check whether to perform a soft restart
						if (this.getConfig().getSoftRestart() > 0 && this.generationsSinceLastBestUpdate % this.getConfig().getSoftRestart() == 0) {
							while (offspring.size() < this.getConfig().getPopulationSize()) {
								offspring.add(this.getInput().newRandomIndividual(this.rand));
							}
						}
						// evolve
						else {
							while (offspring.size() < this.getConfig().getPopulationSize()) {
								// select parents
								IIndividual[] parents = new IIndividual[] { this.select(), this.select() };
								if (this.rand.nextDouble() < this.getConfig().getCrossoverRate()) {
									parents = this.crossover.apply(parents);
								}

								offspring.add(this.mutation.apply(parents[0])[0]);
								if (offspring.size() < this.getConfig().getPopulationSize()) {
									offspring.add(this.mutation.apply(parents[1])[0]);
								}
							}
						}

						// override population
						this.population.clear();
						this.population.addAll(offspring);
					}
				} catch (EvaluationBudgetExhaustedException e) {
					this.logger.info("Evaluation budget has been exhausted. Thus, terminate the algorithm.");
				} finally {
					evoSem.release();
				}
			});

			evoThread.start();

			if (this.getConfig().getMaxRuntimeInMS() > 0) {
				evoSem.tryAcquire(this.getConfig().getMaxRuntimeInMS(), TimeUnit.MILLISECONDS);
			} else {
				evoSem.acquire();
			}

			return this.terminate();
		default:
			throw new IllegalStateException("Illegal state for this algorithm to run anything.");
		}
	}

	public int getNumEvaluations() {
		return this.numEvaluations.get();
	}

	public int getCurrentGeneration() {
		return this.generation;
	}

	private IIndividual select() {
		List<IIndividual> tournamentList = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			tournamentList.add(this.population.get(this.rand.nextInt(this.population.size())));
		}
		Collections.sort(tournamentList);
		return tournamentList.get(0);
	}

	@Override
	public ISimpleGeneticAlgorithmConfig getConfig() {
		return (ISimpleGeneticAlgorithmConfig) super.getConfig();
	}

}
