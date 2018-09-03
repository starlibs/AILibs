package jaicore.basic.algorithm;

public class IOptimizerResult<O, V extends Comparable<V>> {
	private final O result;
	private final V value;

	public IOptimizerResult(O result, V value) {
		super();
		this.result = result;
		this.value = value;
	}

	public O getResult() {
		return result;
	}

	public V getValue() {
		return value;
	}
}
