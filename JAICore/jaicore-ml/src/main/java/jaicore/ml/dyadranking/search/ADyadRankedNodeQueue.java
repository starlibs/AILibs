package jaicore.ml.dyadranking.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.IDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.util.AbstractDyadScaler;
import jaicore.search.model.travesaltree.Node;

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
public abstract class ADyadRankedNodeQueue<N, V extends Comparable<V>> implements Queue<Node<N, V>> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** the dyad ranker used to rank the nodes */
	private IDyadRanker dyadRanker;

	/** for scaling the dyads */
	protected AbstractDyadScaler scaler;

	private boolean useScaler = false;

	/** the actual queue of nodes */
	private List<Node<N, V>> queue = new ArrayList<>();

	/**
	 * characterizations of the nodes (scaled) (not ordered the same way as the
	 * nodes!)
	 */
	private List<Vector> nodeCharacterizations = new ArrayList<>();

	/** unscaled (original) characterization of the nodes */
	private Vector originalContextCharacterization;

	/** characterization of the context the nodes are ranked in */
	private Vector contextCharacterization;

	private List<Dyad> queryDyads = new ArrayList<>();

	/** connects nodes to their respective characterizations */
	private BiMap<Node<N, V>, Vector> nodesAndCharacterizationsMap = HashBiMap.create();

	/**
	 * Constructs a new DyadRankedNodeQueue that ranks the nodes in the queue
	 * according to the given context characterization.
	 * 
	 * @param contextCharacterization
	 *            the characterization of the context the nodes are ranked in
	 */
	public ADyadRankedNodeQueue(Vector contextCharacterization) {
		this.contextCharacterization = contextCharacterization.addConstantToCopy(0);
		this.originalContextCharacterization = contextCharacterization;
		logger.trace("Construct ADyadNodeQueue with contexcharacterization {}", contextCharacterization);
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
	public ADyadRankedNodeQueue(Vector contextCharacterization, IDyadRanker dyadRanker, AbstractDyadScaler scaler) {
		this(contextCharacterization);
		this.dyadRanker = dyadRanker;
		this.scaler = scaler;
		if (scaler != null) {
			useScaler = true;
			// transform dataset
			transformContextCharacterization();
		}
	}

	/**
	 * Provide a characterization of the given node to be used by the dyad ranker.
	 * 
	 * @param node
	 *            the node to be characterized
	 * @return the characterization of the node
	 */
	protected abstract Vector characterize(Node<N, V> node);

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public Iterator<Node<N, V>> iterator() {
		return queue.iterator();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return queue.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Node<?, ?>) {
			int index = -1;
			for (int i = 0; i < queue.size(); i++) {
				if (queue.get(i).equals(o)) {
					index = i;
				}
			}

			if (index != -1) {
				removeNodeAtPosition(index);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return queue.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Node<N, V>> c) {
		logger.trace("Add {} nodes", c.size());
		boolean changed = false;

		for (Node<N, V> elem : c) {
			if (this.add(elem)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;

		for (Object o : c) {
			if (this.remove(o)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		queue.clear();
		nodesAndCharacterizationsMap.clear();
		nodeCharacterizations.clear();
	}

	@Override
	public boolean add(Node<N, V> e) {
		if (queue.contains(e)) {
			return true;
		} else if (e != null) {
			try {
				logger.debug("Add node to OPEN, is Goal: {}", e.isGoal());
				// characterize new node
				Vector characterization = characterize(e);
				nodeCharacterizations.add(characterization);

				Dyad newDyad = new Dyad(contextCharacterization, characterization);
				queryDyads.add(newDyad);

				if (useScaler) {
					// scale node
					DyadRankingDataset dataset = new DyadRankingDataset();
					dataset.add(new DyadRankingInstance(Arrays.asList(newDyad)));
					scaler.transformAlternatives(dataset);
				}

				replaceNaNByZeroes(characterization);

				// add new pairing of node and characterization
				nodesAndCharacterizationsMap.put(e, characterization);

				// predict new ranking and reorder queue accordingly
				IDyadRankingInstance prediction = dyadRanker.predict(new DyadRankingInstance(queryDyads));
				queue.clear();
				for (int i = 0; i < prediction.length(); i++) {
					Node<N, V> toAdd = nodesAndCharacterizationsMap.inverse()
							.get(prediction.getDyadAtPosition(i).getAlternative());
					if (toAdd != null) {
						queue.add(toAdd);
					} else {
						logger.warn("Got a node in a prediction that doesnt exist");
					}
				}

				return true;
			} catch (PredictionException e1) {
				logger.warn("Failed to characterize: {}", e1.getLocalizedMessage());
				// remove unneeded characterization (ranking has failed)
				nodeCharacterizations.remove(nodeCharacterizations.size() - 1);
				return false;
			}
		} else {
			return false;
		}
	}

	private void replaceNaNByZeroes(Vector vector) {
		for (int i = 0; i < vector.length(); i++) {
			if (Double.isNaN(vector.getValue(i))) {
				vector.setValue(i, 0);
			}
		}
	}

	@Override
	public boolean offer(Node<N, V> e) {
		return this.add(e);
	}

	@Override
	public Node<N, V> remove() {
		return removeNodeAtPosition(0);
	}

	public Node<N, V> removeNodeAtPosition(int i) {
		Node<N, V> removedNode = queue.remove(i);
		logger.trace("Retrieve node from OPEN. Is goal: {}, Index: {}", removedNode.isGoal(), i);
		nodeCharacterizations.remove(nodesAndCharacterizationsMap.get(removedNode));
		Vector removedAlternative = nodesAndCharacterizationsMap.remove(removedNode);

		int index = -1;
		for (int j = 0; j < queryDyads.size(); j++) {
			if (queryDyads.get(j).getAlternative().equals(removedAlternative)) {
				index = j;
				break;
			}
		}

		if (index >= -1) {
			queryDyads.remove(index);
		}

		return removedNode;
	}

	@Override
	public Node<N, V> poll() {
		if (!queue.isEmpty()) {
			return this.remove();
		} else {
			return null;
		}
	}

	@Override
	public Node<N, V> element() {
		return queue.get(0);
	}

	@Override
	public Node<N, V> peek() {
		if (!queue.isEmpty()) {
			logger.trace("Peek from OPEN. Is goal: {}", this.element().isGoal());
			return this.element();
		} else {
			return null;
		}
	}

	/**
	 * Get the dyad ranker used to rank the nodes.
	 * 
	 * @return the dyad ranker
	 */
	public IDyadRanker getDyadRanker() {
		return dyadRanker;
	}

	/**
	 * Set which dyad ranker shall be used to rank the nodes. It is not trained in
	 * this class, so it must be pre-trained before setting it as a dyad ranker for
	 * this queue!
	 * 
	 * @param dyadRanker
	 *            the dyad ranker
	 */
	public void setDyadRanker(IDyadRanker dyadRanker) {
		logger.trace("Update dyad ranker. Was {} now is {}", this.dyadRanker.getClass(), dyadRanker.getClass());
		this.dyadRanker = dyadRanker;
	}

	public AbstractDyadScaler getScaler() {
		return scaler;
	}

	public void setScaler(AbstractDyadScaler scaler) {
		if (useScaler) {
			logger.trace("Update scaler. Was {} now is {}", this.scaler.getClass(), scaler.getClass());
		} else {
			logger.trace("Now using scaler {}.", scaler.getClass());
			this.useScaler = true;
		}

		this.scaler = scaler;

		// transform dataset
		contextCharacterization = originalContextCharacterization.addConstantToCopy(0);
		transformContextCharacterization();
	}

	private void transformContextCharacterization() {
		logger.trace("Transform context characterization with scaler {}", this.scaler.getClass());
		Dyad dyad = new Dyad(contextCharacterization, contextCharacterization);
		DyadRankingInstance instance = new DyadRankingInstance(Arrays.asList(dyad));
		DyadRankingDataset dataset = new DyadRankingDataset();
		dataset.add(instance);
		scaler.transformInstances(dataset);
	}

}
