package jaicore.ml.dyadranking.search;

import java.util.LinkedList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.model.travesaltree.Node;

/**
 * A node queue for the best first search that inserts new nodes at a random
 * position in the list.
 * 
 * @author Helena Graf
 *
 * @param <N>
 * @param <V>
 */
@SuppressWarnings("serial")
public class RandomlyRankedNodeQueue<N, V extends Comparable<V>> extends LinkedList<Node<N, V>> {

	private Random random;
	private Logger logger = LoggerFactory.getLogger(RandomlyRankedNodeQueue.class);

	public RandomlyRankedNodeQueue(int seed) {
		this.random = new Random(seed);
	}

	/**
	 * Adds an element at a random position within the 
	 */
	@Override
	public boolean add(Node<N, V> e) {
		int position = random.nextInt(this.size() + 1);
		logger.debug("Add node at random position {} to OPEN list of size {}.", position, this.size());
		super.add(position, e);
		return true;
	}
	
	@Override
	public void add(int position, Node<N, V> e) {
		throw new UnsupportedOperationException("Cannot place items at a specific position wihtin a randomly ranked queue!");
	}
}
