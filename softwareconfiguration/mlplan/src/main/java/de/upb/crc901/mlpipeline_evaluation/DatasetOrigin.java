package de.upb.crc901.mlpipeline_evaluation;

/**
 * Allowed origins for datasets. Must be in db mapping table.
 * 
 * @author Helena Graf
 * @author Lukas
 * @author Joshua
 *
 */
public enum DatasetOrigin {
	OPENML_DATASET_ID, CLUSTER_LOCATION_NEW, LOCAL;

	/**
	 * Maps the dataset origin to its column name in the database for the dataset
	 * equivalence mappings.
	 * 
	 * @param origin
	 *            The dataset origin to map
	 * @return The column name for the dataset origin
	 */
	@SuppressWarnings("incomplete-switch")
	static String mapOriginToColumnIdentifier(DatasetOrigin origin) {
		switch (origin) {
		case OPENML_DATASET_ID:
			return "openML_dataset_id";
		case CLUSTER_LOCATION_NEW:
			return "cluster_location_new";
		}
		throw new IllegalArgumentException("Invalid dataset origin.");
	}
}
