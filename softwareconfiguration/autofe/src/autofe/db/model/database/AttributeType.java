package autofe.db.model.database;

public enum AttributeType {
	
	NUMERIC(true),
	TEXT(false),
	DATE(false),
	ID(false);
	
	private boolean aggregable;

	private AttributeType(boolean aggregable) {
		this.aggregable = aggregable;
	}

	public boolean isAggregable() {
		return aggregable;
	}
	
	

}
