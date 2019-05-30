package jaicore.logic.fol.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.InterpretedLiteral;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.VariableParam;

public class LiteralStringParser {

	private static Pattern basicPattern = Pattern.compile("(!|~)?(.*)\\(([^\\)]*)\\)");

	private LiteralStringParser() {
		/* do nothing */
	}

	public static LiteralSet convertStringToLiteralSetWithConst(final String literalSetString, final Set<String> evaluablePredicates) {
		LiteralSet literalSet = new LiteralSet();

		String[] literals = literalSetString.split("&");
		if (!(literals.length == 1 && literals[0].isEmpty())) {
			for (int i = 0; i < literals.length; i++) {
				literalSet.add(convertStringToLiteralWithConst(literals[i], evaluablePredicates));
			}
		}
		return literalSet;
	}

	public static Literal convertStringToLiteralWithConst(final String literalString, final Set<String> evaluablePredicates) {
		String string = literalString.replace(" ", "");
		string = string.trim();

		Matcher matcher = basicPattern.matcher(string);
		if (!matcher.find()) {
			return null;
		}
		MatchResult results = matcher.toMatchResult();
		String predicateName = results.group(2); // position 2 is predicate name
		String[] paramsAsStrings = results.group(3).split(","); // position 3 are the variables
		List<LiteralParam> params = new LinkedList<>();
		for (int i = 0; i < paramsAsStrings.length; i++) {
			String param = paramsAsStrings[i].trim();
			params.add(param.startsWith("'") ? new ConstantParam(param.replace("'", "")) : new VariableParam(param));
		}

		/* try to match suffix of predicate name */
		if (evaluablePredicates.contains(predicateName)) {
			return new InterpretedLiteral(predicateName, params, results.group(1) == null);
		} else {
			return new Literal(predicateName, params, results.group(1) == null);
		}
	}
}
