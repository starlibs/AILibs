package ai.libs.jaicore.components.api;

import java.io.Serializable;
import java.util.Collection;

import ai.libs.jaicore.basic.sets.Pair;

public interface IParameterDependency extends Serializable {

	public Collection<Collection<Pair<IParameter, IParameterDomain>>> getPremise();

	public Collection<Pair<IParameter, IParameterDomain>> getConclusion();
}
