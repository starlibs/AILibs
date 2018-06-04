package jaicore.basic.kvstore;

public class KeyValueStore {

  private int kvStoreID;
  private String name;
  private KeyValueSchema schema;

  public KeyValueStore(final int kvStoreID, final String name) {
    this.kvStoreID = kvStoreID;
    this.name = name;
  }

  public void setSchema(final KeyValueSchema schema) {
    this.schema = schema;
  }

  public KeyValueSchema getSchema() {
    return this.schema;
  }

  public int getKVStoreID() {
    return this.kvStoreID;
  }

  public String getName() {
    return this.name;
  }

}
