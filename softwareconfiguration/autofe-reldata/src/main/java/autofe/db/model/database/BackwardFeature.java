package autofe.db.model.database;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.Tuple;

public class BackwardFeature extends AbstractFeature {

	public BackwardFeature(final Attribute parent) {
		super(parent);
		this.path = new Path();
	}

	public BackwardFeature(final BackwardFeature toClone) {
		super(toClone.parent);
		this.path = new Path(toClone.getPath());
	}

	public BackwardFeature(final Attribute parent, final Path path) {
		super(parent);
		this.path = path;
	}

	/**
	 * Path from the table containing this feature to the target table or a forward
	 * reachable table
	 */
	private Path path;

	public Path getPath() {
		return this.path;
	}

	public void setPath(final Path path) {
		this.path = path;
	}

	@Override
	public String getName() {
		List<Tuple<AbstractRelationship, AggregationFunction>> pathElements = this.path.getPathElements();

		String name;

		if (pathElements == null || pathElements.isEmpty()) {
			name = this.parent.getFullName();
		} else {
			String parentTableName = pathElements.get(0).getT().getToTableName();
			name = String.format("%s.%s", parentTableName, this.parent.getName());

			for (Tuple<AbstractRelationship, AggregationFunction> pathElement : pathElements) {
				if (pathElement.getT() instanceof BackwardRelationship) {
					name = String.format("%s.%s(%s)", pathElement.getT().getFromTableName(), pathElement.getU(), name);
				} else if (pathElement.getT() instanceof ForwardRelationship) {
					name = String.format("%s.(%s)", pathElement.getT().getFromTableName(), name);
				}
			}
		}

		return name;

	}

	@Override
	public AttributeType getType() {
		// Statically return numeric, could be changed in the future
		return AttributeType.NUMERIC;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.path).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof BackwardFeature)) {
			return false;
		}

		BackwardFeature other = (BackwardFeature) obj;
		return new EqualsBuilder().append(this.path, other.path).isEquals();
	}

	@Override
	public String toString() {
		return "[B:" + this.getName() + "]";
	}

}
