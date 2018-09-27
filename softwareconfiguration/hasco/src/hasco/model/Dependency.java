package hasco.model;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jaicore.basic.sets.SetUtil.Pair;

public class Dependency {
	private final Collection<Collection<Pair<Parameter, ParameterDomain>>> premise; // semantics are DNF (every entry is an AND-connected constraint)
	private final Collection<Pair<Parameter, ParameterDomain>> conclusion;

	@JsonCreator
	public Dependency(@JsonProperty("premise") Collection<Collection<Pair<Parameter, ParameterDomain>>> premise, @JsonProperty("conclusion") Collection<Pair<Parameter, ParameterDomain>> conclusion) {
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
