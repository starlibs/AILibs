package ai.libs.jaicore.components.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstanceConstraint;
import ai.libs.jaicore.components.api.IComponentRepository;

public class ComponentRepository extends ArrayList<IComponent> implements IComponentRepository {

	private static final long serialVersionUID = 3966345495009688845L;

	private final Collection<IComponentInstanceConstraint> constraints = new ArrayList<>();

	public ComponentRepository() {
		this(new ArrayList<>());
	}

	public ComponentRepository(final Collection<? extends IComponent> components) {
		this(components, new ArrayList<>());
	}

	public ComponentRepository(final Collection<? extends IComponent> components, final Collection<? extends IComponentInstanceConstraint> constraints) {
		super();
		this.addAll(components);
		this.constraints.addAll(constraints);
	}

	@Override
	public Component getComponent(final String name) {
		Optional<IComponent> opt = this.stream().filter(c -> c.getName().equals(name)).findAny();
		if (!opt.isPresent()) {
			throw new NoSuchElementException();
		}
		return (Component)opt.get();
	}

	@Override
	public Collection<IComponentInstanceConstraint> getConstraints() {
		return this.constraints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.constraints.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		ComponentRepository other = (ComponentRepository) obj;
		return this.constraints.equals(other.constraints);
	}
}
