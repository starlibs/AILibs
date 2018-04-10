package jaicore.graph.observation;

public interface IObservableGraphAlgorithm<V,E> {
	public void registerListener(Object listener);
}
