package iarfmoose;

import java.util.List;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.EffectData;
import com.github.ocraft.s2client.protocol.observation.raw.EffectLocations;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class ArmyUnit extends ActiveUnit {
	
	private static final float RETREAT_DISTANCE = 5;
	private static final float RADIUS_BUFFER = 3;
	
	ArmyUnitType armyUnitType;
	
	public ArmyUnit(S2Agent bot, UnitInPool unitInPool) {
		super(bot, unitInPool);
		armyUnitType = ArmyUnitType.ARMY_UNIT;
	}
	
	public ArmyUnitType getType() {
		return armyUnitType;
	}
	
	public boolean isIdle() {
		try {
			UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			if (unitInPool.unit().getOrders().isEmpty()) {
				return true;
			} else {
				return false;
			}
		} catch (UnitNotFoundException e) {
			return false;
		}
	}
	
	public void attack(Point2d target) throws UnitNotFoundException {
		if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.ATTACK, target)) {
			UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, target, false);
		} 
	}
	
	public void attack(UnitInPool target) throws UnitNotFoundException {
		if (!dodgeNearbyEffects() && !alreadyIssued(Abilities.ATTACK, target)) {
			UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			Point2d targetPosition = target.unit().getPosition().toPoint2d();
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, targetPosition, false);
		} 
	}
	
	public void move(Point2d target) throws UnitNotFoundException {
		if (!alreadyIssued(Abilities.MOVE, target)) {
			UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			bot.actions().unitCommand(unitInPool.unit(), Abilities.MOVE, target, false);
		}
	}
	
	public void shiftMove(Point2d target) throws UnitNotFoundException {
		if (!alreadyIssued(Abilities.MOVE, target)) {
			UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			bot.actions().unitCommand(unitInPool.unit(), Abilities.MOVE, target, true);
		}
	}
	
	protected boolean dodgeNearbyEffects() throws UnitNotFoundException {
		Point2d unitPosition = getUnitPosition().toPoint2d();
		for (EffectLocations effectLocation : bot.observation().getEffects()) {
			EffectData effectData = bot.observation().getEffectData(false).get(effectLocation.getEffect());
			float radius = effectData.getRadius();
			if (effectLocation.getEffect().getEffectId() == 7) { //nuke dot
				radius = 8;
			}
			if (iarfmoose.EffectData.isAThreat(effectData.getEffect())) {
				for (Point2d effectPosition : effectLocation.getPositions()) {
					if (unitPosition.distance(effectPosition) <= radius + RADIUS_BUFFER) {
						Point2d retreatPosition = calculateRetreatPositionFrom(effectPosition);
						move(retreatPosition);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected boolean alreadyIssued(Ability ability) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		List<UnitOrder> orders = unitInPool.unit().getOrders();
		if (orders.isEmpty()) {
			return false;
		} else {
			UnitOrder currentOrder = orders.get(0);
			if (AbilityData.areEquivalent(currentOrder.getAbility(), ability)) {
				return false;
			} else return true;
		}
	}
	
	protected boolean alreadyIssued(Ability ability, Point2d target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		List<UnitOrder> orders = unitInPool.unit().getOrders();
		if (orders.isEmpty()) {
			return false;
		} else {
			UnitOrder currentOrder = orders.get(0);
			if (!AbilityData.areEquivalent(currentOrder.getAbility(), ability)) {
				return false;
			} else {
				Point currentTarget = currentOrder.getTargetedWorldSpacePosition().orElse(Point.of(0, 0));
				if (currentTarget.toPoint2d().distance(target) < 2) {
					return true;
				} else {
					return false;
				}
			}
		}
	}
	
	protected boolean alreadyIssued(Ability ability, UnitInPool target) throws UnitNotFoundException {
		UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		List<UnitOrder> orders = unitInPool.unit().getOrders();
		if (orders.isEmpty()) {
			return false;
		} else {
			UnitOrder currentOrder = orders.get(0);
			if (!AbilityData.areEquivalent(currentOrder.getAbility(), ability)) {
				return false;
			} else {
				int currentTarget = currentOrder.getTargetedUnitTag().hashCode();
				if (currentTarget == target.getTag().hashCode()) {
					return true;
				} else {
					return false;
				}
			}
		}
	}
	
	protected Point2d calculateRetreatPositionFrom(Point threatPosition) {
		Point unitPosition = getUnitPosition();
		float x = 0;
		float y = 0;
		if (unitPosition.getX() > threatPosition.getX()) {
			x = unitPosition.getX() + RETREAT_DISTANCE;
		} else if (unitPosition.getX() < threatPosition.getX()) {
			x = unitPosition.getX() - RETREAT_DISTANCE;
		} 
		if (unitPosition.getY() > threatPosition.getY()) {
			y = unitPosition.getY() + RETREAT_DISTANCE;
		} else if (unitPosition.getY() < threatPosition.getY()) {
			y = unitPosition.getY() - RETREAT_DISTANCE;
		}
		return Point2d.of(x, y);
	}
	
	protected Point2d calculateRetreatPositionFrom(Point2d threatPosition) {
		Point unitPosition = getUnitPosition();
		float x = 0;
		float y = 0;
		if (unitPosition.getX() > threatPosition.getX()) {
			x = unitPosition.getX() + RETREAT_DISTANCE;
		} else if (unitPosition.getX() < threatPosition.getX()) {
			x = unitPosition.getX() - RETREAT_DISTANCE;
		} 
		if (unitPosition.getY() > threatPosition.getY()) {
			y = unitPosition.getY() + RETREAT_DISTANCE;
		} else if (unitPosition.getY() < threatPosition.getY()) {
			y = unitPosition.getY() - RETREAT_DISTANCE;
		}
		return Point2d.of(x, y);
	}
	
	protected Point getUnitPosition() {
		try {
			UnitInPool unitInPool = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			return unitInPool.unit().getPosition();
		} catch (UnitNotFoundException e) {
			return Point.of(0, 0);
		}
	}
	
	protected UnitInPool getClosestEnemy() {
		Point unitPosition = getUnitPosition();
		UnitInPool closestEnemy = null;
		for (UnitInPool enemy : getEnemyUnits()) {
			Point enemyPosition = enemy.unit().getPosition();
			if (closestEnemy == null ||
					enemyPosition.distance(unitPosition) < 
					closestEnemy.unit().getPosition().distance(unitPosition)) {
				closestEnemy = enemy;
			}
		}
		return closestEnemy;
	}
	
	protected UnitInPool getClosestAntiAirEnemy() {
		Point unitPosition = getUnitPosition();
		UnitInPool closestEnemy = null;
		for (UnitInPool enemy : getAntiAirEnemyUnits()) {
			Point enemyPosition = enemy.unit().getPosition();
			if (closestEnemy == null ||
					enemyPosition.distance(unitPosition) < 
					closestEnemy.unit().getPosition().distance(unitPosition)) {
				closestEnemy = enemy;
			}
		}
		return closestEnemy;
	}
	
	private List<UnitInPool> getEnemyUnits() {
		return bot.observation().getUnits(
				Alliance.ENEMY, 
				enemy -> 
				!UnitData.isStructure(enemy.unit().getType()));
	}
	
	private List<UnitInPool> getAntiAirEnemyUnits() {
		return bot.observation().getUnits(
				Alliance.ENEMY, 
				enemy -> 
				UnitData.canAttackAir(enemy.unit().getType()));
	}
}
