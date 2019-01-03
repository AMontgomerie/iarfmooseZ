package iarfmoose;

import java.util.List;
import java.util.Optional;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class WorkerScouter {
	
	private static final int ATTACK_RADIUS = 15;
	private static final int MINIMUM_HP = 10;
	
	private S2Agent bot;
	private List<Base> enemyStartLocations;
	private List<Base> bases;
	private UnitInPool scout;
	private UnitInPool proxyScout;
	private UnitInPool enemyMain;
	private Base mainBase;
	private boolean scoutedForProxies;
	
	public WorkerScouter(S2Agent bot, BaseLocator baseLocator) {
		this.bot = bot;
		this.enemyStartLocations = baseLocator.getEnemyStartLocationBases();
		this.bases = baseLocator.getBasesSortedClosestFirst();
		this.scout = null;
		this.proxyScout = null;
		this.enemyMain = null;
		this.mainBase = baseLocator.getMainBase();
		this.scoutedForProxies = false;
	}
	
	public void setScout(UnitInPool worker) {
		scout = worker;
	}
	
	public void setProxyScout(UnitInPool worker) {
		proxyScout = worker;
	}
	
	public boolean scoutNotYetAssigned() {
		return scout == null;
	}
	
	public boolean proxyScoutNotYetAssigned() {
		return proxyScout == null;
	}
	
	public void update(GameState currentState) {
		updateEnemyBaseScout();
		updateProxyScout(currentState.getThreats());
	}
	
	private void updateEnemyBaseScout() {
		if (scout != null && scout.isAlive()) {
			if (enemyMain == null) {
				checkForEnemyMain();
				scoutEnemyStartLocations();
			} else {
				harass();
			}
		}
	}
	
	private void updateProxyScout(List<Threat> threats) {
		if (proxyScout != null && proxyScout.isAlive()) {
			if (!scoutedForProxies) {
				scoutNearbyBases();
			} else {
				attackProxy(threats);
			}
		}
	}
	
	private void checkForEnemyMain() {
		List<UnitInPool> townHalls = bot.observation().getUnits(
				Alliance.ENEMY, 
				unit -> UnitData.isTownHall(unit.unit().getType()));
		if (townHalls.size() > 0) {
			enemyMain = townHalls.get(0);
		}
	}
	
	private void scoutEnemyStartLocations() {
		List<UnitOrder> orders = scout.unit().getOrders();
		if (orders.size() == 0 || orders.get(0).getAbility() != Abilities.MOVE) {
			for (Base enemyStartLocation : enemyStartLocations) {
				bot.actions().unitCommand(
						scout.unit(), 
						Abilities.MOVE, 
						enemyStartLocation.getExpansionLocation().toPoint2d(), 
						true);
			}
		}
	}
	
	private void harass() {
		if (canAttack()) {
			attack();
		} else {
			retreat();
		}
	}
	
	private boolean canAttack() {
		if (scout.unit().getHealth().orElse((float) 0) < MINIMUM_HP) {
			return false;
		} else {
			return true;
		}
	}
	
	private void retreat() {
		if (!currentlyRetreating()) {
			UnitInPool mineral = getRetreatMineral().orElse(scout);
			bot.actions().unitCommand(scout.unit(), Abilities.SMART, mineral.unit(), false);
		}	
	}
	
	private boolean currentlyRetreating() {
		if (scout.unit().getOrders().isEmpty() ||
				scout.unit().getOrders().get(0).getAbility() != Abilities.HARVEST_GATHER) {
			return false;
		} else {
			return true;
		}
	}
	
	private Optional<UnitInPool> getRetreatMineral() {
		if (mainBase.getMinerals().isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(mainBase.getMinerals().get(0));
		}
	}
	
	private void attack() {
		UnitInPool target = findTargetEnemyWorker();
		if (targetIsValid(target)) {
			if (!currentlyAttacking()) {
				bot.actions().unitCommand(
						scout.unit(), 
						Abilities.ATTACK, 
						target.unit(), 
						false);
			}
		} else if (currentlyRetreating()) {
			bot.actions().unitCommand(scout.unit(), 
					Abilities.MOVE, 
					enemyMain.unit().getPosition().toPoint2d(), 
					false);
		}
	}
	
	private boolean targetIsValid(UnitInPool target) {
		if (target == null ||
				target.unit().getPosition().distance(enemyMain.unit().getPosition())
				> ATTACK_RADIUS) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean currentlyAttacking() {
		if (scout.unit().getOrders().isEmpty() ||
				scout.unit().getOrders().get(0).getAbility() != Abilities.ATTACK) {
			return false;
		} else {
			return true;
		}
	}
	
	private void scoutNearbyBases() {
		for (int i = 0; i < bases.size() / 2; i++) {
			Point baseLocation = bases.get(i).getExpansionLocation();
			bot.actions().unitCommand(
					proxyScout.unit(), 
					Abilities.ATTACK, 
					baseLocation.toPoint2d(), 
					true);
		}
		bot.actions().unitCommand(
				proxyScout.unit(), 
				Abilities.ATTACK, 
				mainBase.getExpansionLocation().toPoint2d(), 
				true);
		scoutedForProxies = true;
	}
	
	private void attackProxy(List<Threat> threats) {
		for (Threat threat : threats) {
			if (threat.containsStructure()) {
				bot.actions().unitCommand(
						proxyScout.unit(), 
						Abilities.ATTACK, 
						threat.getCentreOfGroup().toPoint2d(), 
						false);
			}
		}
	}
		
	private UnitInPool findTargetEnemyWorker() {
		UnitInPool target = null;
		List<UnitInPool> enemies = bot.observation().getUnits(
				Alliance.ENEMY, 
				unit -> 
				unit.unit().getDisplayType() == DisplayType.VISIBLE &&
				UnitData.isWorker(unit.unit().getType()));
		if (enemies.size() > 0) {
			target = getClosestUnit(enemies);
		}
		return target;
	}
	
	private UnitInPool getClosestUnit(List<UnitInPool> units) {
		Point scoutPosition = scout.unit().getPosition();
		UnitInPool closestUnit = null;
		for (UnitInPool unitInPool : units) {
			Point unitPosition = unitInPool.unit().getPosition();
			if (closestUnit == null || 
					scoutPosition.distance(unitPosition) < 
					scoutPosition.distance(closestUnit.unit().getPosition())) {
				closestUnit = unitInPool;
			}
		}
		return closestUnit;
	}
}
