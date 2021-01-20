package ai.libs.jaicore.ml.hpo.ga;

import java.util.List;
import java.util.Random;

import ai.libs.jaicore.ml.hpo.ga.gene.IGene;

public class SimpleCrossover implements IGeneticOperator {

	private final Random rand;

	public SimpleCrossover(final Random rand) {
		this.rand = rand;
	}

	@Override
	public int getArity() {
		return 2;
	}

	@Override
	public IIndividual[] apply(final IIndividual... individuals) {
		IIndividual ind1 = individuals[0];
		IIndividual ind2 = individuals[1];
		int numObjectives = ind1.getNumObjectives();

		int cutIndex = this.rand.nextInt(ind1.size());

		List<IGene> co1 = ind1.getExtractOfIndividual(0, cutIndex);
		co1.addAll(ind2.getExtractOfIndividual(cutIndex, ind2.size()));
		List<IGene> co2 = ind2.getExtractOfIndividual(0, cutIndex);
		co2.addAll(ind1.getExtractOfIndividual(cutIndex, ind1.size()));

		return new IIndividual[] { new Individual(co1, numObjectives), new Individual(co2, numObjectives) };
	}

}
