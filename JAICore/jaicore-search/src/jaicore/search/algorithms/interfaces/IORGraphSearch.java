package jaicore.search.algorithms.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public interface IORGraphSearch<T, A, V extends Comparable<V>> {

	public void bootstrap(Collection<Node<T,V>> nodes);
	
	public List<T> nextSolution();

	public V getFValue(T node);

	public V getFValue(Node<T, V> node);

	public Map<String,Object> getAnnotationsOfReturnedSolution(List<T> solution);
	
	public Object getAnnotationOfReturnedSolution(List<T> solution, String annotation);
	
	public V getFOfReturnedSolution(List<T> solution);

	public void cancel();

	public Node<T, V> getInternalRepresentationOf(T node);

	public List<Node<T, V>> getOpenSnapshot();
	
	public INodeEvaluator<T, V> getNodeEvaluator();
}
