package iarfmoose;

import java.util.List;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class Queen extends RangedUnit {
	
	private static final float TRANSFUSION_COST = 50;
	private static final float INJECT_LARVA_COST = 25;
	private static final float SPAWN_CREEP_TUMOR_COST = 25;
	private static final float TRANSFUSION_RANGE = 7;
	
	public Queen(S2Agent bot, UnitInPool unitInPool) {
		super(bot, unitInPool);
	}
	
	@Override
	public void attack(Point2d target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (canTransfuse()) {
			transfuseNearbyUnit();
		} else if (canKite()) {
			kite();
		} else if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.ATTACK, target)) {
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, target, false);
		}
	}
	
	@Override
	public void attack(UnitInPool target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (canTransfuse()) {
			transfuseNearbyUnit();
		} else if (canKite()) {
			kite();
		} else if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.ATTACK, target)) {
			Point2d targetPosition = target.unit().getPosition().toPoint2d();
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, targetPosition, false);
		}
	}
	
	public boolean spawnCreepTumor(Point2d target) throws UnitNotFoundException {
		UnitInPool queen = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (queen.unit().getEnergy().orElse((float) 0) >= SPAWN_CREEP_TUMOR_COST &&
				!alreadyIssued(Abilities.BUILD_CREEP_TUMOR_QUEEN)) {
			bot.actions().unitCommand(
					queen.unit(), 
					Abilities.BUILD_CREEP_TUMOR_QUEEN, 
					target, 
					false);
			return true;
		}
		return false;
	}
	
	public boolean injectLarva(UnitInPool targetHatchery) throws UnitNotFoundException {
		UnitInPool queen = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (queen.unit().getEnergy().orElse((float) 0) >= INJECT_LARVA_COST && 
				!alreadyIssued(Abilities.EFFECT_INJECT_LARVA)) {
				bot.actions().unitCommand(
						queen.unit(), 
						Abilities.EFFECT_INJECT_LARVA, 
						targetHatchery.unit(), 
						false);
				return true;
		}
		return false;
	}
		
	public boolean transfuse(UnitInPool target) throws UnitNotFoundException {
		UnitInPool queen = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (target != null && !alreadyIssued(Abilities.EFFECT_TRANSFUSION)) {
			bot.actions().unitCommand(
					queen.unit(), 
					Abilities.EFFECT_TRANSFUSION, 
					target.unit(), 
					false);
			return true;
		}
		return false;
	}
	
	public boolean transfuseNearbyUnit() throws UnitNotFoundException {
		UnitInPool queen = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		UnitInPool target = findMostDamagedUnitInTransfusionRange();
		if (target != null && !alreadyIssued(Abilities.EFFECT_TRANSFUSION)) {
			bot.actions().unitCommand(
					queen.unit(), 
					Abilities.EFFECT_TRANSFUSION, 
					target.unit(), 
					false);
			return true;
		}
		return false;
	}
	
	private boolean canTransfuse() throws UnitNotFoundException {
		UnitInPool queen = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		return queen.unit().getEnergy().orElse((float) 0) >= TRANSFUSION_COST;
	}
		
	private UnitInPool findMostDamagedUnitInTransfusionRange() throws UnitNotFoundException {
		UnitInPool queen = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		Point queenPosition = queen.unit().getPosition();
		List<UnitInPool> nearbyUnits = bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				unitInPool.unit().getPosition().distance(queenPosition) <= TRANSFUSION_RANGE);
		UnitInPool mostDamagedUnit = null;
		float biggestHealthGap = 0;
		for (UnitInPool nearbyUnit : nearbyUnits) {
			float maximumHealth = nearbyUnit.unit().getHealthMax().orElse((float) 0);
			float currentHealth = nearbyUnit.unit().getHealth().orElse((float) 0);
			float healthGap = maximumHealth - currentHealth;
			if (mostDamagedUnit == null || healthGap > biggestHealthGap) {
				mostDamagedUnit = nearbyUnit;
				biggestHealthGap = healthGap;
			}
		}
		return mostDamagedUnit;
	}
	
}
