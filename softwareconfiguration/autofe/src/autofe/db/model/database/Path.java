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

	public Path(List<Tuple<AbstractRelationship, AggregationFunction>> pathElements) {
		this.pathElements = new ArrayList<>(pathElements);
	}

	public Path(Path toClone) {
		this(toClone.pathElements);
	}

	public List<Tuple<AbstractRelationship, AggregationFunction>> getPathElements() {
		return pathElements;
	}

	public void addPathElement(Tuple<AbstractRelationship, AggregationFunction> toAdd) {
		pathElements.add(toAdd);
	}

	public void addPathElement(AbstractRelationship ar, AggregationFunction af) {
		pathElements.add(new Tuple<AbstractRelationship, AggregationFunction>(ar, af));
	}

	public Table getLastTable() {
		if (pathElements.isEmpty()) {
			return null;
		}
		Tuple<AbstractRelationship, AggregationFunction> lastPathElement = pathElements.get(pathElements.size() - 1);
		return lastPathElement.getT().getFrom();
	}

	public int length() {
		return this.pathElements.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathElements == null) ? 0 : pathElements.hashCode());
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
		Path other = (Path) obj;
		if (pathElements == null) {
			if (other.pathElements != null)
				return false;
		} else if (!pathElements.equals(other.pathElements))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (pathElements.isEmpty()) {
			return "[{}]";
		}
		StringBuilder sb = new StringBuilder();
		for (Tuple<AbstractRelationship, AggregationFunction> pathElement : pathElements) {
			sb.append(pathElement.getT().getFromTableName());
			sb.append("<-(" + pathElement.getU() + ")-");
			sb.append(pathElement.getT().getToTableName());
			sb.append("|");
		}
		return sb.substring(0, sb.length() - 1);
	}

}
