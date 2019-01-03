package iarfmoose;

import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Buff;
import com.github.ocraft.s2client.protocol.data.Buffs;

public class Hatchery {
	
	//actually loop value for larva inject is 650
	//the reduced value is so that queens prioritise keeping hatcheries injected
	private static final long INJECT_TIMER_LENGTH = 600;
	
	private S2Agent bot;
	private UnitInPool hatchery;
	private long lastInjectLoop;
	
	public Hatchery(S2Agent bot, UnitInPool hatchery) {
		this.bot = bot;
		this.hatchery = hatchery;
		this.lastInjectLoop = 0;
	}
	
	public UnitInPool getHatchery() {
		return hatchery;
	}
	
	public long getLastInjectLoop() {
		return lastInjectLoop;
	}
	
	public void setLastInjectLoop(long currentLoop) {
		this.lastInjectLoop = currentLoop;
	}
	
	public boolean currentlySpawningLarva() {
		return bot.observation().getGameLoop() - lastInjectLoop < INJECT_TIMER_LENGTH;
	}
	
	public boolean hasBeenInjected() {
		Set<Buff> buffs = hatchery.unit().getBuffs();
		for (Buff buff : buffs) {
			if (buff == Buffs.QUEEN_SPAWN_LARVA_TIMER) {
				return true;
			}
		}
		return false;
	}
}
