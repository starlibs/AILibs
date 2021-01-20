package ai.libs.jaicore.ml.hpo.ga;

import java.util.Random;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.ml.hpo.IComponentInstanceHPOInput;

public interface IComponentInstanceHPOGAInput extends IComponentInstanceHPOInput {

	public IIndividual newRandomIndividual(Random rand);

	public IComponentInstance convertIndividualToComponentInstance(IIndividual individual);

}
