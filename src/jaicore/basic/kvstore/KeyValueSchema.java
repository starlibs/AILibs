package jaicore.basic.kvstore;

import java.util.ArrayList;

public class KeyValueSchema extends ArrayList<KeyValueType<?>> {

  /**
   *
   */
  private static final long serialVersionUID = -8590021143615500460L;

  public KeyValueSchema() {

  }

  public KeyValueSchema(final KeyValueSchema schemaToCopy) {
    for (KeyValueType<?> type : schemaToCopy) {
      this.add(new KeyValueType(type.getTypeClass(), type.getStandardValue()));
    }
  }

  public KeyValueSchema addType(final KeyValueType<?> type) {
    this.add(type);
    return this;
  }

}
