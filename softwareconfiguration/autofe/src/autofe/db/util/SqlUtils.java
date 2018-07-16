package autofe.db.util;

import autofe.db.model.database.Table;

public class SqlUtils {
	
	private static final String VIEW_PREFIX = "";

	private static final String VIEW_POSTFIX = "_VIEW";
	
	private static final String TEMP_POSTFIX = "_TMP";

	public static String replacePlaceholder(String in, int index, String replacement) {
		String placeholder = "$" + index;
		return in.replace(placeholder, replacement);
	}
	
	public static String getViewNameForTable(Table t) {
		return VIEW_PREFIX + t.getName() + VIEW_POSTFIX;
	}
	
	public static String getTempViewName(String viewName) {
		return viewName + TEMP_POSTFIX;
	}

}
