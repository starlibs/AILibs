package autofe.db.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.AbstractAttribute;
import autofe.db.model.database.AttributeType;
import autofe.db.model.database.Database;
import autofe.db.model.database.Table;
import autofe.db.model.operation.BackwardAggregateOperation;
import autofe.db.model.operation.DatabaseOperation;
import autofe.db.model.operation.ForwardJoinOperation;
import autofe.db.util.DBUtils;
import autofe.db.util.SqlUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class DatabaseConnectorImpl implements DatabaseConnector {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseConnectorImpl.class);

	private Database db;

	private DatabaseHelper dbHelper;

	private List<String> createdViews;

	public DatabaseConnectorImpl(Database db) {
		this.db = db;
		createdViews = new ArrayList<>();
		dbHelper = new DatabaseHelper(db);
	}

	@Override
	public void prepareDatabase() {
		// Create view for each table
		try {
			for (Table t : db.getTables()) {
				String createViewStatement = SqlStatement.CREATE_VIEW_AS_COPY;
				String viewName = SqlUtils.getViewNameForTable(t);
				createViewStatement = SqlUtils.replacePlaceholder(createViewStatement, 1, viewName);
				createViewStatement = SqlUtils.replacePlaceholder(createViewStatement, 2, t.getName());
				dbHelper.executeSql(createViewStatement);
				createdViews.add(viewName);
			}
		} catch (Exception e) {
			LOG.error("Cannot prepare database", e);
			dbHelper.rollback();
			throw e;
		}
		dbHelper.commit();
	}

	@Override
	public void applyOperations() {
		for (DatabaseOperation operation : db.getOperationHistory()) {
			if (operation instanceof BackwardAggregateOperation) {
				applyBackwardOperation((BackwardAggregateOperation) operation);
			} else if (operation instanceof ForwardJoinOperation) {
				applyForwardOperation((ForwardJoinOperation) operation);
			}
		}

	}

	private void applyBackwardOperation(BackwardAggregateOperation operation) {
		Table from = DBUtils.getTableByName(operation.getFromTableName(), db);
		Table to = DBUtils.getTableByName(operation.getToTableName(), db);

		// Generate temp view from current view of from table
		String fromViewName = SqlUtils.getViewNameForTable(from);

		try {

			String fromTempViewName = generateTempView(fromViewName);

			// Delete current view
			deleteView(fromViewName);

			// Create new current view
			String sql = SqlStatement.BACKWARD_AGGREGATION;
			sql = SqlUtils.replacePlaceholder(sql, 1, fromViewName);
			String aggregatedAttributeName = DBUtils.getAggregatedAttributeName(operation.getAggregationFunction(),
					operation.getToTableName(), operation.getToBeAggregatedName());
			sql = SqlUtils.replacePlaceholder(sql, 2, aggregatedAttributeName);
			sql = SqlUtils.replacePlaceholder(sql, 3, fromTempViewName);
			sql = SqlUtils.replacePlaceholder(sql, 4, operation.getCommonAttributeName());
			sql = SqlUtils.replacePlaceholder(sql, 5, operation.getAggregationFunction().name());
			sql = SqlUtils.replacePlaceholder(sql, 6, operation.getToBeAggregatedName());
			sql = SqlUtils.replacePlaceholder(sql, 7, SqlUtils.getViewNameForTable(to));

			// Fire SQL
			dbHelper.executeSql(sql);

			// Delete temp view
			deleteTable(fromTempViewName);

		} catch (Exception e) {
			LOG.error("Cannot apply backward operation", e);
			dbHelper.rollback();
			throw e;
		}

		dbHelper.commit();

	}

	private void applyForwardOperation(ForwardJoinOperation operation) {
		Table from = DBUtils.getTableByName(operation.getFromTableName(), db);
		Table to = DBUtils.getTableByName(operation.getToTableName(), db);

		// Generate temp view from current view of from table
		String fromViewName = SqlUtils.getViewNameForTable(from);

		try {

			String fromTempViewName = generateTempView(fromViewName);

			// Delete current view
			deleteView(fromViewName);

			// Create new current view
			String sql = SqlStatement.FORWARD_JOIN;
			sql = SqlUtils.replacePlaceholder(sql, 1, fromViewName);
			String fromName = SqlUtils.getTempViewName(fromViewName);
			String toName = SqlUtils.getViewNameForTable(to);
			sql = SqlUtils.replacePlaceholder(sql, 2, fromName);
			sql = SqlUtils.replacePlaceholder(sql, 3, toName);

			// Fire SQL
			dbHelper.executeSql(sql);

			// Delete temp view
			deleteTable(fromTempViewName);

		} catch (Exception e) {
			LOG.error("Cannot apply forward operation", e);
			dbHelper.rollback();
			throw e;
		}

		dbHelper.commit();
	}

	private String generateTempView(String viewName) {
		String sql = SqlStatement.CREATE_TABLE_AS_COPY;
		String tempViewName = SqlUtils.getTempViewName(viewName);
		sql = SqlUtils.replacePlaceholder(sql, 1, tempViewName);
		sql = SqlUtils.replacePlaceholder(sql, 2, viewName);
		dbHelper.executeSql(sql);
		return tempViewName;
	}

	@Override
	public Instances getInstances() {
		Table target = DBUtils.getTargetTable(db);
		String targetTableName = target.getName();

		// Collect feature attributes
		StringBuilder attributeString = new StringBuilder();
		List<AbstractAttribute> attributeList = new ArrayList<>();
		for (AbstractAttribute att : target.getColumns()) {
			// TODO: Add isFeature() method to attributes
			if (att.getType() != AttributeType.ID) {
				attributeString.append(att.getName() + ",");
				attributeList.add(att);
			}
		}

		// Remove final comma
		String attributeStr = attributeString.substring(0, attributeString.length() - 1);

		// Build SQL
		String sql = SqlStatement.LOAD_INSTANCES;
		sql = SqlUtils.replacePlaceholder(sql, 1, attributeStr);
		sql = SqlUtils.replacePlaceholder(sql, 2, targetTableName);

		// Fire SQL and create instances
		Instances instances = prepareInstances(attributeList);
		ResultSet rs = dbHelper.executeSelect(sql);
		try {
			while (rs.next()) {
				instances.add(createInstance(rs, attributeList, instances));
			}
			dbHelper.close(rs);
		} catch (SQLException e) {
			LOG.error("Cannot create instances!", e);
			throw new RuntimeException("Cannot create instances!", e);
		}
		return instances;
	}

	private Instances prepareInstances(List<AbstractAttribute> attributeList) {
		ArrayList<Attribute> wekaAttributes = new ArrayList<>();
		for (AbstractAttribute att : attributeList) {
			if (att.getType() == AttributeType.TEXT) {
				wekaAttributes.add(new Attribute(att.getName(), true));
			} else if (att.getType() == AttributeType.NUMERIC) {
				wekaAttributes.add(new Attribute(att.getName(), false));
			} else {
				throw new RuntimeException("Unsupported attribute type: " + att.getType());
			}
		}

		// TODO: How to handle capacity?
		return new Instances("Name", wekaAttributes, 1000);
	}

	private Instance createInstance(ResultSet rs, List<AbstractAttribute> attributeList, Instances instances)
			throws SQLException {
		Instance instance = new DenseInstance(attributeList.size());
		instance.setDataset(instances);
		for (int i = 0; i < attributeList.size(); i++) {
			AbstractAttribute att = attributeList.get(i);
			if (att.getType() == AttributeType.TEXT) {
				instance.setValue(i, rs.getString(i + 1));
			} else if (att.getType() == AttributeType.NUMERIC) {
				instance.setValue(i, rs.getInt(i + 1));
			} else {
				throw new RuntimeException("Unsupported attribute type: " + att.getType());
			}
		}

		return instance;
	}

	@Override
	public void cleanup() {
		// Delete all created views
		for (String viewName : this.createdViews) {
			deleteView(viewName);
		}
		dbHelper.commit();

	}

	@Override
	public void close() {
		this.dbHelper.closeConnection();

	}

	private void deleteView(String viewName) {
		String deleteViewStatement = SqlStatement.DELETE_VIEW;
		deleteViewStatement = SqlUtils.replacePlaceholder(deleteViewStatement, 1, viewName);
		dbHelper.executeSql(deleteViewStatement);
	}

	private void deleteTable(String tableName) {
		String deleteTableStatement = SqlStatement.DELETE_TABLE;
		deleteTableStatement = SqlUtils.replacePlaceholder(deleteTableStatement, 1, tableName);
		dbHelper.executeSql(deleteTableStatement);
	}

}
