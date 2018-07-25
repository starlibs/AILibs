package autofe.db.model.database;

import autofe.db.model.relation.AbstractRelationship;
import autofe.db.util.Tuple;

public class BackwardFeature extends AbstractFeature {

	public BackwardFeature(Attribute parent) {
		// TODO: Compute name for backward features
		super(parent.getName(), parent);
	}

	/** Path from the target table to the table containing this feature */
	private Tuple<AbstractRelationship, AggregationFunction> path;

	public Tuple<AbstractRelationship, AggregationFunction> getPath() {
		return path;
	}

	public void setPath(Tuple<AbstractRelationship, AggregationFunction> path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "BackwardFeature [path=" + path + ", name=" + name + ", parent=" + parent + "]";
	}

}
