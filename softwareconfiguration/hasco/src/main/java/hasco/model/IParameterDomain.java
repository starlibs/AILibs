package hasco.model;

public interface IParameterDomain {

	public boolean contains(Object item);

	public boolean subsumes(IParameterDomain otherDomain);
}
