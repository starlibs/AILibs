package jaicore.basic.kvstore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Automatically partitions a KVStoreCollection in a two-layered fashion according to a first and second level key when KVStores or KVStoreCollections are added.
 *
 * @author mwever
 */
class TwoLayerKVStoreCollectionPartition implements Iterable<Entry<String, Map<String, KVStoreCollection>>> {

	/* Keys for partitioning. */
	private final String firstLayerKey;
	private final String secondLayerKey;

	/* Data store. */
	private Map<String, Map<String, KVStoreCollection>> data;

	/**
	 * Creates an empty two layer {@link KVStoreCollection} partition.
	 *
	 * @param firstLayerKey The field name for the first level partition.
	 * @param secondLayerKey The field name for the second level partition.
	 * @param collection The {@link KVStoreCollection} to initialize this two-layer partition.
	 */
	TwoLayerKVStoreCollectionPartition(final String firstLayerKey, final String secondLayerKey, final KVStoreCollection collection) {
		this(firstLayerKey, secondLayerKey);
		this.addAll(collection);
	}

	/**
	 * Creates an empty two layer KVStorCollection partition.
	 *
	 * @param firstLevelKey The field name for the first level partition.
	 * @param secondLevelKey The field name for the second level partition.
	 */
	TwoLayerKVStoreCollectionPartition(final String firstLevelKey, final String secondLevelKey) {
		this.firstLayerKey = firstLevelKey;
		this.secondLayerKey = secondLevelKey;
		this.data = new HashMap<>();
	}

	/**
	 * Adds a signle {@link KVStore} to this {@link TwoLayerKVStoreCollectionPartition}.
	 * @param store
	 */
	void add(final KVStore store) {
		/* First ensure that nested maps contain the required keys and KVStoreCollection respectively. */
		String firstLevelValue = store.getAsString(this.firstLayerKey);
		String secondLevelValue = store.getAsString(this.secondLayerKey);
		if (!this.data.containsKey(firstLevelValue)) {
			Map<String, KVStoreCollection> secondLevelMap = new HashMap<>();
			secondLevelMap.put(secondLevelValue, new KVStoreCollection());
			this.data.put(firstLevelValue, secondLevelMap);
		} else if (!this.data.get(firstLevelValue).containsKey(secondLevelValue)) {
			this.data.get(firstLevelValue).put(secondLevelValue, new KVStoreCollection());
		}
		this.data.get(firstLevelValue).get(secondLevelValue).add(store);
	}

	/**
	 * Adds an entire {@link KVStoreCollection to this {@link TwoLayerKVStoreCollectionPartition}.
	 * @param collection The collection to be added to this partition.
	 */
	void addAll(final KVStoreCollection collection) {
		collection.forEach(this::add);
	}

	/**
	 * @return The set of entries of this partition.
	 */
	Set<Entry<String, Map<String, KVStoreCollection>>> entrySet() {
		return this.data.entrySet();
	}

	@Override
	public Iterator<Entry<String, Map<String, KVStoreCollection>>> iterator() {
		return this.data.entrySet().iterator();
	}

	/**
	 * @return The key name that is used to do the partition for the first layer.
	 */
	public String getFirstLayerKey() {
		return this.firstLayerKey;
	}

	/**
	 * @return The key name that is used to do the partition for the second layer.
	 */
	public String getSecondLayerKey() {
		return this.secondLayerKey;
	}
}
