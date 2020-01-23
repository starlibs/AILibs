package ai.libs.jaicore.ml.core.dataset.serialization;

import java.sql.SQLException;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.IInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

/**
 * This interface is meant to offer the ability to serialize and unserialize datasets from and to database tables.
 * The assumption is that the mapper has information about the database hose and the database itself, so that the
 * interface only allows to specify the table on which the operations take place.
 *
 * @author Felix Mohr
 *
 */
public interface ISQLDatasetMapper {

	public IDataset<?> readDatasetFromTable(String tableName) throws SQLException;

	public IInstanceSchema getInstanceSchemaOfTable(String tableName) throws SQLException;

	public ILabeledInstanceSchema getInstanceSchemaOfTable(String tableName, String labelField) throws SQLException;

	public ILabeledDataset<?> readDatasetFromTable(String tableName, String labelField) throws SQLException;

	public IDataset<?> readDatasetFromQuery(String sqlQuery) throws SQLException;

	public IInstanceSchema getInstanceSchemaForQuery(String sqlQuery) throws SQLException;

	public ILabeledInstanceSchema getInstanceSchemaForQuery(String sqlQuery, String labelField) throws SQLException;

	public ILabeledDataset<?> readDatasetFromQuery(String sqlQuery, String labelField) throws SQLException;

	public void writeDatasetToDatabase(IDataset<?> dataset, String tableName) throws SQLException;
}
