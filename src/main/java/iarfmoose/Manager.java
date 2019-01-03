package iarfmoose;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;

public interface Manager {
	
	public abstract void onUnitCreated(UnitInPool unitInPool);
	public abstract void onUnitDestroyed(UnitInPool unitInPool);
}
