package ai.libs.mlplan.multiclasswithreduction;
import java.util.ArrayList;
import java.util.Collection;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.classes == null) ? 0 : this.classes.hashCode());
		result = prime * result + ((this.l == null) ? 0 : this.l.hashCode());
		result = prime * result + ((this.r == null) ? 0 : this.r.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		ClassSplit<T> other = (ClassSplit<T>) obj;
		if (this.classes == null) {
			if (other.classes != null) {
				return false;
			}
		} else if (!this.classes.equals(other.classes)) {
			return false;
		}
		if (this.l == null) {
			if (other.l != null) {
				return false;
			}
		} else if (!this.l.equals(other.l)) {
			return false;
		}
		if (this.r == null) {
			if (other.r != null) {
				return false;
			}
		} else if (!this.r.equals(other.r)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ClassSplit [classes=" + this.classes + ", l=" + this.l + ", r=" + this.r + "]";
	}

}
