package autofe.db.search;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.Database;

public class DatabaseGraphGenerator implements IGraphGenerator<DatabaseNode, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseGraphGenerator.class);
	private Database database;

	public DatabaseGraphGenerator(final Database database) {
		super();
		this.database = database;
	}

	@Override
	public ISingleRootGenerator<DatabaseNode> getRootGenerator() {
		return () -> {
			try {
				return new DatabaseNode();
			} catch (Exception e) {
				LOGGER.error("Could not create new database node ", e);
				return null;
			}
		};
	}

	@Override
	public ISuccessorGenerator<DatabaseNode, String> getSuccessorGenerator() {
		return new DatabaseSuccessorGenerator(this.database);
	}

	public Database getDatabase() {
		return this.database;
	}

}
