package ai.libs.jaicore.ml.core.dataset.cache;

import java.io.Serializable;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.exception.DatasetTraceInstructionFailedException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Instruction class that can be converted into json. Used by {@link ReproducibleInstances}. The instructions are used to store information about the dataset origin and the splits done.
 * Supported are {@link LoadDataSetInstruction} and {@link FoldBasedSubsetInstruction} at the moment. <br>
 *
 * An instruction is identified by a command name, that specifies the type of instruction and a list if input parameters.
 *
 * @author jnowack, fmohr
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "command")
@JsonSubTypes({ @Type(value = LoadDataSetInstruction.class, name = "loadDataset"), @Type(value = LoadDatasetInstructionForOpenML.class), @Type(value = FoldBasedSubsetInstruction.class, name = "split"),
		@Type(value = StratifiedSplitSubsetInstruction.class) })
public abstract class Instruction implements Serializable {

	private static final long serialVersionUID = -3263546321197292929L;

	/**
	 * Provides the instances induced by this instruction node
	 *
	 * @return The instances computed by this node
	 */
	public abstract List<IDataset<?>> getOutputInstances(final List<IDataset<?>> inputs) throws DatasetTraceInstructionFailedException, InterruptedException;

	@Override
	public abstract Instruction clone();
}
