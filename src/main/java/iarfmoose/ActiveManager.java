package iarfmoose;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;

public abstract class ActiveManager implements Manager {
	
	S2Agent bot;
	BaseLocator baseLocator;
	
	public ActiveManager(S2Agent bot, BaseLocator baseLocator) {
		this.bot = bot;
		this.baseLocator = baseLocator;
	}
	
	public abstract void onStep();
	public abstract void onUnitCreated(UnitInPool unitInPool);
	public abstract void onUnitDestroyed(UnitInPool unitInPool);
}
