package jaicore.basic.kvstore;

import java.util.LinkedList;
import java.util.Map;

public class SimpleKVStoreCollection extends LinkedList<SimpleKVStore> {

  /**
   *
   */
  private static final long serialVersionUID = 8906922960525964818L;

  public SimpleKVStoreCollection select(final Map<String, String> selection) {
    SimpleKVStoreCollection selectedCollection = new SimpleKVStoreCollection();
    for (SimpleKVStore store : this) {
      if (store.matches(selection)) {
        selectedCollection.add(store);
      }
    }
    return selectedCollection;
  }

  public SimpleKVStoreCollection filter(final String[] filterKeys) {
    SimpleKVStoreCollection filteredCollection = new SimpleKVStoreCollection();
    for (SimpleKVStore store : this) {
      store.project(filterKeys);
      filteredCollection.add(store);
    }
    return filteredCollection;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (SimpleKVStore kvStore : this) {
      sb.append(kvStore.getStringRepresentation() + "\n");
    }
    return sb.toString();
  }
}
