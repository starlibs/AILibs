package ai.libs.jaicore.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AEntitySelector<T> {
	protected final Set<T> init;
	protected final Set<T> current;

	protected AEntitySelector(final Collection<T> items) {
		this.init = new HashSet<>(items);
		this.current = new HashSet<>(items);
	}

	public Set<T> get() {
		return new HashSet<>(this.current);
	}

	public Set<T> getInverted() {
		Set<T> inverted = new HashSet<>();
		this.init.forEach(i -> {
			if (!this.current.contains(i)) {
				inverted.add(i);
			}
		});
		return inverted;
	}

	public AEntitySelector<T> invert() {
		Set<T> tmp = new HashSet<>(this.current);
		this.current.clear();
		this.init.forEach(i -> {
			if (!tmp.contains(i)) {
				this.current.add(i);
			}
		});
		return this;
	}
}
