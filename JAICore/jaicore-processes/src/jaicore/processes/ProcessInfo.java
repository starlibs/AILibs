package jaicore.processes;

public class ProcessInfo {
	private final int pid;
	private final String descr;
	private final String memory;

	public ProcessInfo(int pid, String descr, String memory) {
		super();
		this.pid = pid;
		this.descr = descr;
		this.memory = memory;
	}

	public int getPid() {
		return pid;
	}

	public String getDescr() {
		return descr;
	}

	public String getMemory() {
		return memory;
	}

	@Override
	public String toString() {
		return "ProcessInfo [pid=" + pid + ", descr=" + descr + ", memory=" + memory + "]";
	}
}
