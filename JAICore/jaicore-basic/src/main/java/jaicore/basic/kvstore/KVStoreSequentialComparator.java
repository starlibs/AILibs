package jaicore.basic.kvstore;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This comparator may be used to sort KVStore objects in a KVStoreCollection according to the values for the specified keys.
 * The KVStores are then compared successively for the provided keys and ordered in ascending order.
 *
 * @author mwever
 */
public class KVStoreSequentialComparator implements Comparator<KVStore> {

	private static final Logger LOGGER = LoggerFactory.getLogger(KVStoreSequentialComparator.class);

	private final String[] sortKeys;

	/**
	 * Default c'tor initializing the comparator for a set of keys for which the kvstores are to be sorted.
	 *
	 * @param sortKeys The array of keys for which to sort the KVStoreCollection.
	 */
	public KVStoreSequentialComparator(final String... sortKeys) {
		this.sortKeys = sortKeys;
	}

	@Override
	public int compare(final KVStore arg0, final KVStore arg1) {
		for (String sortKey : this.sortKeys) {
			Integer compare = null;

			try {
				compare = arg0.getAsInt(sortKey).compareTo(arg1.getAsInt(sortKey));
			} catch (Exception e) {
				try {
					compare = arg0.getAsLong(sortKey).compareTo(arg1.getAsLong(sortKey));
				} catch (Exception e1) {
					try {
						compare = arg0.getAsString(sortKey).compareTo(arg1.getAsString(sortKey));
					} catch (Exception e2) {
						LOGGER.warn("The values of the key {} are neither int nor long nor string. This type of value is thus not supported for sorting and the key is skipped.", sortKey);
					}
				}
			}

			if (compare == null || compare == 0) {
				continue;
			}
			return compare;
		}
		return 0;
	}

}
