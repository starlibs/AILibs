package ai.libs.jaicore.components.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentRepository;

public class ComponentRepository extends ArrayList<IComponent> implements IComponentRepository {

	private static final long serialVersionUID = 3966345495009688845L;

	public ComponentRepository() {
		super();
	}

	public ComponentRepository(final Collection<? extends IComponent> components) {
		this();
		this.addAll(components);
	}

	@Override
	public Component getComponent(final String name) {
		Optional<IComponent> opt = this.stream().filter(c -> c.getName().equals(name)).findAny();
		if (!opt.isPresent()) {
			throw new NoSuchElementException();
		}
		return (Component)opt.get();
	}

}
