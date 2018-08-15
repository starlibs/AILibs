package autofe.db.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.AttributeType;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Table;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.DBUtils;
import autofe.db.util.SqlUtils;
import jaicore.basic.SQLAdapter;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class DatabaseConnectorImpl implements DatabaseConnector {

	private static boolean TABLE_EXISTS_WORKAROUND = true;

	private static Logger LOG = LoggerFactory.getLogger(DBUtils.class);

	private Database db;

	private SQLAdapter sqlAdapter;

	private Set<String> createdTableNames;

	public DatabaseConnectorImpl(Database db) {
		super();
		this.db = db;
		this.createdTableNames = new HashSet<>();
		setupSqlAdapter();
	}

	private void setupSqlAdapter() {
		String driver = db.getJdbcDriver();
		String host = db.getJdbcUrl() != null ? db.getJdbcUrl() : "";
		String user = db.getJdbcUsername() != null ? db.getJdbcUsername() : "";
		String password = db.getJdbcPassword() != null ? db.getJdbcPassword() : "";
		String database = db.getJdbcDatabase() != null ? db.getJdbcDatabase() : "";
		this.sqlAdapter = new SQLAdapter(driver, host, user, password, database, null, false);
	}

	@Override
	public Instances getInstances(List<AbstractFeature> features) {
		Instances instances = null;
		try {
			// Create feature tables (if not already existent)
			for (AbstractFeature feature : features) {
				if (TABLE_EXISTS_WORKAROUND) {
					LOG.info("Skip check whether Feature table for {} exists", feature);
					createFeatureTable(feature);
					continue;
				}
				if (!featureTableExists(feature)) {
					LOG.info("Feature table for {} does not exist => Creating", feature);
					createFeatureTable(feature);
				} else {
					LOG.info("Feature table for {} with name {} already exists", feature,
							SqlUtils.getTableNameForFeature(feature));
				}
			}

			// Join all feature tables
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * FROM ");
			for (int i = 0; i < features.size(); i++) {
				AbstractFeature feature = features.get(i);
				sql.append(SqlUtils.getTableNameForFeature(feature));
				if (i != features.size() - 1) {
					sql.append(" NATURAL JOIN ");
				}
			}
			// join with target table to get the target attribute
			Table targetTable = DBUtils.getTargetTable(db);
			autofe.db.model.database.Attribute primaryKey = DBUtils.getPrimaryKey(targetTable, db);
			autofe.db.model.database.Attribute target = DBUtils.getTargetAttribute(db);
			sql.append(String.format(" NATURAL JOIN (SELECT %1$s, %2$s FROM %3$s) TARGET", primaryKey.getName(),
					target.getName(), targetTable.getName()));

			instances = setupInstances(features, target);
			LOG.info("Loading instances from DB using sql: {}", sql);
			ResultSet rs = sqlAdapter.getResultsOfQuery(sql.toString());
			while (rs.next()) {
				Instance instance = createInstance(rs, features, target, instances);
				instances.add(instance);
			}
			rs.close();

		} catch (SQLException e) {
			LOG.error("Cannot get instances from database", e);
			throw new RuntimeException("Cannot get instances from database", e);
		}
		return instances;
	}

	private void createFeatureTable(AbstractFeature feature) throws SQLException {
		Table targetTarget = DBUtils.getTargetTable(db);
		Table featureTable = DBUtils.getAttributeTable(feature.getParent(), db);
		List<ForwardRelationship> joinRelations = DBUtils.getJoinTables(targetTarget, featureTable, db);
		if (feature instanceof ForwardFeature) {
			createForwardFeatureTable((ForwardFeature) feature);
		} else {
			createBackwardFeatureTable((BackwardFeature) feature);
		}
	}

	private void createForwardFeatureTable(ForwardFeature feature) throws SQLException {
		Table targetTable = DBUtils.getTargetTable(db);
		Table featureTable = DBUtils.getAttributeTable(feature.getParent(), db);
		List<ForwardRelationship> joinRelations = DBUtils.getJoinTables(targetTable, featureTable, db);
		LOG.debug("Join relations from {} to {} are {}", targetTable.getName(), featureTable.getName(), joinRelations);
		String featureSql = SqlUtils.generateForwardSql(joinRelations, feature, db);
		createTable(SqlUtils.getTableNameForFeature(feature), featureSql);
	}

	private void createBackwardFeatureTable(BackwardFeature feature) throws SQLException {
		Table targetTable = DBUtils.getTargetTable(db);
		Table toTable = DBUtils.getTableByName(feature.getPath().getLastTableName(), db);
		List<ForwardRelationship> joinRelations = DBUtils.getJoinTables(targetTable, toTable, db);
		LOG.debug("Join relations from {} to {} are {}", targetTable.getName(), toTable.getName(), joinRelations);
		String featureSql = SqlUtils.generateBackwardSql(joinRelations, feature, db);
		createTable(SqlUtils.getTableNameForFeature(feature), featureSql);
	}

	private void createTable(String tableName, String featureSql) throws SQLException {
		String sql = String.format("CREATE TABLE IF NOT EXISTS %s AS %s", tableName, featureSql);
		LOG.info("Creating feature table using statement {}", sql);
		sqlAdapter.update(sql, Collections.emptyList());
		createdTableNames.add(tableName);
	}

	private boolean featureTableExists(AbstractFeature feature) throws SQLException {
		boolean exists = false;
		String tableName = SqlUtils.getTableNameForFeature(feature);
		String sql = String.format("SELECT name FROM sqlite_master WHERE type='table' AND name='%s'", tableName);
		ResultSet rs = sqlAdapter.getResultsOfQuery(sql);
		if (rs.next()) {
			exists = true;
		}
		rs.close();
		return exists;
	}

	@Override
	public void cleanup() {
		for (String tableName : createdTableNames) {
			try {
				deleteTable(tableName);
			} catch (SQLException e) {
				LOG.error("Cannot delete table {}", tableName);
			}
		}

	}

	private void deleteTable(String tableName) throws SQLException {
		String sql = String.format("DROP TABLE %s", tableName);
		sqlAdapter.update(sql, Collections.emptyList());
	}

	private Instances setupInstances(List<AbstractFeature> features, autofe.db.model.database.Attribute target) {
		ArrayList<Attribute> wekaAttributes = new ArrayList<>();
		for (AbstractFeature feature : features) {
			if (feature.getType() == AttributeType.TEXT) {
				wekaAttributes.add(new Attribute(feature.getName(), true));
			} else if (feature.getType() == AttributeType.NUMERIC) {
				wekaAttributes.add(new Attribute(feature.getName(), false));
			} else {
				throw new RuntimeException("Unsupported attribute type " + feature.getType());
			}
		}

		// Add target
		if (target.getType() == AttributeType.TEXT) {
			wekaAttributes.add(new Attribute(target.getName(), true));
		} else if (target.getType() == AttributeType.NUMERIC) {
			wekaAttributes.add(new Attribute(target.getName(), false));
		} else {
			throw new RuntimeException("Unsupported attribute type for target: " + target.getType());
		}

		// TODO: How to set name and capacity?
		Instances instances = new Instances("Name", wekaAttributes, 1000);
		instances.setClassIndex(wekaAttributes.size() - 1);

		return instances;
	}

	private Instance createInstance(ResultSet rs, List<AbstractFeature> features,
			autofe.db.model.database.Attribute target, Instances instances) throws SQLException {
		Instance instance = new DenseInstance(features.size() + 1);
		instance.setDataset(instances);
		for (int i = 0; i < features.size(); i++) {
			AbstractFeature feature = features.get(i);
			if (feature.getType() == AttributeType.TEXT) {
				instance.setValue(i, rs.getString(i + 2));
			} else if (feature.getType() == AttributeType.NUMERIC) {
				instance.setValue(i, rs.getInt(i + 2));
			} else {
				throw new RuntimeException("Unsupoorted attribute type " + feature.getType());
			}
		}

		// Add class value (last column in result set)
		if (target.getType() == AttributeType.TEXT) {
			instance.setClassValue(rs.getString(features.size() + 2));
		} else if (target.getType() == AttributeType.NUMERIC) {
			instance.setClassValue(rs.getDouble(features.size() + 2));
		} else {
			throw new RuntimeException("Unsupoorted attribute type for target: " + target.getType());
		}

		return instance;
	}

	@Override
	public void close() {
		this.sqlAdapter.close();
	}

}
