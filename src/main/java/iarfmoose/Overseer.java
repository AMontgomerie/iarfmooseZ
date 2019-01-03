package iarfmoose;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;

public class Overseer extends Overlord {
	
	private static final float CHANGELING_COST = 50;
	private static final float CONTAMINATE_COST = 125;
	
	public Overseer(S2Agent bot, UnitInPool unitInPool) {
		super(bot, unitInPool);
	}
	
	@Override
	public void generateCreep() {}
	
	public void oversight() {
		//TODO
	}
	
	public void spawnChangeling() throws UnitNotFoundException {
		UnitInPool overseer = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (overseer.unit().getEnergy().orElse((float) 0) >= CHANGELING_COST) {
			bot.actions().unitCommand(overseer.unit(), Abilities.EFFECT_SPAWN_CHANGELING, false);
		}
	}
	
	public void contaminate(UnitInPool target) throws UnitNotFoundException {
		UnitInPool overseer = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (overseer.unit().getEnergy().orElse((float) 0) >= CONTAMINATE_COST &&
				!alreadyIssued(Abilities.EFFECT_CONTAMINATE, target)) {
			bot.actions().unitCommand(overseer.unit(), Abilities.EFFECT_CONTAMINATE, target.unit(), false);
		}
	}
}
