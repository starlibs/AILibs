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
	public Database applyTo(Database db) {
		// Check for references
		// TODO: Necessary?
		if (!db.getTables().contains(backwardRelationship.getFrom())
				|| !db.getTables().contains(backwardRelationship.getTo())) {
			throw new RuntimeException("References are incorrect!");
		}

		// New feature in from column
		AggregatedAttribute aggregatedAttribute = new AggregatedAttribute("TBD", null, toBeAggregated,
				aggregationFunction);
		backwardRelationship.getFrom().getColumns().add(aggregatedAttribute);

		db.getOperationHistory().add(this);

		return db;
	}

}
