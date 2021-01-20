package ai.libs.jaicore.ml.hpo.ga;

import java.util.List;

import ai.libs.jaicore.ml.hpo.ga.gene.IGene;

public interface IIndividual extends Comparable<IIndividual> {

	public int size();

	default List<IGene> getGenotype() {
		return this.getPrefixOfIndividual(0);
	}

	public IGene getGene(int index);

	public int getNumObjectives();

	public double getObjective(int index);

	public void setObjective(int index, double value);

	public List<IGene> getExtractOfIndividual(int startIndex, int endIndexExcl);

	public List<IGene> getPrefixOfIndividual(int endIndexExcl);

	public List<IGene> getSuffixOfIndividual(int startIndex);

	public IIndividual copy();

}
