package ai.libs.jaicore.ml.hpo.ga;

import java.util.List;
import java.util.Random;

import ai.libs.jaicore.components.api.IComponentInstance;

public interface IInitialization {

	public List<IIndividual> initialize(IComponentInstance ci);

	public void setRandom(Random rand);

}
