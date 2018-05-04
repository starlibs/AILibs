package hasco.model;

public abstract class ParameterDomain {
	
	abstract public boolean contains(Object item);
	
	abstract public boolean subsumes(ParameterDomain otherDomain);
}
