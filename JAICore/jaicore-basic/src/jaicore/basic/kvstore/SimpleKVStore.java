package jaicore.basic.kvstore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;

public class SimpleKVStore implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 6635572555061279948L;

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKVStore.class);

  private Map<String, String> keyValueMap = new HashMap<>();

  public SimpleKVStore() {

  }

  public SimpleKVStore(final String stringRepresentation) {
    this.readKVStoreFromDescription(stringRepresentation);
  }

  public SimpleKVStore(final Map<String, String> keyValueMap) {
    this.keyValueMap.putAll(keyValueMap);
  }

  public SimpleKVStore(final SimpleKVStore resultsChunk) {
    this.keyValueMap.putAll(resultsChunk.keyValueMap);
  }

  public void store(final String key, final String value) {
    this.keyValueMap.put(key, value);
  }

  public void store(final String key, final int value) {
    this.keyValueMap.put(key, String.valueOf(value));
  }

  public void store(final String key, final double value) {
    this.keyValueMap.put(key, String.valueOf(value));
  }

  public void store(final String key, final long value) {
    this.keyValueMap.put(key, String.valueOf(value));
  }

  public void store(final String key, final byte value) {
    this.keyValueMap.put(key, String.valueOf(value));
  }

  public void store(final String key, final short value) {
    this.keyValueMap.put(key, String.valueOf(value));
  }

  public boolean containsKey(final String key) {
    return this.keyValueMap.containsKey(key);
  }

  public String getValueAsString(final String key) {
    return this.keyValueMap.get(key);
  }

  public Boolean getValueAsBoolean(final String key) {
    return Boolean.valueOf(this.keyValueMap.get(key));
  }

  public Integer getValueAsInt(final String key) {
    return Integer.valueOf(this.keyValueMap.get(key));
  }

  public Double getValueAsDouble(final String key) {
    return Double.valueOf(this.keyValueMap.get(key));
  }

  public Long getValueAsLong(final String key) {
    return Long.valueOf(this.keyValueMap.get(key));
  }

  public Short getValueAsShort(final String key) {
    return Short.valueOf(this.keyValueMap.get(key));
  }

  public Byte getValueAsByte(final String key) {
    return Byte.valueOf(this.keyValueMap.get(key));
  }

  public List<Double> getValueAsDoubleList(final String key, final String separator) {
    return Stream.of(this.keyValueMap.get(key).split(separator)).map(x -> Double.valueOf(x)).collect(Collectors.toList());
  }

  public List<Integer> getValueAsIntList(final String key, final String separator) {
    return Stream.of(this.keyValueMap.get(key).split(separator)).map(x -> Integer.valueOf(x)).collect(Collectors.toList());
  }

  public List<String> getValueAsStringList(final String key, final String separator) {
    return Stream.of(this.keyValueMap.get(key).split(separator)).map(x -> x.trim()).collect(Collectors.toList());
  }

  public List<Boolean> getValueAsBooleanList(final String key, final String separator) {
    return Stream.of(this.keyValueMap.get(key).split(separator)).map(x -> Boolean.valueOf(x)).collect(Collectors.toList());
  }

  public Object getAsObject(final String key, final Class<?> objectClass)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    return objectClass.getConstructor().newInstance(this.keyValueMap.get(key));
  }

  public Set<Entry<String, String>> getAllKVEntries() {
    return this.keyValueMap.entrySet();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.keyValueMap == null) ? 0 : this.keyValueMap.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    SimpleKVStore other = (SimpleKVStore) obj;
    if (this.keyValueMap == null) {
      if (other.keyValueMap != null) {
        return false;
      }
    } else if (!this.keyValueMap.equals(other.keyValueMap)) {
      return false;
    }
    return true;
  }

  public Map<String, String> getKeyValueMap() {
    return this.keyValueMap;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.keyValueMap);
    return sb.toString();
  }

  public String getStringRepresentation() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Entry<String, String> entry : this.keyValueMap.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(";");
      }
      sb.append(entry.getKey() + "=" + entry.getValue());
    }
    return sb.toString();
  }

  public void readKVStoreFromDescription(final String kvDescription) {
    String[] pairSplit = kvDescription.trim().split(";");
    for (String kvPair : pairSplit) {
      String[] kvSplit = kvPair.trim().split("=");
      try {
        if (kvSplit.length == 2) {
          this.keyValueMap.put(kvSplit[0], kvSplit[1]);
        } else {
          this.keyValueMap.put(kvSplit[0], "");
        }
      } catch (Exception e) {
        LOGGER.error("Could not read kv store from string description", e);
        System.exit(0);
      }
    }
  }

  public String getStringRepresentation(final String[] formattingFilter) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String filter : formattingFilter) {
      if (first) {
        first = false;
      } else {
        sb.append(";");
      }
      sb.append(filter + "=" + this.keyValueMap.get(filter));
    }
    return sb.toString();
  }

  public boolean matches(final Map<String, String> selection) {
    for (String key : selection.keySet()) {
      String actual = this.keyValueMap.get(key);
      String desired = selection.get(key);

      if (actual == null && desired != null) {
        return false;
      } else if (actual != null && desired == null) {
        return false;
      } else {
        if (desired != null && desired.contains("*")) {
          String[] exprSplit = desired.split("\\*");

          if (!actual.startsWith(exprSplit[0])) {
            return false;
          }
          if (!actual.endsWith(exprSplit[exprSplit.length - 1])) {
            return false;
          }
          for (int i = 1; i < exprSplit.length - 1; i++) {
            if (!actual.contains(exprSplit[i])) {
              return false;
            }
          }
        } else if (actual == null || !actual.equals(desired)) {
          return false;
        }
      }
    }
    return true;

  }

  public void project(final String[] filterKeys) {
    Set<String> keysToKeep = Arrays.stream(filterKeys).collect(Collectors.toSet());
    Collection<String> keysToRemove = SetUtil.difference(this.keyValueMap.keySet(), keysToKeep);
    this.projectRemove(keysToRemove.toArray(new String[] {}));

    SimpleKVStore filteredStore = new SimpleKVStore();
    for (String key : filterKeys) {
      if (this.keyValueMap.containsKey(key)) {
        filteredStore.keyValueMap.put(key, this.keyValueMap.get(key));
      }
    }
  }

  public void projectRemove(final String[] removeKeys) {
    Set<String> keysToRemove = Arrays.stream(removeKeys).collect(Collectors.toSet());
    for (String key : keysToRemove) {
      this.keyValueMap.remove(key);
    }
  }

  public void applyFilter(final Map<String, IKVFilter> filterMap) {
    for (String filterKey : filterMap.keySet()) {
      if (filterMap.containsKey(filterKey) && this.getKeyValueMap().containsKey(filterKey)) {
        this.getKeyValueMap().put(filterKey, filterMap.get(filterKey).filter(this.getKeyValueMap().get(filterKey)));
      }
    }
  }

  public void writeToFile(final String string) throws IOException {
    File file = new File(string);
    if (file.getParent() != null) {
      file.getParentFile().mkdirs();
    }
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
      bw.write(this.getStringRepresentation());
    }
  }

}
