package jaicore.basic.chunks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TaskElementValues implements Iterable<String> {

  private String datatype;
  private ETaskElementValueType valueType;
  private String valueDef;

  @Override
  public Iterator<String> iterator() {
    List<String> valueList = new LinkedList<>();

    if (this.isNumeric() && this.valueType == ETaskElementValueType.RANGE) {
      String[] boundSplit = this.valueDef.trim().split(":");

      Number lowerBound = null;
      Number upperBound = null;
      Number stepSize = null;

      switch (this.datatype) {
        case "int":
        case "Integer":
          lowerBound = Integer.parseInt(boundSplit[0]);
          upperBound = Integer.parseInt(boundSplit[1]);
          stepSize = Integer.parseInt(boundSplit[1]);
          break;
        case "double":
        case "Double":
          lowerBound = Double.parseDouble(boundSplit[0]);
          upperBound = Double.parseDouble(boundSplit[1]);
          stepSize = Double.parseDouble(boundSplit[1]);
          break;
      }

      if (lowerBound != null && upperBound != null && stepSize != null) {
      } else {
        throw new IllegalArgumentException("Malformed task element values description: " + this.valueDef);
      }
    }

    return valueList.iterator();
  }

  public boolean isNumeric() {
    switch (this.datatype) {
      case "int":
      case "Integer":
      case "double":
      case "Double":
      case "float":
      case "Float":
      case "byte":
      case "Byte":
      case "short":
      case "Short":
        return true;
      default:
        return false;
    }
  }

}
