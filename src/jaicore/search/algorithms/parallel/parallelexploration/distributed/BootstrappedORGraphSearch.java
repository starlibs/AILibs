package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.util.Collection;
import java.util.List;

import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.events.NodeTypeSwitchEvent;

public class BootstrappedORGraphSearch<T, A, V extends Comparable<V>> extends ORGraphSearch<T, A, V>{

	private final Collection<Node<T,V>> initialNodes;
	public BootstrappedORGraphSearch(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, V> pNodeEvaluator, Collection<Node<T,V>> initialNodes) {
		super(graphGenerator, pNodeEvaluator);
		this.initialNodes = initialNodes;
	}
	
	protected void afterInitialization() {
		
		/* remove previous roots from open */
		open.clear();
		
		/* now insert new nodes, and the leaf ones in open */
		for (Node<T,V> node : initialNodes) {
			insertPathIntoLocalGraph(node.path());
			open.add(getLocalVersionOfNode(node));
		}
	}
	
	private void insertPathIntoLocalGraph(List<Node<T, V>> path) {
		Node<T, V> localVersionOfParent = null;
		Node<T, V> leaf = path.get(path.size() - 1);
		for (Node<T, V> node : path) {
			if (!ext2int.containsKey(node.getPoint())) {
				assert node.getParent() != null : "Want to insert a new node that has no parent. That must not be the case! Affected node is: " + node.getPoint();
				assert ext2int.containsKey(node.getParent().getPoint()) : "Want to insert a node whose parent is unknown locally";
				Node<T, V> newNode = newNode(localVersionOfParent, node.getPoint(), node.getInternalLabel());
				if (!newNode.isGoal() && !newNode.getPoint().equals(leaf.getPoint()))
					this.getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(newNode, "or_closed"));
				localVersionOfParent = newNode;
			} else
				localVersionOfParent = getLocalVersionOfNode(node);
		}
	}
	
	private Node<T, V> getLocalVersionOfNode(Node<T, V> node) {
		return ext2int.get(node.getPoint());
	}
}
