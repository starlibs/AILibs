package ai.libs.jaicore.ml.hpo.ga;

public interface IGeneticOperator {

	public IIndividual[] apply(IIndividual... individuals);

	public int getArity();

}
