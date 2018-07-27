package autofe.db.model.database;

import java.util.ArrayList;
import java.util.List;

import autofe.db.model.relation.AbstractRelationship;
import autofe.db.util.Tuple;

public class BackwardFeature extends AbstractFeature {

	public BackwardFeature(Attribute parent) {
		// TODO: Compute name for backward features
		super(parent.getName(), parent);
		path = new ArrayList<>();
	}

	/**
	 * Path from the table containing this feature to the target table or a forward
	 * reachable table
	 */
	private List<Tuple<AbstractRelationship, AggregationFunction>> path;

	public List<Tuple<AbstractRelationship, AggregationFunction>> getPath() {
		return path;
	}

	public void setPath(List<Tuple<AbstractRelationship, AggregationFunction>> path) {
		this.path = path;
	}

	public void addToPath(AbstractRelationship edge, AggregationFunction aggregationFunction) {
		path.add(new Tuple<AbstractRelationship, AggregationFunction>(edge, aggregationFunction));
	}

	@Override
	public String toString() {
		return "BackwardFeature [path=" + path + ", name=" + name + ", parent=" + parent + "]";
	}

}
