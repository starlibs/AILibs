package ai.libs.jaicore.ml.hpo.ga;

import java.util.Random;

import ai.libs.jaicore.ml.hpo.ga.gene.IGene;
import ai.libs.jaicore.ml.hpo.ga.gene.IntGene;
import ai.libs.jaicore.ml.hpo.ga.gene.RealGene;

public class SimpleMutation implements IGeneticOperator {

	private final Random rand;
	private final double rate;

	public SimpleMutation(final Random rand, final double rate) {
		this.rand = rand;
		this.rate = rate;
	}

	@Override
	public IIndividual[] apply(final IIndividual... individuals) {
		IIndividual individualToMutate = individuals[0].copy();
		for (int i = 0; i < individualToMutate.size(); i++) {
			if (this.rand.nextDouble() < this.rate) {
				IGene geneToMutate = individualToMutate.getGene(0);
				if (geneToMutate instanceof IntGene) {
					IntGene ig = (IntGene) geneToMutate;
					geneToMutate.setValue(ig.getLowerBound() + this.rand.nextInt(ig.getUpperBound() - ig.getLowerBound()));
				} else if (geneToMutate instanceof RealGene) {
					RealGene rg = (RealGene) geneToMutate;
					geneToMutate.setValue(rg.getLowerBound() + this.rand.nextDouble() * (rg.getUpperBound() - rg.getLowerBound()));
				}
			}
		}
		return new IIndividual[] { individualToMutate };
	}

	@Override
	public int getArity() {
		return 1;
	}

}
