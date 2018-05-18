package jaicore.basic.kvstore;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Table<V> {

  private final List<String> columnIndex = new LinkedList<>();
  private final List<String> rowIndex = new LinkedList<>();

  private Map<String, Map<String, V>> tableData = new HashMap<>();

  public Table() {

  }

  public void addEntry(final String columnIndexValue, final String rowIndexValue, final V entry) {
    Map<String, V> selectedRow = this.tableData.get(rowIndexValue);
    if (selectedRow == null) {
      selectedRow = new HashMap<>();
      this.tableData.put(rowIndexValue, selectedRow);
      if (!this.rowIndex.contains(rowIndexValue)) {
        this.rowIndex.add(rowIndexValue);
      }
    }
    if (!this.columnIndex.contains(columnIndexValue)) {
      this.columnIndex.add(columnIndexValue);
    }
    selectedRow.put(columnIndexValue, entry);
  }

  public String toLaTeX() {
    StringBuilder sb = new StringBuilder();

    sb.append("\\begin{tabular}{");
    for (int i = 0; i < this.columnIndex.size() + 1; i++) {
      sb.append("l");
    }
    sb.append("}");

    Collections.sort(this.columnIndex);
    for (String c : this.columnIndex) {
      sb.append("&");
      // sb.append("\\rotatebox{90}{");
      sb.append(c);
      // sb.append("}");
    }
    sb.append("\\\\\n");

    for (String r : this.rowIndex) {
      sb.append(r);

      for (String c : this.columnIndex) {
        sb.append(" & ");
        Map<String, V> selectRow = this.tableData.get(r);
        if (selectRow != null) {
          V entry = selectRow.get(c);
          if (entry != null) {
            sb.append(entry.toString().replaceAll("_", "\\_"));
          }
        }
      }
      sb.append("\\\\\n");
    }

    sb.append("\\end{tabular}");
    return sb.toString();
  }

  public String toLaTeX(final String missingEntry) {
    StringBuilder sb = new StringBuilder();

    sb.append("\\begin{tabular}{");
    for (int i = 0; i < this.columnIndex.size() + 1; i++) {
      sb.append("l");
    }
    sb.append("}");

    // Collections.sort(this.columnIndex);
    for (String c : this.columnIndex) {
      sb.append("&");
      // sb.append("\\rotatebox{90}{");
      sb.append(c);
      // sb.append("}");
    }
    sb.append("\\\\\n");

    // Collections.sort(this.rowIndex);
    for (String r : this.rowIndex) {
      sb.append(r);

      for (String c : this.columnIndex) {
        sb.append(" & ");
        Map<String, V> selectRow = this.tableData.get(r);
        if (selectRow != null) {
          V entry = selectRow.get(c);
          if (entry != null) {
            sb.append(entry.toString().replaceAll("_", "\\_"));
          } else {
            sb.append(missingEntry);
          }
        }
      }
      sb.append("\\\\\n");
    }

    sb.append("\\end{tabular}");
    return sb.toString();
  }

  public String toCSV(final String standardValue) {
    return this.toCSV("\t", standardValue);
  }

  public String toCSV(final String separator, final String standardValue) {
    StringBuilder sb = new StringBuilder();

    // Collections.sort(this.columnIndex);
    Collections.sort(this.rowIndex);

    boolean first = true;
    for (String c : this.columnIndex) {
      if (first) {
        first = false;
      } else {
        sb.append(separator);
      }
      sb.append(c);
    }
    sb.append("\n");
    for (String r : this.rowIndex) {
      // sb.append(r);

      first = true;

      for (String c : this.columnIndex) {
        if (first) {
          first = false;
        } else {
          sb.append(separator);
        }

        Map<String, V> selectRow = this.tableData.get(r);
        if (selectRow != null) {
          V entry = selectRow.get(c);
          if (entry != null) {
            sb.append(entry.toString());
          } else {
            sb.append(standardValue);
          }
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public String getColumnsOfCSV() {
    StringBuilder sb = new StringBuilder();
    sb.append("Columns");
    for (String c : this.columnIndex) {
      sb.append("\n");
      sb.append(c);
      // sb.append("}");
    }
    return sb.toString();
  }

  public String getRowsOfCSV() {
    StringBuilder sb = new StringBuilder();
    sb.append("Rows");
    for (String c : this.rowIndex) {
      sb.append("\n");
      sb.append(c);
    }
    return sb.toString();
  }

}
