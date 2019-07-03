package ai.libs.jaicore.basic.kvstore;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Generic interface of KVStores allowing convenient access to the data contained.
 *
 * @author mwever
 *
 */
public interface IKVStore extends Map<String, Object> {

	/**
	 * Returns a value as string for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public String getAsString(final String key);

	/**
	 * Returns a value as boolean for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public Boolean getAsBoolean(final String key);

	/**
	 * Returns a value as integer for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public Integer getAsInt(final String key);

	/**
	 * Returns a value as double for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public Double getAsDouble(final String key);

	/**
	 * Returns a value as long for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public Long getAsLong(final String key);

	/**
	 * Returns a value as short for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public Short getAsShort(final String key);

	/**
	 * Returns a value as byte for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public Byte getAsByte(final String key);

	/**
	 * Returns a value as object for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 * @param objectClass
	 * 	   		  The class object for which a concrete object is to be returned.
	 */
	public Object getAsObject(final String key, final Class<?> objectClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException;

	/**
	 * Returns a value as byte array for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public byte[] getAsBytes(String columnClassifierObject);

	/**
	 * Returns a value as double list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public List<Double> getAsDoubleList(final String key);

	/**
	 * Returns a value as double list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 * @param separator
	 * 			  Separator dividing the entries of the list.
	 */
	public List<Double> getAsDoubleList(final String key, final String separator);

	/**
	 * Returns a value as integer list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public List<Integer> getAsIntList(final String key);

	/**
	 * Returns a value as integer list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 * @param separator
	 * 			  Separator dividing the entries of the list.
	 */
	public List<Integer> getAsIntList(final String key, final String separator);

	/**
	 * Returns a value as string list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public List<String> getAsStringList(final String key);

	/**
	 * Returns a value as string list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 * @param separator
	 * 			  Separator dividing the entries of the list.
	 */
	public List<String> getAsStringList(final String key, final String separator);

	/**
	 * Returns a value as boolean list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public List<Boolean> getAsBooleanList(final String key);

	/**
	 * Returns a value as boolean list for the given key.
	 *
	 * @param key
	 *            Key for which the value shall be returned.
	 */
	public List<Boolean> getAsBooleanList(final String key, final String separator);

	/**
	 * Returns a value as a File object.
	 * @param key Key for which the value shall be returned.
	 * @return The value for the given key as a file.
	 */
	public File getAsFile(final String key);

	/**
	 * Checks whether particular keys have matching values according to {@code selection}.
	 *
	 * @param selection
	 *            Map of keys and values to match for. Values may use * as a wildcard.
	 * @return Returns true whether this key value star meets the keys and values in {@code selection}.
	 */
	public boolean matches(final Map<String, String> selection);

	/**
	 * Merges a set of keys and concatenates the values of the respective keys.
	 *
	 * @param fieldKeys
	 *            Keys of values to be concatenated.
	 * @param separator
	 *            Seperator to separate values of different keys.
	 * @param newKey
	 *            Name of the new key.
	 */
	public void merge(final String[] fieldKeys, final String separator, final String newKey);

	/**
	 * Reduces the key value store such that it only contains the given keys.
	 *
	 * @param filterKeys
	 *            Keys to keep in the key value store.
	 */
	public void project(final String[] filterKeys);

	/**
	 * Allows to apply a {@code filter} to the value of a specific {@code key}.
	 *
	 * @param key
	 *            Key to whose value the filter may be applied.
	 * @param filter
	 *            The filter for applying to the value of the given {@code key}.
	 */
	public void filter(final String key, final IKVFilter filter);

	/**
	 * Enables filtering values of multiple keys.
	 *
	 * @param filterMap
	 *            Map of keys and respective filters to apply.
	 */
	public void filter(final Map<String, IKVFilter> filterMap);

	/**
	 * Renames a key by the new key name.
	 *
	 * @param key
	 *            The old key name.
	 * @param newKeyName
	 *            The new key name.
	 */
	public void renameKey(final String key, final String newKeyName);

	/**
	 * Updates all keys of this key value store and prepends the given prefix.
	 *
	 * @param prefix
	 *            The prefix to prepend to key names.
	 */
	public void prefixAllKeys(final String prefix);

	/**
	 * Removes all the keys provided by {@code removeKeys}.
	 *
	 * @param removeKeys
	 *            Array of keys that shall be removed from the key value store.
	 */
	public void removeAll(final String[] removeKeys);

	/**
	 * Checks whether the value for the requested key is null.
	 * @param key The key for which the value is to be checked.
	 * @return True iff the value is null.
	 */
	public boolean isNull(String key);

}
