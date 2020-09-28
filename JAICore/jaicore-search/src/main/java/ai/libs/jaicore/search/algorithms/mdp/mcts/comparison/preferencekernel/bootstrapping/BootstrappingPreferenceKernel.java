package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IRelaxedEventEmitter;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graphvisualizer.events.graph.NodePropertyChangedEvent;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.IPreferenceKernel;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class BootstrappingPreferenceKernel<N, A> implements IPreferenceKernel<N, A>, ILoggingCustomizable, IRelaxedEventEmitter {

	private static final int MAXTIME_WARN_CREATERANKINGS = 1;

	private Logger logger = LoggerFactory.getLogger(BootstrappingPreferenceKernel.class);

	private final EventBus eventBus = new EventBus();
	private boolean hasListeners = false;

	/* kernel state variables */
	private final Set<N> activeNodes = new HashSet<>(); // the nodes for which we memorize the observations
	private final Map<N, Map<A, DoubleList>> observations = new HashMap<>();
	private final Map<N, Map<A, Double>> bestObservationForAction = new HashMap<>();

	/* configuration units */
	private final IBootstrappingParameterComputer bootstrapParameterComputer;
	private final IBootstrapConfigurator bootstrapConfigurator;

	private final int maxNumSamplesInHistory;
	private final Random random;
	private final Map<N, List<List<A>>> rankingsForNodes = new HashMap<>();
	private final int minSamplesToCreateRankings = 1;

	/* stats */
	private int erasedObservationsInTotal = 0;

	public BootstrappingPreferenceKernel(final IBootstrappingParameterComputer bootstrapParameterComputer, final IBootstrapConfigurator bootstrapConfigurator, final Random random, final int minSamplesToCreateRankings,
			final int maxNumSamplesInHistory) {
		super();
		this.bootstrapParameterComputer = bootstrapParameterComputer;
		this.bootstrapConfigurator = bootstrapConfigurator;
		this.random = random;
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
			Map<A, Double> bestMap = this.bestObservationForAction.computeIfAbsent(node, n -> new HashMap<>());
			bestMap.put(arc, Math.min(newScore, bestMap.computeIfAbsent(arc, a -> Double.MAX_VALUE)));
			if (list.size() > this.maxNumSamplesInHistory) {
				list.removeDouble(0);
			}
			this.logger.debug("Updated observations for action {} in node {}. New list of observations is: {}", arc, node, list);
			if (!this.activeNodes.contains(node)) {
				this.logger.info("The current node has not been marked active and hence, we abort the update procedure saving {} entries.", l - i);
				return;
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
		final int numSamplesPerChildInEachBootstrap = this.bootstrapConfigurator.getBootstrapSizePerChild(observationsPerAction);

		this.logger.debug("Now creating {} bootstraps (rankings)", numBootstraps);
		int totalObservations = 0;
		List<List<A>> rankings = new ArrayList<>(numBootstraps);
		for (int bootstrap = 0; bootstrap < numBootstraps; bootstrap++) {
			Map<A, Double> scorePerAction = new HashMap<>();
			totalObservations = 0;
			for (A action : actions) {
				DoubleList observedScoresForChild = observationsPerAction.get(action);
				totalObservations += observedScoresForChild.size();
				double bestObservation = this.bestObservationForAction.get(node).get(action);
				DescriptiveStatistics statsForThisChild = new DescriptiveStatistics();
				statsForThisChild.addValue(bestObservation); // always ensure that the best value is inside the bootstrap
				for (int sample = 0; sample < numSamplesPerChildInEachBootstrap - 1; sample++) {
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

		/* first check that there is any information about the node */
		if (!this.observations.containsKey(node)) {
			if (this.hasListeners) {
				this.eventBus.post(new NodePropertyChangedEvent<>(null, node, "plkernelstatus", 0.0));
			}
			this.logger.info("No observations for node yet, not allowing to produce rankings.");
			return false;
		}

		/* now check that the minimum number of samples per node is available */
		Map<A, DoubleList> scoresPerAction = this.observations.get(node);
		for (A action : actions) {
			if (!scoresPerAction.containsKey(action) || scoresPerAction.get(action).size() < this.minSamplesToCreateRankings) {
				this.logger.info("Refusing production of rankings, because are less than {} observations for action {}.", this.minSamplesToCreateRankings, action);
				if (this.hasListeners) {
					this.eventBus.post(new NodePropertyChangedEvent<>(null, node, "plkernelstatus", 0.0));
				}
				return false;
			}

		}
		this.logger.debug("Enough examples. Allowing the construction of rankings.");
		if (this.hasListeners) {
			this.eventBus.post(new NodePropertyChangedEvent<>(null, node, "plkernelstatus", 1.0));
		}
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
		if (!this.observations.containsKey(node) || this.observations.get(node).isEmpty()) {
			return;
		}
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Removing {} observations.", this.observations.get(node).values().stream().map(l -> l.size()).reduce((a, b) -> a + b).get());
		}
		this.erasedObservationsInTotal += this.observations.get(node).size();
		this.observations.remove(node);

		if (this.logger.isInfoEnabled() && this.rankingsForNodes.containsKey(node)) {
			this.logger.info("Removing {} rankings.", this.rankingsForNodes.get(node).size());
		}
		this.rankingsForNodes.remove(node);
	}

	public Map<A, DoubleList> getObservations(final N node) {
		return this.observations.get(node);
	}

	public Set<N> getActiveNodes() {
		return this.activeNodes;
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
		this.hasListeners = true;
	}

	@Override
	public void signalNodeActiveness(final N node) {
		this.activeNodes.add(node);
	}

	@Override
	public int getErasedObservationsInTotal() {
		return this.erasedObservationsInTotal;
	}

	/**
	 * Returns the action that has least observations
	 */
	@Override
	public A getMostImportantActionToObtainApplicability(final N node, final Collection<A> actions) {
		Map<A, DoubleList> obsForNode = this.observations.get(node);
		A leastTriedAction = null;
		int minAttempts = Integer.MAX_VALUE;
		for (A action : actions) {
			int attempts = obsForNode.containsKey(action) ? obsForNode.get(action).size() : 0;
			if (attempts < minAttempts) {
				minAttempts = attempts;
				leastTriedAction = action;
			}
		}
		return leastTriedAction;
	}
}
