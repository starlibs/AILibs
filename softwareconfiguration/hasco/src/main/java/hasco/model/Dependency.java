package hasco.model;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jaicore.basic.sets.SetUtil.Pair;

public class Dependency {
	private final Collection<Collection<Pair<Parameter, IParameterDomain>>> premise; // semantics are DNF (every entry is an AND-connected constraint)
	private final Collection<Pair<Parameter, IParameterDomain>> conclusion;

	@JsonCreator
	public Dependency(@JsonProperty("premise") Collection<Collection<Pair<Parameter, IParameterDomain>>> premise, @JsonProperty("conclusion") Collection<Pair<Parameter, IParameterDomain>> conclusion) {
		super();
		this.premise = premise;
		this.conclusion = conclusion;
	}

	public Collection<Collection<Pair<Parameter, IParameterDomain>>> getPremise() {
		return this.premise;
	}

	public Collection<Pair<Parameter, IParameterDomain>> getConclusion() {
		return this.conclusion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.conclusion == null) ? 0 : this.conclusion.hashCode());
		result = prime * result + ((this.premise == null) ? 0 : this.premise.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Dependency other = (Dependency) obj;
		if (this.conclusion == null) {
			if (other.conclusion != null) {
				return false;
			}
		} else if (!this.conclusion.equals(other.conclusion)) {
			return false;
		}
		if (this.premise == null) {
			if (other.premise != null) {
				return false;
			}
		} else if (!this.premise.equals(other.premise)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.premise);
		sb.append(" => ");
		sb.append(this.conclusion);

		return sb.toString();
	}
}
