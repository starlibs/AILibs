package ai.libs.jaicore.basic.kvstore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.api4.java.datastructure.kvstore.IKVStore;

import ai.libs.jaicore.logging.ToJSONStringUtil;

/**
 * Automatically partitions a KVStoreCollection according to the values of the partitioning key when KVStores or KVStoreCollections are added.
 *
 * @author mwever
 */
public class KVStoreCollectionOneLayerPartition implements Iterable<Entry<String, KVStoreCollection>> {

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
	public KVStoreCollectionOneLayerPartition(final String partitionKey, final KVStoreCollection collection) {
		this(partitionKey);
		this.addAll(collection);
	}

	/**
	 * Creates an empty two layer KVStorCollection partition.
	 *
	 * @param partitionKey The field name for the first level partition.
	 */
	public KVStoreCollectionOneLayerPartition(final String firstLevelKey) {
		this.partitionKey = firstLevelKey;
		this.data = new HashMap<>();
	}

	public Map<String, KVStoreCollection> getData() {
		return this.data;
	}

	/**
	 * Adds a signle {@link KVStore} to this {@link KVStoreCollectionOneLayerPartition}.
	 * @param store
	 */
	public void add(final IKVStore store) {
		/* First ensure that nested maps contain the required keys and KVStoreCollection respectively. */
		String keyForPartition = store.getAsString(this.partitionKey);
		if (!this.data.containsKey(keyForPartition)) {
			this.data.put(keyForPartition, new KVStoreCollection());
		}
		this.data.get(keyForPartition).add(store);
	}

	/**
	 * Adds an entire {@link KVStoreCollection to this {@link KVStoreCollectionOneLayerPartition}.
	 * @param collection The collection to be added to this partition.
	 */
	public void addAll(final KVStoreCollection collection) {
		collection.forEach(this::add);
	}

	/**
	 * @return The set of entries of this partition.
	 */
	public Set<Entry<String, KVStoreCollection>> entrySet() {
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
