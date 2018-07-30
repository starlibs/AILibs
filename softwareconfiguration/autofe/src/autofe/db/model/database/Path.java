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

	public Table getLastTable() {
		if (pathElements.isEmpty()) {
			return null;
		}
		Tuple<AbstractRelationship, AggregationFunction> lastPathElement = pathElements.get(pathElements.size() - 1);
		return lastPathElement.getT().getFrom();
	}

}
