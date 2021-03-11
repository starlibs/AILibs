package ai.libs.jaicore.ml.hpo.ggp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.attributedobjects.ScoredItem;
import org.epochx.gr.op.crossover.WhighamCrossover;
import org.epochx.gr.op.init.GrowInitialiser;
import org.epochx.gr.op.mutation.WhighamMutation;
import org.epochx.gr.representation.GRCandidateProgram;
import org.epochx.representation.CandidateProgram;
import org.epochx.tools.grammar.Grammar;
import org.epochx.tools.random.MersenneTwisterFast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.StatisticsUtil;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;
import ai.libs.jaicore.ml.hpo.ggp.GrammarBasedGeneticProgramming.GGPSolutionCandidate;

/**
 * Grammar-based genetic programming is an evolutionary algorithm capable of evolving individuals in the form of trees, where the trees are derived from a context-free grammar (CFG).
 * As in standard evolutionary algorithms (EA), a population is maintained that is evaluated by means of a fitness function and subsequently individuals are recombined with each
 * other and mutated. In contrast to standard EAs, special operators need to be applied to adhere to the rules imposed by the CFG and therefore to not invalidate individuals.<br>
 * <br>
 * This specific implementation allows for both limiting the execution by number of evaluations (generations*population_size) or a timeout. If the number of generations is set to an
 * infeasible value, it will be assumed that the number of generations is set to infinity and thus a timeout will be required. As soon as the timeout is hit, the best solution seen so far
 * will be returned. If the timeout is set to an infeasible value, the number of generations will be a required configuration value. When running the algorithm, it will run to the specified
 * number of generation and return the best result found until this point. If both stopping criterions are set, the algorithm will terminate with the criterion that is first evaluated to true.
 * So, either the timeout is hit or the maximum number of generations reached.<br>
 * <br>
 * <b>Early Stopping:</b>
 * If the algorithm is already converged early, i.e., before the timeout is hit or the maximum number of generations is reached, it will terminate if the algorithm has not seen any improvements
 * for a specified number of generations.<br>
 * <br>
 * <b>Random Restart:</b>
 * Sometimes the algorithm may get stuck in local optima. To overcome this issue and move away from the local optima, there is a soft reset option called random restart. If random restart is
 * configured, every x generations the algorithm could not find any improvement, the current population is wiped except for the best individuals (defined via the elitism config). Then, the
 * population is filled up with new randomly sampled individuals. Thereby, the algorithm is able to move to other areas of the search space easily (given there are better fitness values).
 *
 * @author mwever
 *
 */
public class GrammarBasedGeneticProgramming extends AOptimizer<SoftwareConfigurationProblem<Double>, GGPSolutionCandidate, Double> {

	private static final IGrammarBasedGeneticProgrammingConfig DEF_CONFIG = ConfigFactory.create(IGrammarBasedGeneticProgrammingConfig.class);
	private static final Logger LOGGER = LoggerFactory.getLogger(GrammarBasedGeneticProgramming.class);

	private final MersenneTwisterFast rng;
	private final IObjectEvaluator<IComponentInstance, Double> evaluator;
	private final CFGConverter converter;

	private final Map<String, Double> cacheMap = new HashMap<>();
	private Grammar grammar;

	private AtomicInteger earlyStoppingCounter = new AtomicInteger(0);

	public class GGPSolutionCandidate implements ScoredItem<Double> {

		private IComponentInstance ci;
		private final Double score;

		private GGPSolutionCandidate(final IComponentInstance ci, final Double score) {
			this.ci = ci;
			this.score = score;
		}

		public IComponentInstance getComponentInstance() {
			return this.ci;
		}

		@Override
		public Double getScore() {
			return this.score;
		}

	}

	public GrammarBasedGeneticProgramming(final SoftwareConfigurationProblem<Double> input) {
		this(DEF_CONFIG, input);
	}

	public GrammarBasedGeneticProgramming(final IOwnerBasedAlgorithmConfig config, final SoftwareConfigurationProblem<Double> input) {
		super(config, input);
		this.rng = new MersenneTwisterFast(42);
		this.evaluator = input.getCompositionEvaluator();
		this.converter = new CFGConverter(input.getComponents(), input.getRequiredInterface());
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			LOGGER.info("Setup GrammarBasedGeneticProgramming algorithm.");
			return this.activate();
		case ACTIVE:
			LOGGER.info("Start GrammarBasedGeneticProgramming run");
			Semaphore finished = new Semaphore(0);

			Thread evoThread = new Thread(() -> {
				try {
					String grammarString = GrammarBasedGeneticProgramming.this.converter.toGrammar();
					LOGGER.debug("Generated the following grammar string for the provided component repository:\n{}", grammarString);

					GrammarBasedGeneticProgramming.this.grammar = new Grammar(grammarString);

					// For initializing the population.
					GrowInitialiser initPop = new GrowInitialiser(GrammarBasedGeneticProgramming.this.rng, GrammarBasedGeneticProgramming.this.grammar, GrammarBasedGeneticProgramming.this.getConfig().getPopulationSize(),
							GrammarBasedGeneticProgramming.this.getConfig().getMaxDepth(), false);

					// Set initial population list and sort the population
					List<CandidateProgram> population = new ArrayList<>(initPop.getInitialPopulation());

					int g = 0;
					while (!Thread.currentThread().isInterrupted() && (GrammarBasedGeneticProgramming.this.getConfig().getNumGenerations() <= 0 || g < GrammarBasedGeneticProgramming.this.getConfig().getNumGenerations())) {
						LOGGER.debug("Evaluate population of generation {}.", (g + 1));
						GrammarBasedGeneticProgramming.this.evaluate(population);
						Collections.sort(population);

						// print fitness statistics of current generation if enabled.
						if (GrammarBasedGeneticProgramming.this.getConfig().getPrintFitnessStats()) {
							List<Double> fitnessList = population.stream().map(x -> ((GRCandidateProgram) x).getFitnessValue()).collect(Collectors.toList());
							LOGGER.info("Generation #{} (population size: {}) - min: {} - mean: {} - max: {}", g + 1, fitnessList.size(), StatisticsUtil.min(fitnessList), StatisticsUtil.mean(fitnessList), StatisticsUtil.max(fitnessList));
						}

						// if early termination is activated and the number of generations without change exceeds the configured generations, stop the evolution
						int generationsWithoutImprovementCounter = GrammarBasedGeneticProgramming.this.earlyStoppingCounter.getAndIncrement();
						if (GrammarBasedGeneticProgramming.this.getConfig().getEarlyStopping() >= 1 && generationsWithoutImprovementCounter > GrammarBasedGeneticProgramming.this.getConfig().getEarlyStopping()) {
							LOGGER.info("Best candidate did not change for {} generations: Thus, stop early.", GrammarBasedGeneticProgramming.this.getConfig().getEarlyStopping());
							break;
						}

						List<CandidateProgram> offspring = new ArrayList<>(GrammarBasedGeneticProgramming.this.getConfig().getPopulationSize());
						// keep elite
						for (int i = 0; i < GrammarBasedGeneticProgramming.this.getConfig().getElitismSize(); i++) {
							offspring.add(population.get(i));
						}

						// check whether thread has been interrupted. If so, terminate the algorithm.
						if (Thread.interrupted()) {
							LOGGER.debug("Thread got interrupted, exit GGP.");
							throw new InterruptedException();
						}

						// if enabled, perform random restart every x generations
						if (GrammarBasedGeneticProgramming.this.getConfig().getRandomRestart() > 0 && ((generationsWithoutImprovementCounter) % GrammarBasedGeneticProgramming.this.getConfig().getRandomRestart() == 0)) {
							LOGGER.debug("It is about time to perform a random restart in generation {}. Randomly generate {} individuals for restart.", g,
									GrammarBasedGeneticProgramming.this.getConfig().getPopulationSize() - GrammarBasedGeneticProgramming.this.getConfig().getElitismSize());
							GrowInitialiser randRestartPop = new GrowInitialiser(GrammarBasedGeneticProgramming.this.rng, GrammarBasedGeneticProgramming.this.grammar,
									GrammarBasedGeneticProgramming.this.getConfig().getPopulationSize() - GrammarBasedGeneticProgramming.this.getConfig().getElitismSize(), GrammarBasedGeneticProgramming.this.getConfig().getMaxDepth(),
									false);
							offspring.addAll(randRestartPop.getInitialPopulation());
						} else {
							// fill up offspring with recombinations
							while (offspring.size() < GrammarBasedGeneticProgramming.this.getConfig().getPopulationSize()) {
								if (Thread.interrupted()) {
									throw new InterruptedException();
								}

								CandidateProgram child1 = GrammarBasedGeneticProgramming.this.tournament(population).clone();
								CandidateProgram child2 = GrammarBasedGeneticProgramming.this.tournament(population).clone();

								double randomX = GrammarBasedGeneticProgramming.this.rng.nextDouble();
								if (randomX < GrammarBasedGeneticProgramming.this.getConfig().getCrossoverRate()) {
									WhighamCrossover xover = new WhighamCrossover(GrammarBasedGeneticProgramming.this.rng);
									CandidateProgram[] xoverprograms = xover.crossover(child1.clone(), child2.clone());
									if (xoverprograms != null) {
										child1 = xoverprograms[0];
										child2 = xoverprograms[1];
									}
								}

								child1 = GrammarBasedGeneticProgramming.this.mutate(child1);
								child2 = GrammarBasedGeneticProgramming.this.mutate(child2);

								offspring.add(child1);
								if (offspring.size() < GrammarBasedGeneticProgramming.this.getConfig().getPopulationSize()) {
									offspring.add(child2);
								}
							}
						}
						population = offspring;
						g++;
					}
				} catch (InterruptedException e) {
					LOGGER.debug("GGP thread got interrupted, release semaphore and shutdown.");
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					LOGGER.error("Unexpected exception occurred and forced GGP to terminate.", e);
					e.printStackTrace();
				} finally {
					finished.release();
				}
			});
			evoThread.start();

			try {
				if (this.getConfig().getTimeout().milliseconds() > 0) { // a timeout is specified, thus, finish with what occurs first: timeout or max generations reached
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Wait for {} ms", this.getConfig().getTimeout().milliseconds());
					}
					boolean acquired = finished.tryAcquire(this.getConfig().getTimeout().milliseconds(), TimeUnit.MILLISECONDS);
					if (!acquired) {
						LOGGER.debug("Timeout occurred for evo thread. Now shut it down.");
						evoThread.interrupt();
					}
				} else { // no timeout configured: wait until max generations are reached.
					finished.acquire();
				}
			} catch (InterruptedException e) {
				LOGGER.debug("Main GGP thread got interrupted, now interrupt evoThread.");
				evoThread.interrupt();
			}
			return this.terminate();
		default:
			throw new IllegalStateException("Illegal state for this algorithm to run anything.");
		}
	}

	private CandidateProgram mutate(final CandidateProgram program) {
		CandidateProgram mutated = program.clone();
		if (this.rng.nextDouble() < this.getConfig().getMutationRate()) {
			WhighamMutation mutation = new WhighamMutation(this.rng);
			mutated = mutation.mutate(mutated);
		}
		return mutated;
	}

	private CandidateProgram tournament(final List<CandidateProgram> population) {
		List<CandidateProgram> candidates = new ArrayList<>(population);
		Collections.shuffle(candidates, new Random(this.rng.nextLong()));
		List<CandidateProgram> tournamentCandidates = IntStream.range(0, this.getConfig().getTournamentSize()).mapToObj(candidates::get).collect(Collectors.toList());
		Collections.sort(tournamentCandidates);
		return tournamentCandidates.get(0);
	}

	private void evaluate(final List<CandidateProgram> population) throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(this.getConfig().cpus());
		AtomicBoolean interrupted = new AtomicBoolean(false);
		Semaphore semaphore = new Semaphore(0);

		try {
			for (CandidateProgram individual : population) {
				if (this.cacheMap.containsKey(individual.toString())) {
					((GRCandidateProgram) individual).setFitnessValue(this.cacheMap.get(individual.toString()));
					semaphore.release();
				} else {
					Runnable evaluateTask = () -> {
						GRCandidateProgram realInd = ((GRCandidateProgram) individual);
						try {
							if (Thread.interrupted() || interrupted.get()) {
								throw new InterruptedException();
							}

							ComponentInstance ci = GrammarBasedGeneticProgramming.this.converter.grammarStringToComponentInstance(individual.toString());
							double fitnessValue = GrammarBasedGeneticProgramming.this.evaluator.evaluate(ci);
							if (GrammarBasedGeneticProgramming.this.updateBestSeenSolution(new GGPSolutionCandidate(ci, fitnessValue))) {
								GrammarBasedGeneticProgramming.this.earlyStoppingCounter.set(0);
							}
							realInd.setFitnessValue(fitnessValue);
						} catch (ObjectEvaluationFailedException | InterruptedException e) {
							realInd.setFitnessValue(GrammarBasedGeneticProgramming.this.getConfig().getFailedEvaluationScore());
							if (e instanceof InterruptedException) {
								Thread.currentThread().interrupt();
							}
						} catch (Exception e) {
							LOGGER.warn("Could not evaluate individual {}", individual, e);
						} finally {
							semaphore.release();
						}
					};
					pool.submit(evaluateTask);
				}
			}

			// wait for all the evaluations to finish.
			semaphore.acquire(population.size());

			// try to put the fitness score into the cache to not re-evaluate any candidate solutions
			population.stream().forEach(x -> {
				try {
					this.cacheMap.put(x.toString(), x.getFitness());
				} catch (Exception e) {
					// could not cache fitness => nullpointer exception must have occurred, so ignore the candidate and do not put it into the cache.
				}
			});
		} catch (InterruptedException e) {
			LOGGER.debug("Got interrupted while evaluating population. Shutdown task now.");
			interrupted.set(true);
			pool.shutdownNow();
			throw e;
		}
	}

	@Override
	public IGrammarBasedGeneticProgrammingConfig getConfig() {
		return (IGrammarBasedGeneticProgrammingConfig) super.getConfig();
	}

}
