package jaicore.graph.observation;

/**
 * 
 * @author fmohr
 *
 * @param <V> Class for nodes
 * @param <E> Class for edges
 */
public interface IObservableGraphAlgorithm<V,E> {
	public void registerListener(Object listener);
}
