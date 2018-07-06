package autofe.db.model;

public class AggregatedAttribute extends Attribute {
	
	private Attribute parent;
	
	private AggregationFunction aggregationFunction;

	public AggregatedAttribute(String name, AttributeType type, Attribute parent,
			AggregationFunction aggregationFunction) {
		super(name, type);
		this.parent = parent;
		this.aggregationFunction = aggregationFunction;
	}

	public Attribute getParent() {
		return parent;
	}

	public void setParent(Attribute parent) {
		this.parent = parent;
	}

	public AggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(AggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
	


}
