package jaicore.experiments;

public interface IExperimentKeyGenerator<T> {

	public int getNumberOfValues();

	public T getValue(int i);
}
