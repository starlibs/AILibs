package jaicore.ea.population;

import java.util.Collection;

public interface IPopulation<I extends IIndividual> extends Iterable<I> {

	public Collection<I> getIndividuals();

}
