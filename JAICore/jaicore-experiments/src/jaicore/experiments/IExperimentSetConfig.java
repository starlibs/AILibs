package jaicore.experiments;

import java.util.List;

import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

public interface IExperimentSetConfig extends Mutable, Reloadable {

	public static final String MEM_MAX = "mem.max";
	public static final String MEM_OPP = "mem.opp";
	public static final String CPU_MAX = "cpu.max";

	/*
	 * experiment results must be stored in a database. The table is the one where
	 * the experiments will be maintained
	 */
	public static final String DB_DRIVER = "db.driver";
	public static final String DB_HOST = "db.host";
	public static final String DB_USER = "db.username";
	public static final String DB_PASS = "db.password";
	public static final String DB_NAME = "db.database";
	public static final String DB_TABLE = "db.table";
	public static final String DB_SSL = "db.ssl";

	/* the key fields define the semantics of a single experiment */
	public static final String KEYFIELDS = "keyfields";

	/* the result fields define fields for results of each run */
	public static final String RESULTFIELDS = "resultfields";

	/* the fields for ignoring time and memory information */
	public static final String IGNORE_TIME = "ignore.time";
	public static final String IGNORE_MEMORY = "ignore.memory";

	@Key(MEM_MAX)
	public int getMemoryLimitinMB();

	@Key(MEM_OPP)
	public int getAssumedMemoryOverheadPerProcess();

	@Key(CPU_MAX)
	public int getNumberOfCPUs();

	@Key(DB_HOST)
	public String getDBHost();

	@Key(DB_USER)
	public String getDBUsername();

	@Key(DB_PASS)
	public String getDBPassword();

	@Key(DB_NAME)
	public String getDBDatabaseName();

	@Key(DB_TABLE)
	public String getDBTableName();

	@Key(DB_SSL)
	public boolean getDBSSL();

	@Key(KEYFIELDS)
	public List<String> getKeyFields();

	@Key(RESULTFIELDS)
	public List<String> getResultFields();

	@Key(IGNORE_TIME)
	public List<String> getFieldsForWhichToIgnoreTime();

	@Key(IGNORE_MEMORY)
	public List<String> getFieldsForWhichToIgnoreMemory();

}
