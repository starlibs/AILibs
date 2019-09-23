package ai.libs.jaicore.problems.enhancedttsp;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.shorts.ShortList;

public abstract class EnhancedTTSPBinaryTelescopeNode {
	protected final EnhancedTTSPBinaryTelescopeNode parent;

	public EnhancedTTSPBinaryTelescopeNode(final EnhancedTTSPBinaryTelescopeNode parent) {
		super();
		this.parent = parent;
	}

	public abstract EnhancedTTSPState getState();
	public abstract short getCurLocation();
	public abstract ShortList getCurTour();

	public static class EnhancedTTSPBinaryTelescopeDeterminedDestinationNode extends EnhancedTTSPBinaryTelescopeNode {
		private final EnhancedTTSPState state;

		public EnhancedTTSPBinaryTelescopeDeterminedDestinationNode(final EnhancedTTSPBinaryTelescopeNode parent, final EnhancedTTSPState state) {
			super(parent);
			this.state = state;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.state == null) ? 0 : this.state.hashCode());
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
			EnhancedTTSPBinaryTelescopeDeterminedDestinationNode other = (EnhancedTTSPBinaryTelescopeDeterminedDestinationNode) obj;
			if (this.state == null) {
				if (other.state != null) {
					return false;
				}
			} else if (!this.state.equals(other.state)) {
				return false;
			}
			return true;
		}

		@Override
		public short getCurLocation() {
			return this.state.getCurLocation();
		}

		@Override
		public ShortList getCurTour() {
			return this.state.getCurTour();
		}

		@Override
		public EnhancedTTSPState getState() {
			return this.state;
		}

		@Override
		public String toString() {
			return "EnhancedTTSPBinaryTelescopeDeterminedDestinationNode [state=" + this.state + "]";
		}
	}

	public static class EnhancedTTSPBinaryTelescopeDestinationDecisionNode extends EnhancedTTSPBinaryTelescopeNode {
		public EnhancedTTSPBinaryTelescopeDestinationDecisionNode(final EnhancedTTSPBinaryTelescopeNode parent, final boolean bitChoice) {
			super(parent);
			this.bitChoice = bitChoice;
		}

		private final boolean bitChoice; // false if left child, true if right child

		@Override
		public ShortList getCurTour() {
			return this.parent.getCurTour();
		}

		@Override
		public short getCurLocation() {
			return this.parent.getCurLocation();
		}

		public boolean isBitChoice() {
			return this.bitChoice;
		}

		public List<Boolean> getField() {
			if (this.parent instanceof EnhancedTTSPBinaryTelescopeDeterminedDestinationNode) {
				List<Boolean> l = new ArrayList<>();
				l.add(this.bitChoice);
				return l;
			}
			List<Boolean> l = ((EnhancedTTSPBinaryTelescopeDestinationDecisionNode)this.parent).getField();
			l.add(this.bitChoice);
			return l;
		}

		@Override
		public EnhancedTTSPState getState() {
			return this.parent.getState();
		}

		@Override
		public String toString() {
			return "EnhancedTTSPBinaryTelescopeDestinationDecisionNode [bitChoice=" + this.bitChoice + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (this.bitChoice ? 1231 : 1237);
			result += this.parent.hashCode();
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
			EnhancedTTSPBinaryTelescopeDestinationDecisionNode other = (EnhancedTTSPBinaryTelescopeDestinationDecisionNode) obj;
			if (this.bitChoice != other.bitChoice) {
				return false;
			}
			if (this.parent == null) {
				if (other.parent != null) {
					return false;
				}
			} else if (!this.parent.equals(other.parent)) {
				return false;
			}
			return true;
		}
	}
}
