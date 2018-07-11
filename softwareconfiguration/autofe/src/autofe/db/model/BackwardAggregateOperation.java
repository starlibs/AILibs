package autofe.db.model;

public class BackwardAggregateOperation implements DatabaseOperation {

	private BackwardRelationship backwardRelationship;

	private AggregationFunction aggregationFunction;

	private Attribute toBeAggregated;

	public BackwardAggregateOperation(BackwardRelationship backwardRelationship,
			AggregationFunction aggregationFunction, Attribute toBeAggregated) {
		super();
		this.backwardRelationship = backwardRelationship;
		this.aggregationFunction = aggregationFunction;
		this.toBeAggregated = toBeAggregated;
	}

	public BackwardRelationship getBackwardRelationship() {
		return backwardRelationship;
	}

	public void setBackwardRelationship(BackwardRelationship backwardRelationship) {
		this.backwardRelationship = backwardRelationship;
	}

	public AggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(AggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}

	public Attribute getToBeAggregated() {
		return toBeAggregated;
	}

	public void setToBeAggregated(Attribute toBeAggregated) {
		this.toBeAggregated = toBeAggregated;
	}

	@Override
	public void applyTo(Database db) {
		// New feature in from column
		AggregatedAttribute aggregatedAttribute = new AggregatedAttribute(getAggregatedAttributeName(),
				AttributeType.NUMERIC, toBeAggregated, aggregationFunction);
		backwardRelationship.getFrom().getColumns().add(aggregatedAttribute);

		db.getOperationHistory().add(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aggregationFunction == null) ? 0 : aggregationFunction.hashCode());
		result = prime * result + ((backwardRelationship == null) ? 0 : backwardRelationship.hashCode());
		result = prime * result + ((toBeAggregated == null) ? 0 : toBeAggregated.hashCode());
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
		BackwardAggregateOperation other = (BackwardAggregateOperation) obj;
		if (aggregationFunction != other.aggregationFunction)
			return false;
		if (backwardRelationship == null) {
			if (other.backwardRelationship != null)
				return false;
		} else if (!backwardRelationship.equals(other.backwardRelationship))
			return false;
		if (toBeAggregated == null) {
			if (other.toBeAggregated != null)
				return false;
		} else if (!toBeAggregated.equals(other.toBeAggregated))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BackwardAggregateOperation [backwardRelationship=" + backwardRelationship + ", aggregationFunction="
				+ aggregationFunction + ", toBeAggregated=" + toBeAggregated + "]";
	}

	public String getAggregatedAttributeName() {
		return aggregationFunction.name() + "(" + backwardRelationship.getTo().getName() + "." + toBeAggregated.getName() + ")";
	}

}
