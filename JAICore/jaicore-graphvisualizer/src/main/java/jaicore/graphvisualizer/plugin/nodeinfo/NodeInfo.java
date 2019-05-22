package jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.ArrayList;
import java.util.List;

public class NodeInfo {

	private String mainNodeId;
	private List<String> parentNodeIds;
	private List<String> childrenNodeIds;
	private String nodeType;

	@SuppressWarnings("unused")
	private NodeInfo() {
		// for serialization purposes
	}

	public NodeInfo(String mainNodeId, List<String> parentNodeIds, List<String> childrenNodeIds, String nodeType) {
		super();
		this.mainNodeId = mainNodeId;
		if (parentNodeIds != null) {
			this.parentNodeIds = new ArrayList<>(parentNodeIds);
		}
		if (childrenNodeIds != null) {
			this.childrenNodeIds = new ArrayList<>(childrenNodeIds);
		}
		this.nodeType = nodeType;
	}

	public String getMainNodeId() {
		return mainNodeId;
	}

	public List<String> getParentNodeIds() {
		return parentNodeIds;
	}

	public List<String> getChildrenNodeIds() {
		return childrenNodeIds;
	}

	public String getNodeType() {
		return nodeType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childrenNodeIds == null) ? 0 : childrenNodeIds.hashCode());
		result = prime * result + ((mainNodeId == null) ? 0 : mainNodeId.hashCode());
		result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
		result = prime * result + ((parentNodeIds == null) ? 0 : parentNodeIds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NodeInfo other = (NodeInfo) obj;
		if (childrenNodeIds == null) {
			if (other.childrenNodeIds != null) {
				return false;
			}
		} else if (!childrenNodeIds.equals(other.childrenNodeIds)) {
			return false;
		}
		if (mainNodeId == null) {
			if (other.mainNodeId != null) {
				return false;
			}
		} else if (!mainNodeId.equals(other.mainNodeId)) {
			return false;
		}
		if (nodeType == null) {
			if (other.nodeType != null) {
				return false;
			}
		} else if (!nodeType.equals(other.nodeType)) {
			return false;
		}
		if (parentNodeIds == null) {
			if (other.parentNodeIds != null) {
				return false;
			}
		} else if (!parentNodeIds.equals(other.parentNodeIds)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "NodeInfo [mainNodeId=" + mainNodeId + ", parentNodeIds=" + parentNodeIds + ", childrenNodeIds=" + childrenNodeIds + ", nodeType=" + nodeType + "]";
	}

}
