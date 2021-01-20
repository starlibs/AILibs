package ai.libs.jaicore.ml.hpo.ga.gene;

import java.util.List;
import java.util.Random;

public class NominalGene extends IntGene {

	private final List<String> values;

	public NominalGene(final List<String> values) {
		super(0, values.size());
		this.values = values;
	}

	public NominalGene(final List<String> values, final String value) {
		super(0, values.size(), values.indexOf(value));
		this.values = values;
	}

	public NominalGene(final List<String> values, final Random rand) {
		super(0, values.size(), rand);
		this.values = values;
	}

	@Override
	public String getValueAsString() {
		return this.values.get(super.getValue());
	}

	public List<String> getValues() {
		return this.values;
	}

	@Override
	public IntGene copy() {
		return new NominalGene(this.values, this.getValueAsString());
	}
}
