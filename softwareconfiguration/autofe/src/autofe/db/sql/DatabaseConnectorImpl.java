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

	private static Logger LOG = LoggerFactory.getLogger(DBUtils.class);

	private Database db;

	private SQLAdapter sqlAdapter;

	private Set<String> createdTableNames;

	public DatabaseConnectorImpl(Database db) {
		super();
		this.db = db;
		this.createdTableNames = new HashSet<>();
		// TODO: Setup SQLAdapter properly
		this.sqlAdapter = new SQLAdapter(db.getJdbcDriver(), null, db.getJdbcUsername(), db.getJdbcPassword(),
				db.getJdbcUrl(), null, false);
	}

	@Override
	public Instances getInstances(List<AbstractFeature> features) {
		Instances instances = null;
		try {
			// Create feature tables (if not already existent)
			for (AbstractFeature feature : features) {
				if (!featureTableExists(feature)) {
					createFeatureTable(feature);
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

			instances = setupInstances(features);
			ResultSet rs = sqlAdapter.getResultsOfQuery(sql.toString());
			while (rs.next()) {
				Instance instance = createInstance(rs, features, instances);
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
		Table targetTarget = DBUtils.getTargetTable(db);
		Table featureTable = DBUtils.getAttributeTable(feature.getParent(), db);
		List<ForwardRelationship> joinRelations = DBUtils.getJoinTables(targetTarget, featureTable, db);
		String featureSql = SqlUtils.generateForwardSql(joinRelations, feature, db);
		createTable(SqlUtils.getTableNameForFeature(feature), featureSql);
	}

	private void createBackwardFeatureTable(BackwardFeature feature) throws SQLException {
		Table targetTarget = DBUtils.getTargetTable(db);
		Table toTable = DBUtils.getTableByName(feature.getPath().getLastTableName(), db);
		List<ForwardRelationship> joinRelations = DBUtils.getJoinTables(targetTarget, toTable, db);
		String featureSql = SqlUtils.generateBackwardSql(joinRelations, feature, db);
		createTable(SqlUtils.getTableNameForFeature(feature), featureSql);
	}

	private void createTable(String tableName, String featureSql) throws SQLException {
		String sql = String.format("CREATE TABLE %s AS %s", tableName, featureSql);
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

	private Instances setupInstances(List<AbstractFeature> features) {
		ArrayList<Attribute> wekaAttributes = new ArrayList<>();
		for (AbstractFeature feature : features) {
			if (feature.getType() == AttributeType.TEXT) {
				wekaAttributes.add(new Attribute(feature.getName(), true));
			} else if (feature.getType() == AttributeType.NUMERIC) {
				wekaAttributes.add(new Attribute(feature.getName(), false));
			} else {
				throw new RuntimeException("Unsupoorted attribute type " + feature.getType());
			}
		}

		// TODO: How to set name and capacity?
		return new Instances("Name", wekaAttributes, 1000);
	}

	private Instance createInstance(ResultSet rs, List<AbstractFeature> features, Instances instances)
			throws SQLException {
		Instance instance = new DenseInstance(features.size());
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

		return instance;
	}

}
