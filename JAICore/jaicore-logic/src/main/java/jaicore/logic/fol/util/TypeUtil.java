package jaicore.logic.fol.util;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.TypeModule;

public class TypeUtil {
	private final static Logger LOGGER = LoggerFactory.getLogger(TypeUtil.class);

	public final static String GODFATHER_TYPE = "Thing";

	private static TypeModule typeMod;

	public static void setTypeModule(final TypeModule typeModule) {
		TypeUtil.typeMod = typeModule;
	}

	public static void defineGodfatherDataTypes(final Literal l) {
		checkTypeModule();
		for (LiteralParam p : l.getParameters()) {
			LiteralParam pCast = (LiteralParam)p;
			if (pCast.getType() == null) {
				pCast.setType(typeMod.getType(GODFATHER_TYPE));
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
