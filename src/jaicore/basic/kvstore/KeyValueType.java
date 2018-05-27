package jaicore.basic.kvstore;

public class KeyValueType<V> {

  private Class<V> typeClass;
  private V standardValue;

  public KeyValueType(final Class<V> typeClass, final V standardValue) {
    this.typeClass = typeClass;
    this.standardValue = standardValue;
  }

  public Class<V> getTypeClass() {
    return this.typeClass;
  }

  public V getStandardValue() {
    return this.standardValue;
  }

}
