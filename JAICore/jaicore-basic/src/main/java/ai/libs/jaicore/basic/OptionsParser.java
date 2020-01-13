package ai.libs.jaicore.basic;

import ai.libs.jaicore.basic.kvstore.KVStore;

public class OptionsParser extends KVStore {

	private static final String OPTION_PREFIX_1 = "-";
	private static final String OPTION_PREFIX_2 = "--";

	/**
	 *
	 */
	private static final long serialVersionUID = 2868523552636687194L;

	public OptionsParser(final String optionsString) {
		String[] optionsSplit = optionsString.trim().split(" ");
		for (int i = 0; i < optionsSplit.length; i++) {
			String currentKey = optionsSplit[i].trim();
			String nextValue = null;
			if (i < optionsSplit.length - 1) {
				nextValue = optionsSplit[i + 1].trim();
			}

			if (this.isOptionKey(currentKey)) {
				if (this.isOptionKey(nextValue)) {
					this.put(this.stripOptionKey(currentKey), "true");
				} else {
					this.put(this.stripOptionKey(currentKey), nextValue);
				}
			}
		}
	}

	private boolean isOptionKey(final String string) {
		if (string == null) {
			return false;
		}
		return (string.startsWith(OPTION_PREFIX_1) || string.startsWith(OPTION_PREFIX_2));
	}

	private String stripOptionKey(final String optionKey) {
		if (optionKey.startsWith(OPTION_PREFIX_2)) {
			return optionKey.substring(OPTION_PREFIX_2.length());
		} else if (optionKey.startsWith(OPTION_PREFIX_1)) {
			return optionKey.substring(OPTION_PREFIX_1.length());
		}
		return optionKey;
	}

}
