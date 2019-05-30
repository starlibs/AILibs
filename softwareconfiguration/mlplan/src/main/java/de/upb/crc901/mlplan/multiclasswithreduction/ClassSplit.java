package de.upb.crc901.mlplan.multiclasswithreduction;
import java.util.ArrayList;
import java.util.Collection;

public class ClassSplit<T> {
	private final Collection<T> classes;
	private final Collection<T> l, r;

	public ClassSplit(Collection<T> classes, Collection<T> l, Collection<T> r) {
		super();
		this.classes = classes;
		this.l = l;
		this.r = r;
	}
	
	public ClassSplit(ClassSplit<T> split) {
		this(split.getClasses(), new ArrayList<>(split.getL()), new ArrayList<>(split.getR()));
	}

	public Collection<T> getClasses() {
		return classes;
	}

	public Collection<T> getL() {
		return l;
	}

	public Collection<T> getR() {
		return r;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classes == null) ? 0 : classes.hashCode());
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + ((r == null) ? 0 : r.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		ClassSplit<T> other = (ClassSplit<T>) obj;
		if (classes == null) {
			if (other.classes != null)
				return false;
		} else if (!classes.equals(other.classes))
			return false;
		if (l == null) {
			if (other.l != null)
				return false;
		} else if (!l.equals(other.l))
			return false;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassSplit [classes=" + classes + ", l=" + l + ", r=" + r + "]";
	}
	
}
