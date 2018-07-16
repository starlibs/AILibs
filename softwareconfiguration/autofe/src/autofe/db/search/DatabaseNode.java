package autofe.db.search;

import autofe.db.model.database.Database;

public class DatabaseNode {

	private Database database;

	public DatabaseNode(Database database) {
		super();
		this.database = database;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

}
