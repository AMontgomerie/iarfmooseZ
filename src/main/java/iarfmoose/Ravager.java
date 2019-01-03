package iarfmoose;

import java.util.List;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class Ravager extends RangedUnit {
		
	private static final int CORROSIVE_BILE_COOLDOWN = 157;
	private static final int CORROSIVE_BILE_RANGE = 9;
	
	private long lastCorrosiveBileLoop;
	
	public Ravager(S2Agent bot, UnitInPool unitInPool) {
		super(bot, unitInPool);
		armyUnitType = ArmyUnitType.RAVAGER;
		lastCorrosiveBileLoop = 0;
	}
	
	@Override
	public void attack(Point2d target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (canCastCorrosiveBile()) {
			castCorrosiveBile();
		} else if (canKite()) {
			kite();
		} else if (!dodgeNearbyEffects() && 
				!alreadyIssued(Abilities.EFFECT_CORROSIVE_BILE) &&
				!alreadyIssued(Abilities.ATTACK, target)) {
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, target, false);
		}
	}
	
	@Override
	public void attack(UnitInPool target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (canCastCorrosiveBile()) {
			castCorrosiveBile();
		} else if (canKite()) {
			kite();
		} else if (!dodgeNearbyEffects() &&
				!alreadyIssued(Abilities.EFFECT_CORROSIVE_BILE) &&
				!alreadyIssued(Abilities.ATTACK, target)) {
			Point2d targetPosition = target.unit().getPosition().toPoint2d();
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, targetPosition, false);
		}
	}
	
	private boolean canCastCorrosiveBile() throws UnitNotFoundException {
		UnitInPool ravager = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		Point ravagerPosition = ravager.unit().getPosition();
		UnitInPool target = getClosestEnemy();
		if (target != null && 
				target.unit().getPosition().distance(ravagerPosition) <= CORROSIVE_BILE_RANGE && 
				bot.observation().getGameLoop() > lastCorrosiveBileLoop + CORROSIVE_BILE_COOLDOWN) {
			return true;
		} else return false;
	}
	
	private void castCorrosiveBile() throws UnitNotFoundException {
		UnitInPool ravager = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		UnitInPool target = getClosestTarget();
		if (target != null) {
			bot.actions().unitCommand(
					ravager.unit(), 
					Abilities.EFFECT_CORROSIVE_BILE, 
					target.unit().getPosition().toPoint2d(), 
					false);
			lastCorrosiveBileLoop = bot.observation().getGameLoop();
		}
	}
	
	private UnitInPool getClosestTarget() {
		Point unitPosition = getUnitPosition();
		UnitInPool closestTarget = null;
		for (UnitInPool target : getTargets()) {
			Point enemyPosition = target.unit().getPosition();
			if (closestTarget == null ||
					enemyPosition.distance(unitPosition) < 
					closestTarget.unit().getPosition().distance(unitPosition)) {
				closestTarget = target;
			}
		}
		return closestTarget;
	}
	
	private List<UnitInPool> getTargets() {
		return bot.observation().getUnits(unitInPool ->
				unitInPool.unit().getAlliance() == Alliance.ENEMY ||
				unitInPool.unit().getType() == Units.NEUTRAL_FORCE_FIELD);
	}
}
