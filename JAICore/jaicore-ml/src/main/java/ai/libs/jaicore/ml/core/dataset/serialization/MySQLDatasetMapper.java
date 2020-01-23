package ai.libs.jaicore.ml.core.dataset.serialization;

import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.IInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.datastructure.kvstore.IKVStore;

import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.dataset.DenseInstance;
import ai.libs.jaicore.ml.core.dataset.schema.InstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;

public class MySQLDatasetMapper implements ISQLDatasetMapper {

	private final IDatabaseAdapter adapter;

	public MySQLDatasetMapper(final IDatabaseAdapter adapter) {
		super();
		this.adapter = adapter;
	}

	private String getTableSelectQuery(final String tableName) {
		return "SELECT * FROM `" + tableName + "`";
	}

	@Override
	public IDataset<?> readDatasetFromTable(final String tableName) throws SQLException {
		return this.readDatasetFromQuery(this.getTableSelectQuery(tableName));
	}

	@Override
	public ILabeledDataset<?> readDatasetFromTable(final String tableName, final String labelField) throws SQLException {
		return this.readDatasetFromQuery(this.getTableSelectQuery(tableName), labelField);
	}

	@Override
	public IDataset<?> readDatasetFromQuery(final String sqlQuery) throws SQLException {
		throw new UnsupportedOperationException("Can currently only handle labeled data");
	}

	@Override
	public ILabeledDataset<?> readDatasetFromQuery(final String sqlQuery, final String labelField) throws SQLException {
		List<IKVStore> relation = this.adapter.getResultsOfQuery(sqlQuery);
		ILabeledInstanceSchema schema = this.getInstanceSchemaFromResultList(relation, labelField);
		Dataset ds = new Dataset(schema);
		for (IKVStore row : relation) {
			List<Object> attributeValues = new ArrayList<>();
			for (IAttribute attribute : schema.getAttributeList()) {
				if (attribute.getName().equals(labelField)) {
					continue;
				}
				Object receivedVal = row.get(attribute.getName());
				Object convertedVal = receivedVal == null ? null : attribute.getAsAttributeValue(receivedVal).getValue() ;// maybe the received val is a number hidden in a string
				attributeValues.add(convertedVal);
			}
			Object labelAttribute = schema.getLabelAttribute().getAsAttributeValue(row.get(labelField)).getValue();
			ds.add(new DenseInstance(attributeValues, labelAttribute));
		}
		return ds;
	}

	@Override
	public void writeDatasetToDatabase(final IDataset<?> dataset, final String tableName) {
		throw new UnsupportedOperationException("Currently, only read access to the database is supported.");
	}

	@Override
	public IInstanceSchema getInstanceSchemaOfTable(final String tableName) throws SQLException {
		return this.getInstanceSchemaForQuery(this.getTableSelectQuery(tableName));
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchemaOfTable(final String tableName, final String labelField) throws SQLException {
		return this.getInstanceSchemaForQuery(this.getTableSelectQuery(tableName), labelField);
	}

	@Override
	public IInstanceSchema getInstanceSchemaForQuery(final String sqlQuery) throws SQLException {
		return this.getInstanceSchemaFromResultList(this.adapter.getResultsOfQuery(sqlQuery));
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchemaForQuery(final String sqlQuery, final String labelField) throws SQLException {
		return this.convertInstanceSchemaIntoLabeledInstanceSchema(this.getInstanceSchemaForQuery(sqlQuery), labelField);
	}

	public IInstanceSchema getInstanceSchemaFromResultList(final List<IKVStore> data) {
		IKVStore firstRow = data.get(0);
		List<IAttribute> attributeList = new ArrayList<>(firstRow.size());
		for (Entry<String, Object> serializedAttribute : firstRow.entrySet()) {
			Object val = serializedAttribute.getValue();
			String key = serializedAttribute.getKey();
			if (val == null) {
				continue;
			}
			if (val instanceof Number || (val instanceof String && NumberUtils.isCreatable((String)val))) {
				attributeList.add(new NumericAttribute(key));
			}
			else if (val instanceof String || val instanceof Time) {
				Set<Object> availableValues = data.stream().map(r -> r.get(key)).filter(Objects::nonNull).collect(Collectors.toSet());
				attributeList.add(new IntBasedCategoricalAttribute(key, availableValues.stream().map(Object::toString).collect(Collectors.toList())));
			}
			else {
				throw new UnsupportedOperationException("Cannot recognize type of attribute " + key + " with value " + val + " of type " + val.getClass().getName());
			}
		}
		return new InstanceSchema("SQL-mapped data", attributeList);
	}

	public ILabeledInstanceSchema convertInstanceSchemaIntoLabeledInstanceSchema(final IInstanceSchema schema, final String labelField) {
		List<IAttribute> allAttributes = new ArrayList<>(schema.getAttributeList());
		IAttribute targetAttribute = allAttributes.stream().filter(a -> a.getName().equals(labelField)).findAny().get();
		allAttributes.remove(targetAttribute);
		return new LabeledInstanceSchema(schema.getRelationName(), allAttributes, targetAttribute);
	}

	public ILabeledInstanceSchema getInstanceSchemaFromResultList(final List<IKVStore> data, final String labelField) {
		return this.convertInstanceSchemaIntoLabeledInstanceSchema(this.getInstanceSchemaFromResultList(data), labelField);

	}
}
