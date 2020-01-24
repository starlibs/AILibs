package ai.libs.mlplan.multilabel.mekamlplan;

/**
 * Static path configuration for ML2-Plan. Resource files are prefixed with RES_. However, resource files only function as back-up solutions. If there is a configuration file on the standard file path marked with FS_ this configuration will be preferred over the configuration in the resource files.
 * @author mwever
 *
 */
public class ML2PlanMekaPathConfig {

	/**
	 * Default path for the search space config (SSC) in the resources:
	 */
	public static final String RES_SSC = "automl/searchmodels/meka/mlplan-meka.json";

	/**
	 * Path to the search space config (SSC) in the standard file system:
	 */
	public static final String FS_SSC = "conf/searchmodels/mlplan-meka.json";

	public static final String RES_PREFC = "mlplan/meka-preferenceList.txt";

	public static final String FS_PREFC = "conf/mlpan-meka-preferenceList.txt";

}
