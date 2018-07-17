package autofe.db.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import autofe.db.model.database.Database;
import autofe.db.model.database.Table;
import autofe.db.model.operation.BackwardAggregateOperation;
import autofe.db.model.operation.DatabaseOperation;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.util.DBUtils;
import autofe.db.util.SqlUtils;
import weka.core.Instances;

public class DatabaseConnectorImpl implements DatabaseConnector {

	private Database db;

	private Connection con;

	private List<String> createdViews;

	public DatabaseConnectorImpl(Database db) {
		this.db = db;
		createdViews = new ArrayList<>();
		try {
			initConnection();
		} catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException("Cannot establish JDBC connection", e);
		}
	}

	@Override
	public void prepareDatabase() {
		// Create view for each table
		for (Table t : db.getTables()) {
			String createViewStatement = SqlStatement.CREATE_VIEW_AS_COPY;
			String viewName = SqlUtils.getViewNameForTable(t);
			createViewStatement = SqlUtils.replacePlaceholder(createViewStatement, 1, viewName);
			createViewStatement = SqlUtils.replacePlaceholder(createViewStatement, 2, t.getName());
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				stmt.execute(createViewStatement);
			} catch (SQLException e) {
				String err = String.format("Cannot create view {} for table {}", viewName, t.getName());
				throw new RuntimeException(err, e);
			} finally {
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			createdViews.add(viewName);

		}
	}

	@Override
	public void applyOperations() {
		for (DatabaseOperation operation : db.getOperationHistory()) {
			if (operation instanceof BackwardAggregateOperation) {
				applyBackwardOperation((BackwardAggregateOperation) operation);
			}
		}

	}

	private void applyBackwardOperation(BackwardAggregateOperation operation) {
		Table from = DBUtils.getTableByName(operation.getFromTableName(), db);
		Table to = DBUtils.getTableByName(operation.getToBeAggregatedName(), db);

		// Generate temp view from current view of from table
		String fromViewName = SqlUtils.getViewNameForTable(from);
		String fromTempViewName = generateTempView(fromViewName);

		// Delete current view
		deleteView(fromViewName);

		// Create new current view
		String sql = SqlStatement.BACKWARD_AGGREGATION;
		sql = SqlUtils.replacePlaceholder(sql, 1, fromViewName);
		sql = SqlUtils.replacePlaceholder(sql, 2, operation.getAggregatedAttributeName());
		sql = SqlUtils.replacePlaceholder(sql, 3, fromTempViewName);
		sql = SqlUtils.replacePlaceholder(sql, 4, operation.getCommonAttributeName());
		sql = SqlUtils.replacePlaceholder(sql, 5, operation.getAggregationFunction().name());
		sql = SqlUtils.replacePlaceholder(sql, 6, operation.getToBeAggregatedName());
		sql = SqlUtils.replacePlaceholder(sql, 7, SqlUtils.getViewNameForTable(to));
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			String err = String.format("Cannot create view {}", fromViewName);
			throw new RuntimeException(err, e);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Delete temp view
		deleteView(fromTempViewName);
	}

	private String generateTempView(String viewName) {
		String sql = SqlStatement.CREATE_VIEW_AS_COPY;
		String tempViewName = SqlUtils.getTempViewName(viewName);
		SqlUtils.replacePlaceholder(sql, 1, tempViewName);
		SqlUtils.replacePlaceholder(sql, 2, viewName);
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			String err = String.format("Cannot create temp view {} from {}", tempViewName, viewName);
			throw new RuntimeException(err, e);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tempViewName;
	}

	@Override
	public Instances getInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanup() {
		// Delete all created views
		for (String viewName : this.createdViews) {
			deleteView(viewName);
		}

	}

	private void deleteView(String viewName) {
		String deleteViewStatement = SqlStatement.DELETE_VIEW;
		SqlUtils.replacePlaceholder(deleteViewStatement, 1, viewName);
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.execute(deleteViewStatement);
		} catch (SQLException e) {
			String err = String.format("Cannot delete view {}", viewName);
			throw new RuntimeException(err, e);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initConnection() throws ClassNotFoundException, SQLException {
		Class.forName(db.getJdbcDriver());
		con = DriverManager.getConnection(db.getJdbcUrl(), db.getJdbcUsername(), db.getJdbcPassword());
		con.setAutoCommit(false);
	}

}
