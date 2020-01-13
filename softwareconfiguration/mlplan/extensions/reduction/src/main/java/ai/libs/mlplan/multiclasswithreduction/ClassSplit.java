package ai.libs.mlplan.multiclasswithreduction;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ClassSplit<T> {
	private final Collection<T> classes;
	private final Collection<T> l;
	private final Collection<T> r;

	public ClassSplit(final Collection<T> classes, final Collection<T> l, final Collection<T> r) {
		super();
		this.classes = classes;
		this.l = l;
		this.r = r;
	}

	public ClassSplit(final ClassSplit<T> split) {
		this(split.getClasses(), new ArrayList<>(split.getL()), new ArrayList<>(split.getR()));
	}

	public Collection<T> getClasses() {
		return this.classes;
	}

	public Collection<T> getL() {
		return this.l;
	}

	public Collection<T> getR() {
		return this.r;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.classes).append(this.l).append(this.r).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ClassSplit)) {
			return false;
		}
		ClassSplit<?> other = (ClassSplit<?>) obj;
		return new EqualsBuilder().append(other.classes, this.classes).append(other.l, this.l).append(other.r, this.r).isEquals();

	}

	@Override
	public String toString() {
		return "ClassSplit [classes=" + this.classes + ", l=" + this.l + ", r=" + this.r + "]";
	}

}
