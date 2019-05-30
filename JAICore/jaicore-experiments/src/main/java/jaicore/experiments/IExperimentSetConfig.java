package jaicore.experiments;

import java.util.List;

import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

public interface IExperimentSetConfig extends Mutable, Reloadable {

	public static final String MEM_MAX = "mem.max";
	public static final String MEM_OPP = "mem.opp";
	public static final String CPU_MAX = "cpu.max";

		/* the key fields define the semantics of a single experiment */
	public static final String KEYFIELDS = "keyfields";

	/* the result fields define fields for results of each run */
	public static final String RESULTFIELDS = "resultfields";

	/* the fields for ignoring time and memory information */
	public static final String IGNORE_TIME = "ignore.time";
	public static final String IGNORE_MEMORY = "ignore.memory";

	@Key(MEM_MAX)
	public Integer getMemoryLimitInMB();

	@Key(MEM_OPP)
	public Integer getAssumedMemoryOverheadPerProcess();

	@Key(CPU_MAX)
	public Integer getNumberOfCPUs();

	@Key(KEYFIELDS)
	public List<String> getKeyFields();

	@Key(RESULTFIELDS)
	public List<String> getResultFields();

	@Key(IGNORE_TIME)
	public List<String> getFieldsForWhichToIgnoreTime();

	@Key(IGNORE_MEMORY)
	public List<String> getFieldsForWhichToIgnoreMemory();

}
