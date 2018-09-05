package hasco.model;

import java.util.Collection;

import jaicore.basic.sets.SetUtil.Pair;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conclusion == null) ? 0 : conclusion.hashCode());
		result = prime * result + ((premise == null) ? 0 : premise.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dependency other = (Dependency) obj;
		if (conclusion == null) {
			if (other.conclusion != null)
				return false;
		} else if (!conclusion.equals(other.conclusion))
			return false;
		if (premise == null) {
			if (other.premise != null)
				return false;
		} else if (!premise.equals(other.premise))
			return false;
		return true;
	}
}
