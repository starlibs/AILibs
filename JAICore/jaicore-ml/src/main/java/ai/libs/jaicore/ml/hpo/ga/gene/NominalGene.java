package ai.libs.jaicore.ml.hpo.ga.gene;

import java.util.List;

public class NominalGene extends IntGene {

	private final List<String> values;

	public NominalGene(final List<String> values) {
		super(0, values.size());
		this.values = values;
	}

	public String getValueAsString() {
		return this.values.get(super.getValue());
	}

	public List<String> getValues() {
		return this.values;
	}

}
