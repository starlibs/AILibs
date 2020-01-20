package ai.libs.jaicore.basic.reconstruction;

import java.util.List;

import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;

public class ReconstructionUtil {

	private ReconstructionUtil() {
		/* empty constructor to avoid instantiation */
	}

	public static void requireNonEmptyInstructionsIfReconstructibilityClaimed(final Object object) {
		if (!areInstructionsNonEmptyIfReconstructibilityClaimed(object)) {
			throw new IllegalArgumentException("Object that is declared to be reconstructible does not carry any instructions.");
		}
	}

	public static boolean areInstructionsNonEmptyIfReconstructibilityClaimed(final Object object) {

		/* consistency check: check whether object, if reconstructible, already has a construction */
		if (object instanceof IReconstructible) {
			List<IReconstructionInstruction> instructions = ((IReconstructible) object).getConstructionPlan().getInstructions();
			if (instructions == null || instructions.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
