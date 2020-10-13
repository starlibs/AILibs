package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodeInfo {

	private String mainNodeId;
	private List<String> parentNodeIds;
	private List<String> childrenNodeIds;
	private String nodeType;
	private Map<String, Object> properties;

	@SuppressWarnings("unused")
	private NodeInfo() {
		// for serialization purposes
	}

	public NodeInfo(final String mainNodeId, final List<String> parentNodeIds, final List<String> childrenNodeIds, final String nodeType) {
		this(mainNodeId, parentNodeIds, childrenNodeIds, nodeType, null);
	}

	public NodeInfo(final String mainNodeId, final List<String> parentNodeIds, final List<String> childrenNodeIds, final String nodeType, final Map<String, Object> properties) {
		super();
		this.mainNodeId = mainNodeId;
		if (parentNodeIds != null) {
			this.parentNodeIds = new ArrayList<>(parentNodeIds);
		}
		if (childrenNodeIds != null) {
			this.childrenNodeIds = new ArrayList<>(childrenNodeIds);
		}
		this.nodeType = nodeType;
		this.properties = properties;
	}

	public String getMainNodeId() {
		return this.mainNodeId;
	}

	public List<String> getParentNodeIds() {
		return this.parentNodeIds;
	}

	public List<String> getChildrenNodeIds() {
		return this.childrenNodeIds;
	}

	public String getNodeType() {
		return this.nodeType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.childrenNodeIds == null) ? 0 : this.childrenNodeIds.hashCode());
		result = prime * result + ((this.mainNodeId == null) ? 0 : this.mainNodeId.hashCode());
		result = prime * result + ((this.nodeType == null) ? 0 : this.nodeType.hashCode());
		result = prime * result + ((this.parentNodeIds == null) ? 0 : this.parentNodeIds.hashCode());
		result = prime * result + ((this.properties == null) ? 0 : this.properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		NodeInfo other = (NodeInfo) obj;
		if (this.childrenNodeIds == null) {
			if (other.childrenNodeIds != null) {
				return false;
			}
		} else if (!this.childrenNodeIds.equals(other.childrenNodeIds)) {
			return false;
		}
		if (this.mainNodeId == null) {
			if (other.mainNodeId != null) {
				return false;
			}
		} else if (!this.mainNodeId.equals(other.mainNodeId)) {
			return false;
		}
		if (this.nodeType == null) {
			if (other.nodeType != null) {
				return false;
			}
		} else if (!this.nodeType.equals(other.nodeType)) {
			return false;
		}
		if (this.parentNodeIds == null) {
			if (other.parentNodeIds != null) {
				return false;
			}
		} else if (!this.parentNodeIds.equals(other.parentNodeIds)) {
			return false;
		}
		if (this.properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else if (!this.properties.equals(other.properties)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "NodeInfo [mainNodeId=" + this.mainNodeId + ", parentNodeIds=" + this.parentNodeIds + ", childrenNodeIds=" + this.childrenNodeIds + ", nodeType=" + this.nodeType + ", properties=" + this.properties + "]";
	}

	public Map<String, Object> getProperties() {
		return this.properties;
	}

	public void setProperties(final Map<String, Object> properties) {
		this.properties = properties;
	}
}
