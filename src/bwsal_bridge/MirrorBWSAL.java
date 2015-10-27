package bwsal_bridge;

import bwapi.Mirror;

public class MirrorBWSAL extends Mirror {
	public MirrorBWSAL() {
		super();
		GameHandler.setMirror(this);
	}
}
