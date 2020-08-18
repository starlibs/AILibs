package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.IPreferenceKernel;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class BootstrappingPreferenceKernel<N, A> implements IPreferenceKernel<N, A>, ILoggingCustomizable {

	private static final int MAXTIME_WARN_CREATERANKINGS = 1;

	private Logger logger = LoggerFactory.getLogger(BootstrappingPreferenceKernel.class);
	private final Map<N, Map<A, DoubleList>> observations = new HashMap<>();

	/* configuration units */
	private final IBootstrappingParameterComputer bootstrapParameterComputer;
	private final IBootstrapConfigurator bootstrapConfigurator;

	private final int maxNumSamplesInHistory;
	// private final int maxNumSamplesInBootstrap;
	// private final int numBootstrapsPerChild;

	private final Random random;
	private final Map<N, List<List<A>>> rankingsForNodes = new HashMap<>();
	private final int minSamplesToCreateRankings;

	public BootstrappingPreferenceKernel(final IBootstrappingParameterComputer bootstrapParameterComputer, final IBootstrapConfigurator bootstrapConfigurator, final Random random, final int minSamplesToCreateRankings,
			final int maxNumSamplesInHistory) {
		super();
		this.bootstrapParameterComputer = bootstrapParameterComputer;
		this.bootstrapConfigurator = bootstrapConfigurator;
		this.random = random;
		this.minSamplesToCreateRankings = minSamplesToCreateRankings;
		this.maxNumSamplesInHistory = maxNumSamplesInHistory;
	}

	public BootstrappingPreferenceKernel(final IBootstrappingParameterComputer bootstrapParameterComputer, final IBootstrapConfigurator bootstrapConfigurator, final int minSamplesToCreateRankings) {
		this(bootstrapParameterComputer, bootstrapConfigurator, new Random(0), minSamplesToCreateRankings, 1000);
	}

	@Override
	public void signalNewScore(final ILabeledPath<N, A> path, final double newScore) {

		/* add the observation to all stats on the path */
		List<N> nodes = path.getNodes();
		List<A> arcs = path.getArcs();
		int l = nodes.size();
		for (int i = 0; i < l - 1; i++) {
			N node = nodes.get(i);
			A arc = arcs.get(i);
			DoubleList list = this.observations.computeIfAbsent(node, n -> new HashMap<>()).computeIfAbsent(arc, a -> new DoubleArrayList());
			list.add(newScore);
			if (list.size() > this.maxNumSamplesInHistory) {
				list.removeDouble(0);
			}
		}
	}

	/**
	 * Computes new rankings from a fresh bootstrap
	 *
	 * @param node
	 * @param parameterComputer
	 * @return
	 */
	public List<List<A>> drawNewRankingsForActions(final N node, final Collection<A> actions, final IBootstrappingParameterComputer parameterComputer) {
		long start = System.currentTimeMillis();
		for (A action : actions) {
			if (!this.observations.containsKey(node) || !this.observations.get(node).containsKey(action)) {
				throw new IllegalArgumentException("No observations available for action " + action + ", cannot draw ranking.");
			}
		}
		Map<A, DoubleList> observationsPerAction = this.observations.get(node);
		final int numBootstraps = this.bootstrapConfigurator.getNumBootstraps(observationsPerAction);
		final int numSamplesInEachBootstrap = this.bootstrapConfigurator.getBootstrapSize(observationsPerAction);

		this.logger.debug("Now creating {} bootstraps (rankings)", numBootstraps);
		int totalObservations = 0;
		List<List<A>> rankings = new ArrayList<>(numBootstraps);
		for (int bootstrap = 0; bootstrap < numBootstraps; bootstrap++) {
			Map<A, Double> scorePerAction = new HashMap<>();
			totalObservations = 0;
			for (A action : actions) {
				DoubleList observedScoresForChild = observationsPerAction.get(action);
				totalObservations += observedScoresForChild.size();
				DescriptiveStatistics statsForThisChild = new DescriptiveStatistics();
				for (int sample = 0; sample < numSamplesInEachBootstrap; sample++) {
					statsForThisChild.addValue(SetUtil.getRandomElement(observedScoresForChild, this.random));
				}
				scorePerAction.put(action, parameterComputer.getParameter(statsForThisChild));
			}
			List<A> ranking = actions.stream().sorted((a1, a2) -> Double.compare(scorePerAction.get(a1), scorePerAction.get(a2))).collect(Collectors.toList());
			rankings.add(ranking);
		}
		long runtime = System.currentTimeMillis() - start;
		if (runtime > MAXTIME_WARN_CREATERANKINGS) {
			this.logger.warn("Creating the {} rankings took {}ms for {} options and {} total observations, which is more than the allowed {}ms!", numBootstraps, runtime, actions.size(), totalObservations, MAXTIME_WARN_CREATERANKINGS);
		}
		return rankings;
	}

	@Override
	public List<List<A>> getRankingsForActions(final N node, final Collection<A> actions) {
		this.rankingsForNodes.put(node, this.drawNewRankingsForActions(node, actions, this.bootstrapParameterComputer));
		return this.rankingsForNodes.get(node);
	}

	@Override
	public boolean canProduceReliableRankings(final N node, final Collection<A> actions) {
		int minObservations = Integer.MAX_VALUE;
		if (!this.observations.containsKey(node)) {
			return false;
		}
		Map<A, DoubleList> scoresPerAction = this.observations.get(node);
		for (A action : actions) {
			if (!scoresPerAction.containsKey(action)) {
				this.logger.debug("Refusing production of rankings, because are no observations for {}.", minObservations, this.minSamplesToCreateRankings);
				return false;
			}
			int numSamples = scoresPerAction.get(action).size();
			if (numSamples < this.minSamplesToCreateRankings) {
				this.logger.debug("Refusing production of rankings, because there are only {} observations for action {}, which is less than the required number of {}.", numSamples, action, this.minSamplesToCreateRankings);
				return false;
			}
		}
		this.logger.debug("Enough examples. Allowing the construction of rankings.");
		return true;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void clearKnowledge(final N node) {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Removing {} observations and {} rankings for node.", this.observations.get(node).values().stream().map(l -> l.size()).reduce((a, b) -> a + b).get(), this.rankingsForNodes.get(node).size());
		}
		this.observations.remove(node);
		this.rankingsForNodes.remove(node);
	}
}
