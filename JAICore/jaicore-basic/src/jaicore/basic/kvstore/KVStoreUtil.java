package jaicore.basic.kvstore;

import jaicore.basic.ValueUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class KVStoreUtil {

  public static String kvStoreCollectionToLaTeXTable(final Collection<SimpleKVStore> kvStoreCollection, final String rowIndex, final String columnIndex) {
    StringBuilder sb = new StringBuilder();
    Table<String> table = new Table<>();
    for (SimpleKVStore store : kvStoreCollection) {
      String rowValue = store.getValueAsString(rowIndex).replaceAll("\\_", "\\\\_");
      String columnValue = store.getValueAsString(columnIndex).replaceAll("\\_", "\\\\_");
      String tableEntry = ValueUtil.valueToString(store.getValueAsDouble("meanAccuracy"), 2) + "+-" + ValueUtil.valueToString(store.getValueAsDouble("stdAccuracy"), 2);
      table.addEntry(columnValue, rowValue, tableEntry);
    }
    sb.append(table.toLaTeX());
    return sb.toString();
  }

  public static String kvStoreCollectionToLaTeXTable(final Collection<SimpleKVStore> kvStoreCollection, final String rowIndex, final String columnIndex,
      final String cellFormatting) {

    StringBuilder sb = new StringBuilder();
    Table<String> table = new Table<>();
    for (SimpleKVStore store : kvStoreCollection) {

      String[] cellFormattingSplit = store.getValueAsString(cellFormatting).split("#");
      List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

      String rowValue = store.getValueAsString(rowIndex).replaceAll("\\_", "\\\\_");
      String columnValue = store.getValueAsString(columnIndex).replaceAll("\\_", "\\\\_");

      StringBuilder tableEntryBuilder = new StringBuilder();
      for (String cellKey : cleanedCellFormatting) {
        if (!store.containsKey(cellKey)) {
          tableEntryBuilder.append(cellKey);
        } else {
          tableEntryBuilder.append(store.getValueAsString(cellKey));
        }
      }

      table.addEntry(columnValue, rowValue, tableEntryBuilder.toString());
    }
    sb.append(table.toLaTeX());
    return sb.toString();
  }

  public static String kvStoreCollectionToLaTeXTable(final Collection<SimpleKVStore> kvStoreCollection, final String rowIndex, final String columnIndex,
      final String cellFormatting, final String missingEntry) {

    StringBuilder sb = new StringBuilder();
    Table<String> table = new Table<>();
    for (SimpleKVStore store : kvStoreCollection) {

      String[] cellFormattingSplit = store.getValueAsString(cellFormatting).split("#");
      List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

      String rowValue = store.getValueAsString(rowIndex).replaceAll("\\_", "\\\\_");
      String columnValue = store.getValueAsString(columnIndex).replaceAll("\\_", "\\\\_");

      StringBuilder tableEntryBuilder = new StringBuilder();
      for (String cellKey : cleanedCellFormatting) {
        if (!store.containsKey(cellKey)) {
          tableEntryBuilder.append(cellKey);
        } else {
          tableEntryBuilder.append(store.getValueAsString(cellKey));
        }
      }

      table.addEntry(columnValue, rowValue, tableEntryBuilder.toString());
    }
    sb.append(table.toLaTeX(missingEntry));
    return sb.toString();
  }

  public static String kvStoreCollectionToCSVTable(final Collection<SimpleKVStore> kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting,
      final String standardValue) {

    StringBuilder sb = new StringBuilder();
    Table<String> table = new Table<>();
    for (SimpleKVStore store : kvStoreCollection) {

      String[] cellFormattingSplit = store.getValueAsString(cellFormatting).split("#");
      List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

      String rowValue = store.getValueAsString(rowIndex).replaceAll("\\_", "\\\\_");
      String columnValue = store.getValueAsString(columnIndex).replaceAll("\\_", "\\\\_");

      StringBuilder tableEntryBuilder = new StringBuilder();
      for (String cellKey : cleanedCellFormatting) {
        if (!store.containsKey(cellKey)) {
          tableEntryBuilder.append(cellKey);
        } else {
          tableEntryBuilder.append(store.getValueAsString(cellKey));
        }
      }

      table.addEntry(columnValue, rowValue, tableEntryBuilder.toString());
    }
    sb.append(table.toCSV(standardValue));
    return sb.toString();
  }

  public static Table<String> kvStoreCollectionToTable(final Collection<SimpleKVStore> kvStoreCollection, final String rowIndex, final String columnIndex,
      final String cellFormatting, final String standardValue) {
    Table<String> table = new Table<>();
    for (SimpleKVStore store : kvStoreCollection) {

      String[] cellFormattingSplit = store.getValueAsString(cellFormatting).split("#");
      List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

      String rowValue = store.getValueAsString(rowIndex).replaceAll("\\_", "\\\\_");
      String columnValue = store.getValueAsString(columnIndex).replaceAll("\\_", "\\\\_");

      StringBuilder tableEntryBuilder = new StringBuilder();
      for (String cellKey : cleanedCellFormatting) {
        if (!store.containsKey(cellKey)) {
          tableEntryBuilder.append(cellKey);
        } else {
          tableEntryBuilder.append(store.getValueAsString(cellKey));
        }
      }

      table.addEntry(columnValue, rowValue, tableEntryBuilder.toString());
    }
    return table;
  }

}
