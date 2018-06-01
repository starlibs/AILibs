package hasco.model;

import java.util.Collection;

import jaicore.basic.SetUtil.Pair;

public class Dependency {
	private final Collection<Collection<Pair<Parameter, ParameterDomain>>> premise; // semantics are DNF (every entry is an AND-connected constraint)
	private final Collection<Pair<Parameter, ParameterDomain>> conclusion;

	public Dependency(Collection<Collection<Pair<Parameter, ParameterDomain>>> premise, Collection<Pair<Parameter, ParameterDomain>> conclusion) {
		super();
		this.premise = premise;
		this.conclusion = conclusion;
	}

	public Collection<Collection<Pair<Parameter, ParameterDomain>>> getPremise() {
		return premise;
	}

	public Collection<Pair<Parameter, ParameterDomain>> getConclusion() {
		return conclusion;
	}
}
