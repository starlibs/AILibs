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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;

public class DatabaseConnectorImpl implements DatabaseConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectorImpl.class);

	private static final boolean TABLE_EXISTS_WORKAROUND = true;

	private Database db;
	private SQLAdapter sqlAdapter;
	private Set<String> createdTableNames;

	public DatabaseConnectorImpl(final Database db) {
		super();
		this.db = db;
		this.createdTableNames = new HashSet<>();
		this.setupSqlAdapter();
	}

	private void setupSqlAdapter() {
		String driver = this.db.getJdbcDriver();
		String host = this.db.getJdbcUrl() != null ? this.db.getJdbcUrl() : "";
		String user = this.db.getJdbcUsername() != null ? this.db.getJdbcUsername() : "";
		String password = this.db.getJdbcPassword() != null ? this.db.getJdbcPassword() : "";
		String database = this.db.getJdbcDatabase() != null ? this.db.getJdbcDatabase() : "";
		this.sqlAdapter = new SQLAdapter(driver, host, user, password, database, null, false);
	}

	@Override
	public Instances getInstances(final List<AbstractFeature> features) throws RetrieveInstancesFromDatabaseFailedException {
		if (features == null || features.isEmpty()) {
			throw new IllegalArgumentException("Empty feature list provided!");
		}
		Instances instances = null;
		try {
			// Create feature tables (if not already existent)
			for (AbstractFeature feature : features) {
				if (TABLE_EXISTS_WORKAROUND) {
					LOGGER.debug("Skip check whether Feature table for {} exists", feature);
					this.createFeatureTable(feature);
					continue;
				}
				if (!this.featureTableExists(feature)) {
					LOGGER.debug("Feature table for {} does not exist => Creating", feature);
					this.createFeatureTable(feature);
				} else {
					LOGGER.debug("Feature table for {} with name {} already exists", feature, SqlUtils.getTableNameForFeature(feature));
				}
			}

			// Join all feature tables
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * FROM ");
			for (int i = 0; i < features.size(); i++) {
				AbstractFeature feature = features.get(i);
				sql.append("`" + SqlUtils.getTableNameForFeature(feature) + "`");
				if (i != features.size() - 1) {
					sql.append(" NATURAL JOIN ");
				}
			}
			// join with target table to get the target attribute
			Table targetTable = DBUtils.getTargetTable(this.db);
			autofe.db.model.database.Attribute primaryKey = DBUtils.getPrimaryKey(targetTable);
			autofe.db.model.database.Attribute target = DBUtils.getTargetAttribute(this.db);
			sql.append(String.format(" NATURAL JOIN (SELECT %1$s, %2$s FROM %3$s) TARGET", primaryKey.getName(), target.getName(), targetTable.getName()));

			instances = this.setupInstances(features, target);
			LOGGER.debug("Loading instances from DB using sql: {}", sql);
			ResultSet rs = this.sqlAdapter.getResultsOfQuery(sql.toString());
			while (rs.next()) {
				Instance instance = this.createInstance(rs, features, target, instances);
				instances.add(instance);
			}
			rs.close();
			instances = this.finalizeInstances(instances);

		} catch (Exception e) {
			throw new RetrieveInstancesFromDatabaseFailedException("Cannot get instances from database", e);
		}
		return instances;
	}

	private Instances finalizeInstances(final Instances toFinalize) throws Exception {
		Instances toReturn = toFinalize;

		// Convert string attributes to nominal attributes
		StringToNominal stringToNominal = new StringToNominal();
		stringToNominal.setInputFormat(toFinalize);
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "first-last";
		stringToNominal.setOptions(options);
		toReturn = Filter.useFilter(toReturn, stringToNominal);

		// Transform numeric attribute to nominal
		if (toReturn.classAttribute().isNumeric()) {
			NumericToNominal numericToNominal = new NumericToNominal();
			numericToNominal.setInputFormat(toReturn);
			options = new String[2];
			options[0] = "-R";
			options[1] = "first-last";
			numericToNominal.setOptions(options);
			toReturn = Filter.useFilter(toReturn, numericToNominal);
		}

		return toReturn;
	}

	private void createFeatureTable(final AbstractFeature feature) throws SQLException {
		if (feature instanceof ForwardFeature) {
			this.createForwardFeatureTable((ForwardFeature) feature);
		} else {
			this.createBackwardFeatureTable((BackwardFeature) feature);
		}
	}

	private void createForwardFeatureTable(final ForwardFeature feature) throws SQLException {
		Table targetTable = DBUtils.getTargetTable(this.db);
		Table featureTable = DBUtils.getAttributeTable(feature.getParent(), this.db);
		List<ForwardRelationship> joinRelations = DBUtils.getJoinTables(targetTable, featureTable, this.db);
		LOGGER.debug("Join relations from {} to {} are {}", targetTable.getName(), featureTable.getName(), joinRelations);
		String featureSql = SqlUtils.generateForwardSql(joinRelations, feature, this.db);
		this.createTable(SqlUtils.getTableNameForFeature(feature), featureSql);
	}

	private void createBackwardFeatureTable(final BackwardFeature feature) throws SQLException {
		Table targetTable = DBUtils.getTargetTable(this.db);
		Table toTable = DBUtils.getTableByName(feature.getPath().getLastTableName(), this.db);
		List<ForwardRelationship> joinRelations = DBUtils.getJoinTables(targetTable, toTable, this.db);
		LOGGER.debug("Join relations from {} to {} are {}", targetTable.getName(), toTable.getName(), joinRelations);
		String featureSql = SqlUtils.generateBackwardSql(joinRelations, feature, this.db);
		this.createTable(SqlUtils.getTableNameForFeature(feature), featureSql);
	}

	private void createTable(final String tableName, final String featureSql) throws SQLException {
		String sql = String.format("CREATE TABLE IF NOT EXISTS `%s` AS %s", tableName, featureSql);
		LOGGER.debug("Creating feature table using statement {}", sql);
		this.sqlAdapter.update(sql, Collections.emptyList());
		this.createdTableNames.add(tableName);
	}

	private boolean featureTableExists(final AbstractFeature feature) throws SQLException {
		boolean exists = false;
		String tableName = SqlUtils.getTableNameForFeature(feature);
		String sql = String.format("SELECT name FROM sqlite_master WHERE type='table' AND name='%s'", tableName);
		ResultSet rs = this.sqlAdapter.getResultsOfQuery(sql);
		if (rs.next()) {
			exists = true;
		}
		rs.close();
		return exists;
	}

	@Override
	public void cleanup() {
		for (String tableName : this.createdTableNames) {
			try {
				this.deleteTable(tableName);
			} catch (SQLException e) {
				LOGGER.error("Cannot delete table {}", tableName);
			}
		}

	}

	private void deleteTable(final String tableName) throws SQLException {
		String sql = String.format("DROP TABLE `%s`", tableName);
		this.sqlAdapter.update(sql, Collections.emptyList());
	}

	private Instances setupInstances(final List<AbstractFeature> features, final autofe.db.model.database.Attribute target) throws UnsupportedAttributeTypeException {
		ArrayList<Attribute> wekaAttributes = new ArrayList<>();
		for (AbstractFeature feature : features) {
			if (feature.getType() == AttributeType.TEXT) {
				wekaAttributes.add(new Attribute(feature.getName(), true));
			} else if (feature.getType() == AttributeType.NUMERIC) {
				wekaAttributes.add(new Attribute(feature.getName(), false));
			} else {
				throw new UnsupportedAttributeTypeException("Unsupported attribute type " + feature.getType());
			}
		}

		// Add target
		if (target.getType() == AttributeType.TEXT) {
			wekaAttributes.add(new Attribute(target.getName(), true));
		} else if (target.getType() == AttributeType.NUMERIC) {
			wekaAttributes.add(new Attribute(target.getName(), false));
		} else {
			throw new UnsupportedAttributeTypeException("Unsupported attribute type for target " + target.getType());
		}

		Instances instances = new Instances("Name", wekaAttributes, 0);
		instances.setClassIndex(wekaAttributes.size() - 1);

		return instances;
	}

	private Instance createInstance(final ResultSet rs, final List<AbstractFeature> features, final autofe.db.model.database.Attribute target, final Instances instances) throws SQLException, UnsupportedAttributeTypeException {
		Instance instance = new DenseInstance(features.size() + 1);
		instance.setDataset(instances);
		for (int i = 0; i < features.size(); i++) {
			AbstractFeature feature = features.get(i);
			if (feature.getType() == AttributeType.TEXT) {
				String value = rs.getString(i + 2);
				if (value != null) {
					instance.setValue(i, value);
				}
			} else if (feature.getType() == AttributeType.NUMERIC) {
				long value = rs.getLong(i + 2);
				if (!rs.wasNull()) {
					instance.setValue(i, value);
				}
			} else {
				throw new UnsupportedAttributeTypeException("Unsupported attribute type " + feature.getType());
			}
		}

		// Add class value (last column in result set)
		if (target.getType() == AttributeType.TEXT)

		{
			instance.setClassValue(rs.getString(features.size() + 2));
		} else if (target.getType() == AttributeType.NUMERIC) {
			instance.setClassValue(rs.getDouble(features.size() + 2));
		} else {
			throw new UnsupportedAttributeTypeException("Unsupported attribute type for target " + target.getType());
		}

		return instance;
	}

	@Override
	public void close() {
		this.sqlAdapter.close();
	}

}
