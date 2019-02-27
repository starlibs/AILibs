package jaicore.web.mcmc.rest.message;

import java.util.ArrayList;
import java.util.List;

public class McmcResponse {

	private List<LinearCombinationParameterSet> parameterSets;

	public List<LinearCombinationParameterSet> getParameterSets() {
		if (this.parameterSets == null) {
			this.parameterSets = new ArrayList<>();
		}
		return parameterSets;
	}

	public void setParameterSets(List<LinearCombinationParameterSet> parameterSets) {
		this.parameterSets = parameterSets;
	}

	public void addParameterSet(LinearCombinationParameterSet parameterSet) {
		this.getParameterSets().add(parameterSet);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parameterSets == null) ? 0 : parameterSets.hashCode());
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
		McmcResponse other = (McmcResponse) obj;
		if (parameterSets == null) {
			if (other.parameterSets != null)
				return false;
		} else if (!parameterSets.equals(other.parameterSets))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "McmcResponse [parameterSets=" + parameterSets + "]";
	}

}
