package jaicore.basic.chunks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ChunkCreator {

  private final Map<String, Set<String>> keyValueRangeMap;

  public ChunkCreator() {
    this.keyValueRangeMap = new HashMap<>();
  }

  public void addTaskElementRange(final String name, final String[] values) {
    this.keyValueRangeMap.put(name, Arrays.stream(values).collect(Collectors.toSet()));
  }

  public TaskChunk<Task> createChunk(final Random rand, final int numberOfSeeds, final String seedFieldName) {
    Set<Long> seeds = new HashSet<>();
    while (seeds.size() < numberOfSeeds) {
      seeds.add(rand.nextLong());
    }
    return this.createChunk(seedFieldName, seeds);
  }

  public TaskChunk<Task> createChunk(final String seedFieldName, final Set<Long> seeds) {
    TaskChunk<Task> chunk = new TaskChunk<>("chunkID=1");
    Task t = new Task();
    List<Task> taskList = new LinkedList<>(t.storeEach(seedFieldName, seeds.stream().map(x -> x.toString()).collect(Collectors.toSet())));
    for (Entry<String, Set<String>> kvEntry : this.keyValueRangeMap.entrySet()) {
      List<Task> temporaryList = new LinkedList<>();
      for (Task task : taskList) {
        temporaryList.addAll(task.storeEach(kvEntry.getKey(), kvEntry.getValue()));
      }
      taskList.clear();
      taskList.addAll(temporaryList);
    }

    int taskID = 1;
    for (Task task : taskList) {
      task.setTaskID((taskID++) + "");
      chunk.add(task);
    }

    return chunk;
  }

}
