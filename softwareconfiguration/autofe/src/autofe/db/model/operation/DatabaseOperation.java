package autofe.db.model.operation;

import autofe.db.model.database.Database;

public interface DatabaseOperation {

	void applyTo(Database db);
	
}
