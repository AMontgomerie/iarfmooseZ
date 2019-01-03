package iarfmoose;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class Lurker extends RangedUnit {

	public Lurker (S2Agent bot, UnitInPool unitInPool) {
		super(bot, unitInPool);
		armyUnitType = ArmyUnitType.LURKER;
	}
	
	@Override
	public void attack(Point2d target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		UnitType lurkerType = unitInPool.unit().getType();
		Point2d lurkerPosition = unitInPool.unit().getPosition().toPoint2d();
		if (lurkerType == Units.ZERG_LURKER_MP) {
			if (lurkerPosition.distance(target) > groundWeapon.getRange()) {
				if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.MOVE, target)) {
					bot.actions().unitCommand(unitInPool.unit(), Abilities.MOVE, target, false);
				}
			} else {
				if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.BURROW_DOWN_LURKER, target)) {
					bot.actions().unitCommand(unitInPool.unit(), Abilities.BURROW_DOWN_LURKER, false);
				}
			}
		} else if (lurkerType == Units.ZERG_LURKER_MP_BURROWED) {
			if (lurkerPosition.distance(target) > groundWeapon.getRange()) {
				if (!alreadyIssued(Abilities.BURROW_UP_LURKER, target)) {
					bot.actions().unitCommand(unitInPool.unit(), Abilities.BURROW_UP_LURKER, false);
				}
			}
		}
	}
	
	@Override
	public void attack(UnitInPool target) throws UnitNotFoundException {
		Point2d targetPosition = target.unit().getPosition().toPoint2d();
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		UnitType lurkerType = unitInPool.unit().getType();
		Point2d lurkerPosition = unitInPool.unit().getPosition().toPoint2d();
		if (lurkerType == Units.ZERG_LURKER_MP) {
			if (lurkerPosition.distance(targetPosition) > groundWeapon.getRange()) {
				if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.MOVE, target)) {
					bot.actions().unitCommand(unitInPool.unit(), Abilities.MOVE, targetPosition, false);
				}
			} else {
				if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.BURROW_DOWN_LURKER, targetPosition)) {
					bot.actions().unitCommand(unitInPool.unit(), Abilities.BURROW_DOWN_LURKER, false);
				}
			}
		} else if (lurkerType == Units.ZERG_LURKER_MP_BURROWED) {
			if (lurkerPosition.distance(targetPosition) > groundWeapon.getRange()) {
				if (!alreadyIssued(Abilities.BURROW_UP_LURKER, target)) {
					bot.actions().unitCommand(unitInPool.unit(), Abilities.BURROW_UP_LURKER, false);
				}
			}
		}
	}
}
