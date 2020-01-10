package ai.libs.jaicore.ml.ranking.dyad.learner.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DenseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.AbstractDyadScaler;

/**
 * A queue whose elements are nodes, sorted by a dyad ranker.
 *
 * @author Helena Graf
 *
 * @param <N>
 *            First node parameter
 * @param <V>
 *            Second node parameter
 */
public abstract class ADyadRankedNodeQueue<N, V extends Comparable<V>> implements Queue<IEvaluatedPath<N, ?, V>> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** the dyad ranker used to rank the nodes */
	private IDyadRanker dyadRanker;

	/** for scaling the dyads */
	protected AbstractDyadScaler scaler;

	private boolean useScaler = false;

	/** the actual queue of nodes */
	private List<IEvaluatedPath<N, ?, V>> queue = new ArrayList<>();

	/**
	 * characterizations of the nodes (scaled) (not ordered the same way as the
	 * nodes!)
	 */
	private List<IVector> nodeCharacterizations = new ArrayList<>();

	/** unscaled (original) characterization of the nodes */
	private IVector originalContextCharacterization;

	/** characterization of the context the nodes are ranked in */
	private IVector contextCharacterization;

	private List<IDyad> queryDyads = new ArrayList<>();

	/** connects nodes to their respective characterizations */
	private BiMap<IEvaluatedPath<N, ?, V>, IVector> nodesAndCharacterizationsMap = HashBiMap.create();

	/**
	 * Constructs a new DyadRankedNodeQueue that ranks the nodes in the queue
	 * according to the given context characterization.
	 *
	 * @param contextCharacterization
	 *            the characterization of the context the nodes are ranked in
	 */
	public ADyadRankedNodeQueue(final IVector contextCharacterization) {
		this.contextCharacterization = contextCharacterization.addConstantToCopy(0);
		this.originalContextCharacterization = contextCharacterization;
		this.logger.trace("Construct ADyadNodeQueue with contexcharacterization {}", contextCharacterization);
	}

	/**
	 * Constructs a new DyadRankedNodeQueue that ranks the nodes in the queue
	 * according to the given context characterization and given dyad ranker. Given
	 * dyad ranker must be pre-trained.
	 *
	 * @param contextCharacterization
	 *            the characterization of the context the nodes are ranked in
	 * @param dyadRanker
	 *            the dyad ranker to be used for the ranking of the nodes
	 */
	public ADyadRankedNodeQueue(final IVector contextCharacterization, final IDyadRanker dyadRanker, final AbstractDyadScaler scaler) {
		this(contextCharacterization);
		this.dyadRanker = dyadRanker;
		this.scaler = scaler;
		if (scaler != null) {
			this.useScaler = true;
			// transform dataset
			this.transformContextCharacterization();
		}
	}

	/**
	 * Provide a characterization of the given node to be used by the dyad ranker.
	 *
	 * @param node
	 *            the node to be characterized
	 * @return the characterization of the node
	 */
	protected abstract IVector characterize(IEvaluatedPath<N, ?, V> node);

	@Override
	public int size() {
		return this.queue.size();
	}

	@Override
	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return this.queue.contains(o);
	}

	@Override
	public Iterator<IEvaluatedPath<N, ?, V>> iterator() {
		return this.queue.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.queue.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return this.queue.toArray(a);
	}

	@Override
	public boolean remove(final Object o) {
		if (o instanceof IEvaluatedPath<?, ?, ?>) {
			int index = -1;
			for (int i = 0; i < this.queue.size(); i++) {
				if (this.queue.get(i).equals(o)) {
					index = i;
				}
			}

			if (index != -1) {
				this.removeNodeAtPosition(index);
				return true;
			}
			return false;

		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return this.queue.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends IEvaluatedPath<N, ?, V>> c) {
		this.logger.trace("Add {} nodes", c.size());
		boolean changed = false;

		for (IEvaluatedPath<N, ?, V> elem : c) {
			if (this.add(elem)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;

		for (Object o : c) {
			if (this.remove(o)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		this.queue.clear();
		this.nodesAndCharacterizationsMap.clear();
		this.nodeCharacterizations.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(final IEvaluatedPath<N, ?, V> e) {
		if (this.queue.contains(e)) {
			return true;
		} else if (e != null) {
			try {
				this.logger.debug("Add node to OPEN.");
				// characterize new node
				IVector characterization = this.characterize(e);
				this.nodeCharacterizations.add(characterization);

				Dyad newDyad = new Dyad(this.contextCharacterization, characterization);
				this.queryDyads.add(newDyad);

				if (this.useScaler) {
					// scale node
					DyadRankingDataset dataset = new DyadRankingDataset();
					dataset.add(new DenseDyadRankingInstance(Arrays.asList(newDyad)));
					this.scaler.transformAlternatives(dataset);
				}

				this.replaceNaNByZeroes(characterization);

				// add new pairing of node and characterization
				this.nodesAndCharacterizationsMap.put(e, characterization);

				// predict new ranking and reorder queue accordingly
				IPrediction prediction = this.dyadRanker.predict(new DenseDyadRankingInstance(this.queryDyads));
				this.queue.clear();
				for (int i = 0; i < ((IRanking<Dyad>) prediction.getPrediction()).size(); i++) {
					IEvaluatedPath<N, ?, V> toAdd = this.nodesAndCharacterizationsMap.inverse().get(((IRanking<Dyad>) prediction.getPrediction()).get(i).getAlternative());
					if (toAdd != null) {
						this.queue.add(toAdd);
					} else {
						this.logger.warn("Got a node in a prediction that doesnt exist");
					}
				}
				return true;
			} catch (PredictionException e1) {
				this.logger.warn("Failed to characterize: {}", e1.getLocalizedMessage());
				// remove unneeded characterization (ranking has failed)
				this.nodeCharacterizations.remove(this.nodeCharacterizations.size() - 1);
				return false;
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
				return false;
			}
		} else {
			return false;
		}
	}

	private void replaceNaNByZeroes(final IVector vector) {
		for (int i = 0; i < vector.length(); i++) {
			if (Double.isNaN(vector.getValue(i))) {
				vector.setValue(i, 0);
			}
		}
	}

	@Override
	public boolean offer(final IEvaluatedPath<N, ?, V> e) {
		return this.add(e);
	}

	@Override
	public IEvaluatedPath<N, ?, V> remove() {
		return this.removeNodeAtPosition(0);
	}

	public IEvaluatedPath<N, ?, V> removeNodeAtPosition(final int i) {
		IEvaluatedPath<N, ?, V> removedNode = this.queue.remove(i);
		this.logger.trace("Retrieve node from OPEN. Index: {}", i);
		this.nodeCharacterizations.remove(this.nodesAndCharacterizationsMap.get(removedNode));
		IVector removedAlternative = this.nodesAndCharacterizationsMap.remove(removedNode);

		int index = -1;
		for (int j = 0; j < this.queryDyads.size(); j++) {
			if (this.queryDyads.get(j).getAlternative().equals(removedAlternative)) {
				index = j;
				break;
			}
		}

		if (index >= -1) {
			this.queryDyads.remove(index);
		}

		return removedNode;
	}

	@Override
	public IEvaluatedPath<N, ?, V> poll() {
		if (!this.queue.isEmpty()) {
			return this.remove();
		}
		return null;
	}

	@Override
	public IEvaluatedPath<N, ?, V> element() {
		return this.queue.get(0);
	}

	@Override
	public IEvaluatedPath<N, ?, V> peek() {
		if (!this.queue.isEmpty()) {
			this.logger.trace("Peek from OPEN.");
			return this.element();
		}
		return null;
	}

	/**
	 * Get the dyad ranker used to rank the nodes.
	 *
	 * @return the dyad ranker
	 */
	public IDyadRanker getDyadRanker() {
		return this.dyadRanker;
	}

	/**
	 * Set which dyad ranker shall be used to rank the nodes. It is not trained in
	 * this class, so it must be pre-trained before setting it as a dyad ranker for
	 * this queue!
	 *
	 * @param dyadRanker
	 *            the dyad ranker
	 */
	public void setDyadRanker(final IDyadRanker dyadRanker) {
		this.logger.trace("Update dyad ranker. Was {} now is {}", this.dyadRanker.getClass(), dyadRanker.getClass());
		this.dyadRanker = dyadRanker;
	}

	public AbstractDyadScaler getScaler() {
		return this.scaler;
	}

	public void setScaler(final AbstractDyadScaler scaler) {
		if (this.useScaler) {
			this.logger.trace("Update scaler. Was {} now is {}", this.scaler.getClass(), scaler.getClass());
		} else {
			this.logger.trace("Now using scaler {}.", scaler.getClass());
			this.useScaler = true;
		}

		this.scaler = scaler;

		// transform dataset
		this.contextCharacterization = this.originalContextCharacterization.addConstantToCopy(0);
		this.transformContextCharacterization();
	}

	private void transformContextCharacterization() {
		this.logger.trace("Transform context characterization with scaler {}", this.scaler.getClass());
		Dyad dyad = new Dyad(this.contextCharacterization, this.contextCharacterization);
		DyadRankingDataset dataset = new DyadRankingDataset();
		DenseDyadRankingInstance instance = new DenseDyadRankingInstance(Arrays.asList(dyad));
		dataset.add(instance);
		this.scaler.transformInstances(dataset);
	}

}
