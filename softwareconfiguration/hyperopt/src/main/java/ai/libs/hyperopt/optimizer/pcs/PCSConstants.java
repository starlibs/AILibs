package ai.libs.hyperopt.optimizer.pcs;

import java.util.regex.Pattern;

public class PCSConstants {

	public static final Pattern NUMERIC_PATTERN = Pattern.compile("([a-zA-Z0-9_\\-@\\.:;\\\\\\/?!$%&*+<>]*) *(\\[(-?[0-9]*e?-?[0-9]*\\.?[0-9]*e?-?[0-9]*), *(-?[0-9]*\\.?[0-9]*)]) *\\[([0-9]*\\.?[0-9]*)](i?l?)");

	public static final Pattern CATEGORICAL_PATTERN = Pattern.compile("([a-zA-Z0-9_\\-@\\.:;\\\\\\/?!$%&*+<>]*) *\\{([a-zA-Z0-9_\\-@\\.:;, \\\\\\/?!$%&*+<>]*)\\} *\\[([a-zA-Z0-9_\\-@\\.:;\\\\\\/?!$%&*+<>]*)\\]");

}
