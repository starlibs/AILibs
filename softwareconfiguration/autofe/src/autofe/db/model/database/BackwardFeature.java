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
		path = new Path();
	}

	public BackwardFeature(BackwardFeature toClone) {
		super(toClone.name, toClone.parent);
		this.path = new Path(toClone.getPath());
	}

	/**
	 * Path from the table containing this feature to the target table or a forward
	 * reachable table
	 */
	private Path path;

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
		updateName();
	}

	private void updateName() {
		List<Tuple<AbstractRelationship, AggregationFunction>> pathElements = path.getPathElements();
		
		if (pathElements == null || pathElements.isEmpty()) {
			this.name = parent.getName();
			return;
		}

		String parentTableName = pathElements.get(0).getT().getToTableName();
		String updatedName = String.format("%s.%s", parentTableName, parent.getName());

		for (Tuple<AbstractRelationship, AggregationFunction> pathElement : pathElements) {
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
