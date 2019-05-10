package jaicore.basic.kvstore;

import java.util.Comparator;

public class KVStoreSequentialComparator implements Comparator<KVStore> {

	private final String[] sortKeys;

	public KVStoreSequentialComparator(final String... sortKeys) {
		this.sortKeys = sortKeys;
	}

	@Override
	public int compare(final KVStore arg0, final KVStore arg1) {
		for (String sortKey : this.sortKeys) {
			try {
				int compareInt = arg0.getAsInt(sortKey).compareTo(arg1.getAsInt(sortKey));
				if (compareInt == 0) {
					continue;
				}
				return compareInt;
			} catch (Exception e) {
			}
			try {
				int compareLong = arg0.getAsLong(sortKey).compareTo(arg1.getAsLong(sortKey));
				if (compareLong == 0) {
					continue;
				}
				return compareLong;
			} catch (Exception e) {
			}
			try {
				int compareString = arg0.getAsString(sortKey).compareTo(arg1.getAsString(sortKey));
				if (compareString == 0) {
					continue;
				}
				return compareString;
			} catch (Exception e) {
			}
		}
		return 0;
	}

}
