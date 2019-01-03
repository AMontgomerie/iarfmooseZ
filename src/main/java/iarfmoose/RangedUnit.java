package iarfmoose;

import java.util.Optional;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.data.Weapon.TargetType;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class RangedUnit extends ArmyUnit {

	protected Weapon airWeapon;
	protected Weapon groundWeapon;
	
	public RangedUnit(S2Agent bot, UnitInPool unitInPool) {
		super(bot, unitInPool);
		armyUnitType = ArmyUnitType.RANGED_UNIT;
		initialiseWeapons();
	}
	
	private void initialiseWeapons() {
		airWeapon = null;
		groundWeapon = null;
		Optional<UnitInPool> unitInPool = getUnitInPool();
		if (unitInPool.isPresent()) {
			UnitTypeData unitData = bot.observation()
					.getUnitTypeData(false)
					.get(unitInPool.get().unit().getType());
			for(Weapon weapon : unitData.getWeapons()) {
				if(weapon.getTargetType() == TargetType.ANY) {
					groundWeapon = weapon;
					airWeapon = weapon;
				} else if (weapon.getTargetType() == TargetType.GROUND) {
					groundWeapon = weapon;
				} else if (weapon.getTargetType() == TargetType.AIR) {
					airWeapon = weapon;
				}
			}
		}
	}
	
	@Override
	public void attack(Point2d target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (canKite()) {
			kite();
		} else if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.ATTACK, target)) {
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, target, false);
		}
	}
	
	@Override
	public void attack(UnitInPool target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (canKite()) {
			kite();
		} else if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.ATTACK, target)) {
			Point2d targetPosition = target.unit().getPosition().toPoint2d();
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, targetPosition, false);
		}
	}
	
	protected boolean canKite() {
		UnitInPool targetEnemy = getClosestEnemy();
		if (targetEnemy != null && withinRange(targetEnemy) && onCoolDown()) {
			UnitType enemyType = targetEnemy.unit().getType();
			boolean flyingEnemy = targetEnemy.unit().getFlying().orElse(false);
			Weapon enemyWeapon = getEnemyWeapon(enemyType);
			if (flyingEnemy) {
				return outranges(airWeapon, enemyWeapon);
			} else {
				return outranges(groundWeapon, enemyWeapon);
			}
		}
		return false;
	}
	
	private boolean withinRange(UnitInPool enemy) {
		Point unitPosition = getUnitPosition();
		Point enemyPosition = enemy.unit().getPosition();
		boolean flyingEnemy = enemy.unit().getFlying().orElse(false);
		if (flyingEnemy && airWeapon != null) {
			return unitPosition.distance(enemyPosition) < airWeapon.getRange();
		} else if (groundWeapon != null) {
			return unitPosition.distance(enemyPosition) < groundWeapon.getRange();
		} else return false;
	}
	
	private boolean onCoolDown() {
		try {
			UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			return unitInPool.unit().getWeaponCooldown().orElse((float) 0) > 0;
		} catch (UnitNotFoundException e) {
			return false;
		}
		
	}
		
	private Weapon getEnemyWeapon(UnitType enemyType) {
		UnitTypeData unitData = bot.observation().getUnitTypeData(false).get(enemyType);
		for (Weapon weapon : unitData.getWeapons()) {
			if (weapon.getTargetType() != TargetType.AIR) {
				return weapon;
			}
		}
		return null;
	}
	
	private boolean outranges(Weapon weaponA, Weapon weaponB) {
		if (weaponA == null) {
			return false;
		} else if (weaponB == null) {
			return true;
		}
		if (weaponA.getRange() > weaponB.getRange()) {
			return true;
		} else {
			return false;
		}
	}
	
	protected void kite() throws UnitNotFoundException {
		UnitInPool enemy = getClosestEnemy();
		if (enemy != null) {
			Point2d retreatPosition = calculateRetreatPositionFrom(enemy.unit().getPosition());
			move(retreatPosition);
		}
	}
	

}
