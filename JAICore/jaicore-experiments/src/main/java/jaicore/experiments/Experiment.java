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

	public Experiment(int memoryInMB, int numCPUs, Map<String, String> valuesOfKeyFields) {
		super();
		this.memoryInMB = memoryInMB;
		this.numCPUs = numCPUs;
		this.valuesOfKeyFields = valuesOfKeyFields;
	}

	public Map<String, String> getValuesOfKeyFields() {
		return valuesOfKeyFields;
	}

	public void setKeys(Map<String, String> keys) {
		this.valuesOfKeyFields = keys;
	}

	public int getMemoryInMB() {
		return memoryInMB;
	}

	public int getNumCPUs() {
		return numCPUs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((valuesOfKeyFields == null) ? 0 : valuesOfKeyFields.hashCode());
		result = prime * result + memoryInMB;
		result = prime * result + numCPUs;
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
		Experiment other = (Experiment) obj;
		if (valuesOfKeyFields == null) {
			if (other.valuesOfKeyFields != null)
				return false;
		} else if (!valuesOfKeyFields.equals(other.valuesOfKeyFields)) {
			return false;
		}
		if (memoryInMB != other.memoryInMB)
			return false;
		return numCPUs == other.numCPUs;
	}
}
