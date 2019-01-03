package iarfmoose;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;

public class ArmyUnitTypeHandler {
	
	private S2Agent bot;
	
	public ArmyUnitTypeHandler(S2Agent bot) {
		this.bot = bot;
	}
	
	public ArmyUnit assignBehaviourType(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		if (unitType == Units.ZERG_RAVAGER) {
			return new Ravager(bot, unitInPool);
		} else if (UnitData.isRanged(unitType)) {
			return new RangedUnit(bot, unitInPool);
		} else {
			return new ArmyUnit(bot, unitInPool);
		}
	}
	
	public boolean isCorrectType(ArmyUnit armyUnit) {
		UnitType unitType = armyUnit.getLastKnownType();
		if ((unitType == Units.ZERG_RAVAGER && 
				armyUnit.getType() != ArmyUnitType.RAVAGER)) {
			return false;
		} else if ((unitType == Units.ZERG_LURKER_MP || 
				unitType == Units.ZERG_LURKER_MP_BURROWED) && 
				armyUnit.getType() != ArmyUnitType.LURKER) {
			return false;
		} else {
			return true;
		}
	}
}
