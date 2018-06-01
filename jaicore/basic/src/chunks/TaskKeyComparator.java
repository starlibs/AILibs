package jaicore.basic.chunks;

import java.util.Comparator;

public class TaskKeyComparator implements Comparator<Task> {

  private final String[] sortKeys;

  public TaskKeyComparator(final String[] sortKeys) {
    this.sortKeys = sortKeys;
  }

  @Override
  public int compare(final Task arg0, final Task arg1) {
    for (String sortKey : this.sortKeys) {
      try {
        int compareInt = arg0.getValueAsInt(sortKey).compareTo(arg1.getValueAsInt(sortKey));
        if (compareInt == 0) {
          continue;
        }
        return compareInt;
      } catch (Exception e) {
      }
      try {
        int compareLong = arg0.getValueAsLong(sortKey).compareTo(arg1.getValueAsLong(sortKey));
        if (compareLong == 0) {
          continue;
        }
        return compareLong;
      } catch (Exception e) {
      }
      try {
        int compareString = arg0.getValueAsString(sortKey).compareTo(arg1.getValueAsString(sortKey));
        if (compareString == 0) {
          continue;
        }
        return compareString;
      } catch (Exception e) {
      }
    }
    return 0;
  }

}
