package jaicore.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ProcessList extends ArrayList<ProcessInfo> {

	private final long timestamp = System.currentTimeMillis();
	private final List<Integer> fieldSeparationIndices = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		new ProcessList().stream().forEach(p -> System.out.println(p));
	}

	public ProcessList() throws IOException {
		String line;
		Process p = ProcessUtil.getProcessListProcess();
		try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			boolean tableInitialized = false;
			int lineNumber = 0;
			while ((line = input.readLine()) != null) {
				switch (ProcessUtil.getOS()) {
				case WIN: {
					if (!tableInitialized) {
						if (line.startsWith("===")) {
							int offset = 0;
							String[] headerLineParts = line.split(" ");
							for (int i = 0; i < headerLineParts.length; i++) {
								offset += headerLineParts[i].length();
								fieldSeparationIndices.add(offset);
								offset++;
							}
							tableInitialized = true;
						}
					} else {
						List<String> entries = new ArrayList<>();
						int indexFrom = 0;
						int indexTo = 0;
						for (int i = 0; i < fieldSeparationIndices.size(); i++) {
							indexTo = fieldSeparationIndices.get(i);
							entries.add(line.substring(indexFrom, indexTo).trim());
							indexFrom = indexTo + 1;
						}
						entries.add(line.substring(indexTo).trim());
						this.add(new ProcessInfo(Integer.parseInt(entries.get(1)), entries.get(0), entries.get(4)));
					}
					break;
				}
				case LINUX: {
					if (lineNumber > 0) {
						String remainingLine = line;
						List<String> entries = new ArrayList<>();
						for (int i = 0; i < 6; i++) {
							int indexOfNextSpace = remainingLine.indexOf(" ");
							if (indexOfNextSpace >= 0) {
								entries.add(remainingLine.substring(0, indexOfNextSpace));
								remainingLine = remainingLine.substring(indexOfNextSpace).trim();
							}
							else
								entries.add(remainingLine);
						}
						this.add(new ProcessInfo(Integer.parseInt(entries.get(1)), entries.get(5), entries.get(4)));
					}
					break;
				}
				default:
					throw new UnsupportedOperationException("Cannot create process list for OS " + ProcessUtil.getOS());
				}
				lineNumber++;
			}
		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

	public long getTimestamp() {
		return timestamp;
	}
}
