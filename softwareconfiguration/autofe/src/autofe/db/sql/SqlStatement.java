package autofe.db.sql;

public class SqlStatement {
	
	public static final String CREATE_VIEW_AS_COPY = "CREATE VIEW $1 AS SELECT * FROM $2";
	
	public static final String CREATE_TABLE_AS_COPY = "CREATE TABLE $1 AS SELECT * FROM $2";
	
	public static final String DELETE_VIEW = "DROP VIEW $1";
	
	public static final String DELETE_TABLE = "DROP TABLE $1";
	
	public static final String BACKWARD_AGGREGATION = "CREATE VIEW $1 AS SELECT f.*,t.AGG as '$2' FROM $3 f JOIN (SELECT $4,$5($6) AS AGG FROM $7 GROUP BY ($4) t ON (f.$4 = t.$4)";

	public static final String FORWARD_JOIN = "CREATE VIEW $1 AS SELECT * FROM $2 NATURAL JOIN $3";
	
	public static final String LOAD_INSTANCES = "SELECT $1 FROM $2";
}
