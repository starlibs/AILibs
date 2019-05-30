package jaicore.basic.kvstore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jaicore.logging.ToJSONStringUtil;

/**
 * Automatically partitions a KVStoreCollection according to the values of the partitioning key when KVStores or KVStoreCollections are added.
 *
 * @author mwever
 */
public class KVStoreCollectionPartition implements Iterable<Entry<String, KVStoreCollection>> {

	/* Key for partitioning. */
	private final String partitionKey;

	/* data store */
	private Map<String, KVStoreCollection> data;

	/**
		 * Creates an empty two layer {@link KVStoreCollection} partition.
		 *
		 * @param partitionKey The field name for the partitioning key.
		 * @param collection The {@link KVStoreCollection} to initialize this partition.
		 */
	KVStoreCollectionPartition(final String partitionKey, final KVStoreCollection collection) {
		this(partitionKey);
		this.addAll(collection);
	}

	/**
		 * Creates an empty two layer KVStorCollection partition.
		 *
		 * @param partitionKey The field name for the first level partition.
		 */
	KVStoreCollectionPartition(final String firstLevelKey) {
		this.partitionKey = firstLevelKey;
		this.data = new HashMap<>();
	}

	/**
		 * Adds a signle {@link KVStore} to this {@link KVStoreCollectionPartition}.
		 * @param store
		 */
	void add(final KVStore store) {
		/* First ensure that nested maps contain the required keys and KVStoreCollection respectively. */
		String keyForPartition = store.getAsString(this.partitionKey);
		if (!this.data.containsKey(keyForPartition)) {
			this.data.put(keyForPartition, new KVStoreCollection());
		}
		this.data.get(keyForPartition).add(store);
	}

	/**
	 * Adds an entire {@link KVStoreCollection to this {@link KVStoreCollectionPartition}.
	 * @param collection The collection to be added to this partition.
	 */
	void addAll(final KVStoreCollection collection) {
		collection.forEach(this::add);
	}

	/**
	 * @return The set of entries of this partition.
	 */
	Set<Entry<String, KVStoreCollection>> entrySet() {
		return this.data.entrySet();
	}

	@Override
	public Iterator<Entry<String, KVStoreCollection>> iterator() {
		return this.data.entrySet().iterator();
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("partitionKey", this.partitionKey);
		fields.put("data", this.data);
		return ToJSONStringUtil.toJSONString(fields);
	}
}
