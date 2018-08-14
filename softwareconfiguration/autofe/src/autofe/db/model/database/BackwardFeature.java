package autofe.db.model.database;

import java.util.List;

import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.Tuple;

public class BackwardFeature extends AbstractFeature {

	public BackwardFeature(Attribute parent) {
		super(parent);
		path = new Path();
	}

	public BackwardFeature(BackwardFeature toClone) {
		super(toClone.parent);
		this.path = new Path(toClone.getPath());
	}

	public BackwardFeature(Attribute parent, Path path) {
		super(parent);
		this.path = path;
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
	}

	@Override
	public String getName() {
		List<Tuple<AbstractRelationship, AggregationFunction>> pathElements = path.getPathElements();

		String name;

		if (pathElements == null || pathElements.isEmpty()) {
			name = parent.getFullName();
		} else {
			String parentTableName = pathElements.get(0).getT().getToTableName();
			name = String.format("%s.%s", parentTableName, parent.getFullName());

			for (Tuple<AbstractRelationship, AggregationFunction> pathElement : pathElements) {
				if (pathElement.getT() instanceof BackwardRelationship) {
					name = String.format("%s.%s(%s)", pathElement.getT().getFromTableName(), pathElement.getU(), name);
				} else if (pathElement.getT() instanceof ForwardRelationship) {
					name = String.format("%s.(%s)", pathElement.getT().getFromTableName(), name);
				}
			}
		}

		return "[B:" + name + "]";

	}

	@Override
	public AttributeType getType() {
		// Statically return numeric, could be changed in the future
		return AttributeType.NUMERIC;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		BackwardFeature other = (BackwardFeature) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BackwardFeature [path=" + path + ", parent=" + parent + ", getName()=" + getName() + "]";
	}

}
