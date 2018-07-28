package autofe.db.model.database;

import java.util.ArrayList;
import java.util.List;

import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
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
		updateName();
	}

	public void addToPath(AbstractRelationship edge, AggregationFunction aggregationFunction) {
		path.add(new Tuple<AbstractRelationship, AggregationFunction>(edge, aggregationFunction));
		updateName();
	}

	private void updateName() {
		if (path == null || path.isEmpty()) {
			this.name = parent.getName();
			return;
		}

		String parentTableName = path.get(0).getT().getToTableName();
		String updatedName = String.format("%s.%s", parentTableName, parent.getName());

		for (Tuple<AbstractRelationship, AggregationFunction> pathElement : path) {
			if (pathElement.getT() instanceof BackwardRelationship) {
				updatedName = String.format("%s.%s(%s)", pathElement.getT().getFromTableName(), pathElement.getU(),
						updatedName);
			} else if (pathElement.getT() instanceof ForwardRelationship) {
				updatedName = String.format("%s<-(%s)", pathElement.getT().getFromTableName(), updatedName);
			}
		}

		this.name = updatedName;

	}

	@Override
	public String toString() {
		return "BackwardFeature [path=" + path + ", name=" + name + ", parent=" + parent + "]";
	}

}
