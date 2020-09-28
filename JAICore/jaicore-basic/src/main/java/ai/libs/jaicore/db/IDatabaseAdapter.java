package ai.libs.jaicore.db;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.kvstore.IKVStore;

/**
 * This is a simple util interface for easy database access and query execution in sql. You need to make sure that the respective JDBC connector is in the class path. By default, the adapter uses the mysql driver, but any jdbc driver can be
 * used.
 *
 * @author fmohr, mwever
 *
 */
public interface IDatabaseAdapter extends Serializable, AutoCloseable, ILoggingCustomizable {

	/**
	 * Checks whether the connection to the database is still alive and re-establishs the connection if it is not.
	 *
	 * @throws SQLException
	 *             Thrown, if there was an issue with reconnecting to the database server.
	 */
	public void checkConnection() throws SQLException;

	public boolean doesTableExist(final String tablename) throws SQLException, IOException;

	public void createTable(final String tablename, final String nameOfPrimaryField, final Collection<String> fieldnames, final Map<String, String> types, final Collection<String> keys) throws SQLException;

	/**
	 * Retrieves all rows of a table.
	 *
	 * @param table
	 *            The table for which all entries shall be returned.
	 * @return A list of {@link IKVStore}s containing the data of the table.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the connection to the database.
	 */
	default List<IKVStore> getRowsOfTable(final String table) throws SQLException {
		return this.getRowsOfTable(table, new HashMap<>());
	}

	/**
	 * Retrieves all rows of a table which satisfy certain conditions (WHERE clause).
	 *
	 * @param table
	 *            The table for which all entries shall be returned.
	 * @param conditions
	 *            The conditions a result entry must satisfy.
	 * @return A list of {@link IKVStore}s containing the data of the table.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the connection to the database.
	 */
	public List<IKVStore> getRowsOfTable(final String table, final Map<String, String> conditions) throws SQLException;

	/**
	 * Retrieves the select result for the given query.
	 *
	 * @param query
	 *            The SQL query which is to be executed.
	 * @return A list of {@link IKVStore}s containing the result data of the query.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the connection to the database.
	 */
	default List<IKVStore> getResultsOfQuery(final String query) throws SQLException {
		return this.getResultsOfQuery(query, new ArrayList<>());
	}

	/**
	 * Retrieves the select result for the given query that can have placeholders.
	 *
	 * @param query
	 *            The SQL query which is to be executed (with placeholders).
	 * @param values
	 *            An array of placeholder values that need to be filled in.
	 * @return A list of {@link IKVStore}s containing the result data of the query.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the connection to the database.
	 */
	default List<IKVStore> getResultsOfQuery(final String query, final String[] values) throws SQLException {
		return this.getResultsOfQuery(query, Arrays.asList(values));
	}

	/**
	 * Retrieves the select result for the given query that can have placeholders.
	 *
	 * @param query
	 *            The SQL query which is to be executed (with placeholders).
	 * @param values
	 *            A list of placeholder values that need to be filled in.
	 * @return A list of {@link IKVStore}s containing the result data of the query.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the query format or the connection to the database.
	 */
	public List<IKVStore> getResultsOfQuery(final String query, final List<String> values) throws SQLException;

	/**
	 * Executes an insert query
	 *
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	default int[] insert(final String sql) throws SQLException {
		return this.insert(sql, Arrays.asList());
	}

	/**
	 * Executes an insert query and returns the row ids of the created entries.
	 *
	 * @param sql
	 *            The insert statement which shall be executed that may have placeholders.
	 * @param values
	 *            The values for the placeholders.
	 * @return An array of the row ids of the inserted entries.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the query format or the connection to the database.
	 */
	default int[] insert(final String sql, final String[] values) throws SQLException {
		return this.insert(sql, Arrays.asList(values));
	}

	/**
	 * Executes an insert query and returns the row ids of the created entries.
	 *
	 * @param sql
	 *            The insert statement which shall be executed that may have placeholders.
	 * @param values
	 *            A list of values for the placeholders.
	 * @return An array of the row ids of the inserted entries.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the query format or the connection to the database.
	 */
	public int[] insert(final String sql, final List<? extends Object> values) throws SQLException;

	/**
	 * Creates and executes an insert query for the given table and the values as specified in the map.
	 *
	 * @param table
	 *            The table where to insert the data.
	 * @param map
	 *            The map of key:value pairs to be inserted into the table.
	 * @return An array of the row ids of the inserted entries.
	 * @throws SQLException
	 *             Thrown, if there was an issue with the query format or the connection to the database.
	 */
	public int[] insert(final String table, final Map<String, ? extends Object> map) throws SQLException;

	/**
	 * Creates a multi-insert statement and executes it. The returned array contains the row id's of the inserted rows. (By default it creates chunks of size 10.000 rows per query to be inserted.)
	 *
	 * @param table
	 *            The table to which the rows are to be added.
	 * @param keys
	 *            The list of column keys for which values are set.
	 * @param datarows
	 *            The list of value lists to be filled into the table.
	 * @return An array of row id's of the inserted rows.
	 * @throws SQLException
	 *             Thrown, if the sql statement was malformed, could not be executed, or the connection to the database failed.
	 */
	default int[] insertMultiple(final String table, final List<String> keys, final List<List<? extends Object>> datarows) throws SQLException {
		return this.insertMultiple(table, keys, datarows, 10000);
	}

	/**
	 * Creates a multi-insert statement and executes it. The returned array contains the row id's of the inserted rows.
	 *
	 * @param table
	 *            The table to which the rows are to be added.
	 * @param keys
	 *            The list of column keys for which values are set.
	 * @param datarows
	 *            The list of value lists to be filled into the table.
	 * @param chunkSize
	 *            The number of rows which are added within one single database transaction. (10,000 seems to be a good value for this)
	 * @return An array of row id's of the inserted rows.
	 * @throws SQLException
	 *             Thrown, if the sql statement was malformed, could not be executed, or the connection to the database failed.
	 */
	public int[] insertMultiple(final String table, final List<String> keys, final List<List<? extends Object>> datarows, final int chunkSize) throws SQLException;

	/**
	 * Execute the given sql statement as an update.
	 *
	 * @param sql
	 *            The sql statement to be executed.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException
	 *             Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	default int update(final String sql) throws SQLException {
		return this.update(sql, new ArrayList<String>());
	}

	/**
	 * Execute the given sql statement with placeholders as an update filling the placeholders with the given values beforehand.
	 *
	 * @param sql
	 *            The sql statement with placeholders to be executed.
	 * @param sql
	 *            Array of values for the respective placeholders.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException
	 *             Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	default int update(final String sql, final String[] values) throws SQLException {
		return this.update(sql, Arrays.asList(values));
	}

	/**
	 * Execute the given sql statement with placeholders as an update filling the placeholders with the given values beforehand.
	 *
	 * @param sql
	 *            The sql statement with placeholders to be executed.
	 * @param values
	 *            List of values for the respective placeholders.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException
	 *             Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	public int update(final String sql, final List<? extends Object> values) throws SQLException;

	/**
	 * Create and execute an update statement for some table updating the values as described in <code>updateValues</code> and only affect those entries satisfying the <code>conditions</code>.
	 *
	 * @param table
	 *            The table which is to be updated.
	 * @param updateValues
	 *            The description how entries are to be updated.
	 * @param conditions
	 *            The description of the where-clause, conditioning the entries which are to be updated.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException
	 *             Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	public int update(final String table, final Map<String, ? extends Object> updateValues, final Map<String, ? extends Object> conditions) throws SQLException;

	/**
	 * Deletes all rows from the table that match the given conditions
	 *
	 * @param table
	 * @param conditions
	 * @return the number of deleted rows.
	 * @throws SQLException
	 */
	public int delete(String table, Map<String, ? extends Object> conditions) throws SQLException;

	/**
	 * Executes the given statements atomically. Only works if no other statements are sent through this adapter in parallel! Only use for single-threaded applications, otherwise side effects may happen as this changes the auto commit
	 * settings of the connection temporarily.
	 *
	 * @param queries
	 *            The queries to execute atomically
	 * @throws SQLException
	 *             If the status of the connection cannot be changed. If something goes wrong while executing the given statements, they are rolled back before they are committed.
	 */
	public void executeQueriesAtomically(final List<PreparedStatement> queries) throws SQLException;

	/**
	 * Sends a query to the database server which can be an arbitrary query.
	 *
	 * @param sqlStatement
	 *            The sql statement to be executed.
	 * @return If there is a result set returned it will be parsed into a list of {@link IKVStore}s
	 * @throws SQLException
	 *             Thrown, if the the sql statement cannot be executed for whatever reasons.
	 * @throws IOException
	 *             Thrown, if the result set cannot be parsed into {@link IKVStore}s.
	 */
	public List<IKVStore> query(String sqlStatement) throws SQLException, IOException;

	/**
	 * Close the connection. No more queries can be sent after having the access object closed
	 */
	@Override
	public void close();

}
