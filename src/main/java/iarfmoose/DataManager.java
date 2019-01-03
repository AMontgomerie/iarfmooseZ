package iarfmoose;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;

public abstract class DataManager implements Manager {

	S2Agent bot;
	
	DataManager(S2Agent bot) {
		this.bot = bot;
	}
	
	@Override
	public abstract void onUnitCreated(UnitInPool unitInPool);

	@Override
	public abstract void onUnitDestroyed(UnitInPool unitInPool);

}
