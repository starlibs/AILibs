package ai.libs.jaicore.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class EntitySelector<T> {
	protected final Set<T> init;
	protected final Set<T> current;
	
	public EntitySelector(Collection<T> items) {
		init = new HashSet<>(items);
		current = new HashSet<>(items);
	}
	
	public Set<T> get() {
		return new HashSet<>(current);
	}
	
	public Set<T> getInverted() {
		Set<T> inverted = new HashSet<>();
		init.forEach(i -> { if (!current.contains(i)) inverted.add(i); });
		return inverted;
	}

	public EntitySelector<T> invert() {
		Set<T> tmp = new HashSet<>(current);
		current.clear();
		init.forEach(i -> { if (!tmp.contains(i)) current.add(i); });
		return this;
	}
}
