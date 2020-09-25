package ai.libs.jaicore.components.api;

import java.util.Collection;

public interface IComponentRepository extends Collection<IComponent> {

	public IComponent getComponent(String name);

	public Collection<IComponentInstanceConstraint> getConstraints();
}
