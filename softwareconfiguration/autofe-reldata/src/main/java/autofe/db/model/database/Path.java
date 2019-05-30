package autofe.db.model.database;

import java.util.ArrayList;
import java.util.List;

import autofe.db.model.relation.AbstractRelationship;
import autofe.db.util.Tuple;

public class Path {

	private List<Tuple<AbstractRelationship, AggregationFunction>> pathElements;

	public Path() {
		this.pathElements = new ArrayList<>();
	}

	public Path(final List<Tuple<AbstractRelationship, AggregationFunction>> pathElements) {
		this.pathElements = new ArrayList<>(pathElements);
	}

	public Path(final Path toClone) {
		this(toClone.pathElements);
	}

	public List<Tuple<AbstractRelationship, AggregationFunction>> getPathElements() {
		return this.pathElements;
	}

	public void addPathElement(final Tuple<AbstractRelationship, AggregationFunction> toAdd) {
		this.pathElements.add(toAdd);
	}

	public void addPathElement(final AbstractRelationship ar, final AggregationFunction af) {
		this.pathElements.add(new Tuple<AbstractRelationship, AggregationFunction>(ar, af));
	}

	public String getLastTableName() {
		if (this.pathElements.isEmpty()) {
			return null;
		}
		Tuple<AbstractRelationship, AggregationFunction> lastPathElement = this.pathElements.get(this.pathElements.size() - 1);
		return lastPathElement.getT().getFromTableName();
	}

	public int length() {
		return this.pathElements.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.pathElements == null) ? 0 : this.pathElements.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Path)) {
			return false;
		}
		Path other = (Path) obj;
		if (this.pathElements == null) {
			if (other.pathElements != null) {
				return false;
			}
		} else if (!this.pathElements.equals(other.pathElements)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (this.pathElements.isEmpty()) {
			return "[{}]";
		}
		StringBuilder sb = new StringBuilder();
		for (Tuple<AbstractRelationship, AggregationFunction> pathElement : this.pathElements) {
			sb.append(pathElement.getT().getFromTableName());
			sb.append("<-(" + pathElement.getU() + ")-");
			sb.append(pathElement.getT().getToTableName());
			sb.append("|");
		}
		return sb.substring(0, sb.length() - 1);
	}

}
