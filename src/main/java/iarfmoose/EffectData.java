package iarfmoose;

import com.github.ocraft.s2client.protocol.data.Effect;

public abstract class EffectData {

	public static boolean isAThreat(Effect effect) {
		switch (effect.getEffectId()) {
		case 1: //psistorm
		case 7: //nuke dot
		case 8: //liberator defender zone setup
		case 9: //defender zone
		case 10://blinding cloud
			return true;
		default:
			return false;
		}
	}
}
