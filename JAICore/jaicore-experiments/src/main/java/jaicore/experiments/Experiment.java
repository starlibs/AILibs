package jaicore.experiments;

import java.util.Map;

/**
 * Basic experiment class that describes an experiment conceptually in terms of hardware information and semantic keys.
 *
 * @author fmohr
 *
 */
public class Experiment {
	private final int memoryInMB;
	private final int numCPUs;
	private Map<String,String> valuesOfKeyFields;

	public Experiment(final int memoryInMB, final int numCPUs, final Map<String, String> valuesOfKeyFields) {
		super();
		this.memoryInMB = memoryInMB;
		this.numCPUs = numCPUs;
		this.valuesOfKeyFields = valuesOfKeyFields;
	}

	public Map<String, String> getValuesOfKeyFields() {
		return this.valuesOfKeyFields;
	}

	public void setKeys(final Map<String, String> keys) {
		this.valuesOfKeyFields = keys;
	}

	public int getMemoryInMB() {
		return this.memoryInMB;
	}

	public int getNumCPUs() {
		return this.numCPUs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.valuesOfKeyFields == null) ? 0 : this.valuesOfKeyFields.hashCode());
		result = prime * result + this.memoryInMB;
		result = prime * result + this.numCPUs;
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
		Experiment other = (Experiment) obj;
		if (this.valuesOfKeyFields == null) {
			if (other.valuesOfKeyFields != null) {
				return false;
			}
		} else if (!this.valuesOfKeyFields.equals(other.valuesOfKeyFields)) {
			return false;
		}
		if (this.memoryInMB != other.memoryInMB) {
			return false;
		}
		return this.numCPUs == other.numCPUs;
	}
}
