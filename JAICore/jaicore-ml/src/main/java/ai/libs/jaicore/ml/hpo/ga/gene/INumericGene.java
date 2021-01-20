package ai.libs.jaicore.ml.hpo.ga.gene;

public interface INumericGene extends IGene {

	public Number getUpperBound();

	public Number getLowerBound();

	@Override
	default String getValueAsString() {
		return this.getValue() + "";
	}

}
