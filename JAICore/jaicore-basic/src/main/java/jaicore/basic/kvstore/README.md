# Key Value Stores

# KVStore

A KVStore is a HashMap extension for providing easier access to different object values of the map.
Additionally, KVStore provides some methods to manipulate the contained data such as applying a filter,
merging values of a set of keys into a single key-value pair, etc.
Furthermore it may be attached to a KVStoreCollection via setCollection(•)

## Functional Methods
| Method | Description |
| --- | --- |
| getCollection() | Returns the KVStoreCollection this KVStore is assigned to |
| setCollection(KVStoreCollection collection) | Attaches this KVStore to a KVStoreCollection |
| getStringRepresentation(String[] projectionFilter) | Returns a String based serialization format including only the key-value pairs for the keys provided in the array |
| matches(Map<String, String>) | Returns whether the data stored in this KVStore matches the description in the provided map. |
| readKVStoreFromDescription(String description) | This reads a KVStore structure from a String description and adds the respective data to this KVStore. |
| serializeTo(File file) | Serializes the KVStore to the provided file. Data contained within the file is overwritten. |
| serializeTo(String fileName) | Alias for the serializeTo method above. |
| toString() | Returns a string representation of this KVStore containing all available keys. |

## Manipulation Methods
| Method | Description |
| --- | --- |
| filter(Map<String, IKVFilter>), filter(String key, IKVFilter filter) | For each key in the map or for the given key it applies the respective implementation of the IKVFilter interface. The implementation may observe or even manipulate the values of the keys. |
| merge(String[] keysToMerge, String separator, String newKey) | This method allows to merge the values of the array of keys, separates them by the given separator (can also be empty), and stores the result for the given newKey |
| prefixAllKeys(String) | This method allows to rename each key attaching a prefix to each one of these. |
| project(String[] filterKeys) | This method keeps only key-value pairs provided in the array, other pairs are removed from the KVStore |
| removeAll(String[] keysToRemove) | Removes all the key-value pairs corresponding to the provided array of keys |

## Access methods
* getAsBoolean
* getAsBooleanList
* getAsByte
* getAsDouble
* getAsDoubleList
* getAsInt
* getAsIntList
* getAsLong
* getAsObject
* getAsShort
* getAsString
* getAsStringList

# KVStoreCollection
KVStoreCollection is a LinkedList extension additionally allowing for reorganizing the contained KVStores.
The KVStoreCollection is able to store meta data (represented by a KVStore again).


## Functional Methods

## Manipulation Methods 

# KVStoreUtil

# KVStoreStatisticsUtil
