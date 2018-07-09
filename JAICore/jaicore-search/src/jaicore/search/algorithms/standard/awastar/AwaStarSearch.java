package jaicore.search.algorithms.standard.awastar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.OpenCollection;
import jaicore.search.structure.core.PriorityQueueOpen;

public class AwaStarSearch <T,A> extends ORGraphSearch<T, A, Double> {

	private static final Logger logger = LoggerFactory.getLogger(AwaStarSearch.class);
	
	private class AwaStarCollection implements OpenCollection<Node<T, Double>> {

		private PriorityQueueOpen<Node<T, Double>> open;
		private Set<Node<T, Double>> suspend;
		private Set<Node<T, Double>> closed;
		
		public AwaStarCollection() {
			open = new PriorityQueueOpen<>();
			suspend = new HashSet<>();
			closed = new HashSet<>();			
		}
		
		@Override
		public boolean add(Node<T, Double> arg0) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends Node<T, Double>> arg0) {
			return false;
		}

		@Override
		public void clear() {			
		}

		@Override
		public boolean contains(Object arg0) {
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Iterator<Node<T, Double>> iterator() {
			return null;
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			return false;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public Object[] toArray() {
			return null;
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			return null;
		}

		@Override
		public Node<T, Double> peek() {
			if (suspend.isEmpty() || open.isEmpty()) {
				return null;
			} else {
				if (!closed.contains(open.peek())) {
					closed.add(open.peek());
				}
				return open.peek();
			}
		}

		@Override
		public boolean remove(Object o) {
			return false;
		}
		
	}
	
	private int windowSize;
	
	public AwaStarSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> pNodeEvaluator) {
		super(graphGenerator, pNodeEvaluator);
		windowSize = 0;
		this.setOpen(new AwaStarCollection());
	}
}
