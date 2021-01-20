package ai.libs.jaicore.ml.hpo.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ai.libs.jaicore.ml.hpo.ga.gene.IGene;

public class Individual implements IIndividual {

	private List<IGene> genotype;
	private double[] objectives;

	public Individual(final List<IGene> genes, final int numObjectives) {
		this.objectives = new double[numObjectives];
		this.genotype = genes.stream().map(x -> x.copy()).collect(Collectors.toList());
	}

	@Override
	public int getNumObjectives() {
		return this.objectives.length;
	}

	@Override
	public double getObjective(final int index) {
		return this.objectives[index];
	}

	@Override
	public void setObjective(final int index, final double value) {
		this.objectives[index] = value;
	}

	@Override
	public int size() {
		return this.genotype.size();
	}

	@Override
	public IGene getGene(final int index) {
		return this.genotype.get(index);
	}

	@Override
	public List<IGene> getExtractOfIndividual(final int startIndex, final int endIndexExcl) {
		List<IGene> extract = new ArrayList<>(endIndexExcl - startIndex);
		for (int i = startIndex; i < endIndexExcl; i++) {
			extract.add(this.genotype.get(i));
		}
		return extract;
	}

	@Override
	public List<IGene> getPrefixOfIndividual(final int endIndexOfPrefixExcl) {
		return this.getExtractOfIndividual(0, endIndexOfPrefixExcl);
	}

	@Override
	public List<IGene> getSuffixOfIndividual(final int startIndex) {
		return this.getExtractOfIndividual(startIndex, this.size());
	}

	@Override
	public Individual copy() {
		return new Individual(this.genotype, this.objectives.length);
	}

	@Override
	public int compareTo(final IIndividual o) {
		boolean allBetter = true;
		boolean allWorse = true;

		for (int i = 0; i < this.getNumObjectives(); i++) {
			if (this.getObjective(i) < o.getObjective(i)) {
				allWorse &= false;
			}
			if (this.getObjective(i) > o.getObjective(i)) {
				allBetter &= false;
			}
		}

		if (allBetter) {
			return -1;
		} else if (allWorse) {
			return 1;
		} else {
			return 0;
		}
	}

}
