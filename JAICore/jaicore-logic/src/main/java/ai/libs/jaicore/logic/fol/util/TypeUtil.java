package ai.libs.jaicore.logic.fol.util;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.TypeModule;

public class TypeUtil {

	private TypeUtil() {
		/* avoid instantiation */
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TypeUtil.class);

	public static final String GODFATHER_TYPE = "Thing";

	private static TypeModule typeMod;

	public static void setTypeModule(final TypeModule typeModule) {
		TypeUtil.typeMod = typeModule;
	}

	public static void defineGodfatherDataTypes(final Literal l) {
		checkTypeModule();
		for (LiteralParam p : l.getParameters()) {
			if (p.getType() == null) {
				p.setType(typeMod.getType(GODFATHER_TYPE));
			}
		}
	}

	public static void defineGodfatherDataTypes(final Set<? extends Literal> m) {
		for (Literal l : m) {
			defineGodfatherDataTypes(l);
		}
	}

	public static void defineGodfatherDataTypes(final List<? extends Literal> m) {
		for (Literal l : m) {
			defineGodfatherDataTypes(l);
		}
	}

	private static void checkTypeModule() {
		if (typeMod == null) {
			typeMod = new TypeModule();
			LOGGER.warn("TypeModule in DataTypeUtil has not been set. Now, operating on own TypeModule");
		}
	}

}
