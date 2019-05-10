//package jaicore.search.algorithms.standard.opencollections;
//
//import java.util.Collection;
//import java.util.Iterator;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import jaicore.search.algorithms.standard.bestfirst.model.OpenCollection;
//import jaicore.search.algorithms.standard.bestfirst.model.PriorityQueueOpen;
//import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
//import jaicore.search.model.travesaltree.Node;
//
///**
// * This is like EnforcedExplorationOpenSelection except that there is a fixed rule how the restriction on exploration is set.
// * 
// * @author fmohr
// *
// * @param <N>
// * @param <V>
// * @param <W>
// */
//public class TimedEnforcedExplorationOpenSelection<N,V extends Comparable<V>,W extends Comparable<W>> extends EnforcedExplorationOpenSelection<N, V, W> {
//	
//	private static final Logger logger = LoggerFactory.getLogger(TimedEnforcedExplorationOpenSelection.class);
//	
//	private final PriorityQueueOpen<Node<N, V>> secondaryOpen = new PriorityQueueOpen<>();
//	private final INodeEvaluator<N, W> explorationEvaluator;
//	private int explorationPhaseLength = 10;
//	private int exploitationPhaseLength = 50;
//	private int selectedNodes = 0;
//	private int exploredNodes = 0;
//	private boolean exploring = false;
//
//	public TimedEnforcedExplorationOpenSelection(OpenCollection<Node<N, V>> primaryOpen, INodeEvaluator<N, W> explorationEvaluator, int explorationPhaseLength, int exploitationPhaseLength) {
//		super(primaryOpen);
//		this.explorationEvaluator = explorationEvaluator;
//		this.explorationPhaseLength = explorationPhaseLength;
//		this.exploitationPhaseLength = exploitationPhaseLength;
//	}
//
//	@Override
//	public Node<N, V> peek() {
//		
//		if (!exploring) {
//			selectedNodes++;
//			if (selectedNodes % exploitationPhaseLength != 0) {
//				// System.out.println("Exploiting ...");
//				return primaryOpen.peek();
//			}
//
//			/* now chose particular node for expansion */
//			Node<N, V> nodeToBeExplored = primaryOpen.stream().min((n1, n2) -> {
//				try {
//					return explorationEvaluator.f(n1).compareTo(explorationEvaluator.f(n2));
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//				return 0;
//			}).get();
//
//			/* enable exploration with the node selected by the explorer evaluator */
//			try {
//				if (logger.isInfoEnabled())
//					logger.info("Entering exploration phase under {} with exploration value: {}", nodeToBeExplored, explorationEvaluator.f(nodeToBeExplored));
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
//			exploring = true;
//			exploredNodes = 0;
//			primaryOpen.remove(nodeToBeExplored);
//			secondaryOpen.clear();
//			secondaryOpen.add(nodeToBeExplored);
//			return nodeToBeExplored;
//		} else {
//			exploredNodes++;
//			if (exploredNodes > explorationPhaseLength || secondaryOpen.isEmpty()) {
//				exploring = false;
//				primaryOpen.addAll(secondaryOpen);
//				secondaryOpen.clear();
//				return primaryOpen.peek();
//			}
//			return secondaryOpen.peek();
//		}
//	}
//
//	@Override
//	public boolean add(Node<N, V> node) {
//		assert !contains(node) : "Node " + node + " is already there!";
//		if (exploring) {
//			return secondaryOpen.add(node);
//		} else
//			return primaryOpen.add(node);
//	}
//
//	@Override
//	public boolean remove(Object node) {
//		assert !(primaryOpen.contains(node) && secondaryOpen.contains(node)) : "A node (" + node + ") that is to be removed is in BOTH open lists!";
//		if (exploring) {
//			return secondaryOpen.remove(node) || primaryOpen.remove(node);
//		} else {
//			return primaryOpen.remove(node) || secondaryOpen.remove(node);
//		}
//	}
//
//	@Override
//	public boolean addAll(Collection<? extends Node<N, V>> arg0) {
//		if (exploring) {
//			return secondaryOpen.addAll(arg0);
//		} else
//			return primaryOpen.addAll(arg0);
//	}
//
//	@Override
//	public void clear() {
//		primaryOpen.clear();
//		secondaryOpen.clear();
//	}
//
//	@Override
//	public boolean contains(Object arg0) {
//		return primaryOpen.contains(arg0) || secondaryOpen.contains(arg0);
//	}
//
//	@Override
//	public boolean containsAll(Collection<?> arg0) {
//		for (Object o : arg0) {
//			if (!contains(o))
//				return false;
//		}
//		return true;
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return primaryOpen.isEmpty() && secondaryOpen.isEmpty();
//	}
//
//	@Override
//	public Iterator<Node<N, V>> iterator() {
//		return null;
//	}
//
//	@Override
//	public boolean removeAll(Collection<?> arg0) {
//		return primaryOpen.removeAll(arg0) && secondaryOpen.removeAll(arg0);
//	}
//
//	@Override
//	public boolean retainAll(Collection<?> arg0) {
//		return primaryOpen.retainAll(arg0) && secondaryOpen.retainAll(arg0);
//	}
//
//	@Override
//	public int size() {
//		return primaryOpen.size() + secondaryOpen.size();
//	}
//
//	@Override
//	public Object[] toArray() {
//		return primaryOpen.toArray();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public <T> T[] toArray(T[] arg0) {
//		return (T[]) primaryOpen.toArray();
//	}
//
//}
