package iarfmoose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class WorkerManager extends ResponsiveManager {

	private static final float NEW_BUILD_RADIUS = 5;
	private static final float BASE_RADIUS = 15;
	private static final float FIGHT_RADIUS = 15;
	private static final int MINIMUM_DEFENDER_HP = 10;
	private static final int PROXY_WINDOW_START = 1120;
	private static final int PROXY_WINDOW_END = 4000;
	private WorkerScouter workerScouter;
	
	private Set<UnitInPool> workers;
	private Set<UnitInPool> defenders;
	private Set<UnitInPool> gases;
	private Set<UnitInPool> hatcheries;
	private UnitInPool currentBuilder;
	private GameState currentGameState;
	
	public WorkerManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		workerScouter = new WorkerScouter(bot, baseLocator);
		workers = new HashSet<UnitInPool>();
		defenders = new HashSet<UnitInPool>();
		gases = new HashSet<UnitInPool>();
		hatcheries = new HashSet<UnitInPool>();
		currentBuilder = null;
		currentGameState = new GameState();
	}
	
	@Override
	public void onStep(GameState currentGameState) {
		this.currentGameState = currentGameState;
		if(workerScouter.scoutNotYetAssigned()) {
			assignScout();
		}
		if (workerScouter.proxyScoutNotYetAssigned() && needToScoutForProxies()) {
			assignProxyScout();
		}
		workerScouter.update(currentGameState);
		float ourArmySupply = currentGameState
				.findPlayerState(Alliance.SELF).orElse(new PlayerState())
				.getArmySupply().orElse((float) 0);
		if (currentGameState.getThreats().size() > 0 && ourArmySupply < 20) {
			workerDefence();
		} else {
			clearDefenders();
		}
		if (defenders.isEmpty()) {
			updateGasSaturation();
			updateMineralSaturation();
		}
	}

	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		if(UnitData.isWorker(unitType)) {
			workers.add(unitInPool);
		} else if (UnitData.isStructure(unitType)) {
			builderMorphedIntoStructure();
			if (unitType == Units.ZERG_EXTRACTOR) {
				addGas(unitInPool);
			} else if (unitType == Units.ZERG_HATCHERY) {
				addHatchery(unitInPool);
			}
		}
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		workers.remove(unitInPool);
		if (unitType == Units.ZERG_EXTRACTOR) {
			removeGas(unitInPool);
		} else if (unitType == Units.ZERG_HATCHERY) {
			removeHatchery(unitInPool);
		}
	}
	
	public void onUnitIdle(UnitInPool unitInPool) {
		returnToMining(unitInPool);
	}
	
	public int getWorkerCount() {
		return workers.size();
	}
	
	public void moveBuilderTo(Point point) {
		if (currentBuilder == null || !currentBuilder.isAlive()) {
			findBuilderCloseTo(point);
		}
		if (currentBuilder != null) {		
			if (currentBuilder.unit().getPosition().distance(point) > NEW_BUILD_RADIUS) {
			bot.actions().unitCommand(
					currentBuilder.unit(), 
					Abilities.MOVE,
					point.toPoint2d(), 
					false);
			} else {
				List<UnitInPool> nearbyUnits = bot.observation().getUnits(
						unitInPool -> 
						(UnitData.isStructure(unitInPool.unit().getType()) || 
						unitInPool.unit().getAlliance() == Alliance.ENEMY ||
						UnitData.isCreepTumor(unitInPool.unit().getType())) &&
						unitInPool.unit().getPosition().distance(point) <= NEW_BUILD_RADIUS);
				if (!nearbyUnits.isEmpty()) {
					UnitInPool target = nearbyUnits.get(0);
					bot.actions().unitCommand(currentBuilder.unit(), Abilities.ATTACK, target.unit(), false);
				}
			}
		}
	}
	
	private void returnToMining(UnitInPool unitInPool) {
		Unit closestMineral = findClosestMineral(unitInPool).unit();
		if (closestMineral.getDisplayType() == DisplayType.VISIBLE) {
			bot.actions().unitCommand(
					unitInPool.unit(), 
					Abilities.SMART, closestMineral, 
					false);
		} else {
			bot.actions().unitCommand(
					unitInPool.unit(), 
					Abilities.MOVE, 
					closestMineral.getPosition().toPoint2d(),
					false);
		}
	}
	
	private void addGas(UnitInPool unitInPool) {
		gases.add(unitInPool);
	}
	
	private void removeGas(UnitInPool unitInPool) {
		gases.remove(unitInPool);
	}
	
	private void addHatchery(UnitInPool unitInPool) {
		hatcheries.add(unitInPool);
	}
	
	private void removeHatchery(UnitInPool unitInPool) {
		hatcheries.remove(unitInPool);
	}
	
	public UnitInPool findBuilder() {
		if (currentBuilder != null && currentBuilder.isAlive()) {
			return currentBuilder;
		}
		Iterator<UnitInPool> workerIterator = workers.iterator();
		while (workerIterator.hasNext()) {
			UnitInPool worker = workerIterator.next();
			if (worker.isAlive() && UnitData.isWorker(worker.unit().getType())) {
				currentBuilder = worker;
				return currentBuilder;
			} else {
				workerIterator.remove();
			}
		}
		return null;
	}
	
	public UnitInPool findBuilderCloseTo(Point target) {
		if (currentBuilder != null && currentBuilder.isAlive()) {
			return currentBuilder;
		}
		Iterator<UnitInPool> workerIterator = workers.iterator();
		while (workerIterator.hasNext()) {
			UnitInPool worker = workerIterator.next();
			if (worker.isAlive() && 
					UnitData.isWorker(worker.unit().getType()) &&
					worker.unit().getPosition().distance(target) < BASE_RADIUS) {
				currentBuilder = worker;
				return currentBuilder;
			} 
		}
		//if we can't find one nearby then just find any worker
		return findBuilder();
	}
	
	public void builderMorphedIntoStructure() {
		if (currentBuilder != null) {
			workers.remove(currentBuilder);
			currentBuilder = null;
		}
	}
	
	private boolean needToScoutForProxies() {
		PlayerState enemy = currentGameState.findPlayerState(Alliance.ENEMY).orElse(new PlayerState());
		if (bot.observation().getGameLoop() < PROXY_WINDOW_END) {
			if (bot.observation().getGameLoop() > PROXY_WINDOW_START && 
					enemy.getProductionFacilityCount() == 0) {
				return true;
			}
			for (Threat threat : currentGameState.getThreats()) {
				if (threat.containsStructure()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void updateGasSaturation() {
		for (UnitInPool gas : gases) {
			int currentHarvesters = gas.unit().getAssignedHarvesters().orElse(0);
			int maxHarvesters = gas.unit().getIdealHarvesters().orElse(0);
			 //this prevents too many workers being put in gas in very low econ situations
			if (maxHarvesters > 0) {
				if (workers.size() <= 5) {
					maxHarvesters = 0;
				} else if (workers.size() <= 12) {
					maxHarvesters = 1;
				}
			}
			if (currentHarvesters < maxHarvesters) {
				addWorkerTo(gas);
			} else if (currentHarvesters > maxHarvesters) {
				removeWorkerFrom(gas);
			}
		}
	}
	
	private void addWorkerTo(UnitInPool gas) {
		UnitInPool worker = findWorkerTargetingMinerals();
		if (worker != null) {
			bot.actions().unitCommand(worker.unit(), Abilities.SMART, gas.unit(), false);
		}
	}
	
	private void removeWorkerFrom(UnitInPool gas) {
		UnitInPool worker = findWorkerGatheringFrom(gas);
		if (worker != null) {
			UnitInPool mineral = findClosestMineral(worker);
			if (mineral != null) {
				bot.actions().unitCommand(worker.unit(), Abilities.SMART, mineral.unit(), false);
			}
		}
	}
	
	private void updateMineralSaturation() {
		for (UnitInPool hatchery : hatcheries) {
			int currentSaturation = hatchery.unit().getAssignedHarvesters().orElse(0);
			int maxSaturation = hatchery.unit().getIdealHarvesters().orElse(0);
			if(currentSaturation > maxSaturation) {
				transferWorkerFrom(hatchery);
			}
		}
	}
			
	private void transferWorkerFrom(UnitInPool hatchery) {
		baseLocator.updateResourceNodeDisplayTypes();
		Base oversaturatedBase = baseLocator.findClosestBaseTo(hatchery.unit().getPosition());
		Base undersaturatedBase = findUndersaturatedBase();
		if (oversaturatedBase == null || undersaturatedBase == null) {
			return;
		}
		for(UnitInPool mineral : oversaturatedBase.getMinerals()) {
			UnitInPool worker = findWorkerGatheringFrom(mineral);
			if (worker != null) {
				bot.actions().unitCommand(
						worker.unit(), 
						Abilities.MOVE, 
						undersaturatedBase.getExpansionLocation().toPoint2d(), 
						false);
				return;
			}
		}
	}
	
	private Base findUndersaturatedBase() {
		Base undersaturatedBase = null;
		for (UnitInPool hatchery : hatcheries) {
			int currentSaturation = hatchery.unit().getAssignedHarvesters().orElse(0);
			int maxSaturation = hatchery.unit().getIdealHarvesters().orElse(0);
			if(currentSaturation < maxSaturation) {
				undersaturatedBase = baseLocator.findClosestBaseTo(hatchery.unit().getPosition());
				return undersaturatedBase;
			}
		}
		return undersaturatedBase;
	}
		
	private UnitInPool findClosestMineral(UnitInPool worker) {
		UnitInPool closestMineral = null;
		for (UnitInPool neutralUnit : bot.observation().getUnits(Alliance.NEUTRAL)) {
			if (UnitData.isMineral(neutralUnit.unit().getType())) {
				if (closestMineral == null || 
						worker.unit().getPosition().distance(neutralUnit.unit().getPosition()) < 
						worker.unit().getPosition().distance(closestMineral.unit().getPosition())) {
					closestMineral = neutralUnit;
				}
			}
		}
		return closestMineral;
	}
	
	private UnitInPool findWorkerTargetingMinerals() {
		for (UnitInPool worker : workers) {
			if (worker != currentBuilder) {
				for (UnitOrder order : worker.unit().getOrders()) {
					if (order.getAbility() == Abilities.HARVEST_GATHER) {
						Tag unitTag = getOrderTargetTag(order);
						if (unitTag != null) {
							UnitInPool target = findUnitInPoolByTag(
									bot.observation().getUnits(), 
									unitTag);
							if (target != null && UnitData.isMineral(target.unit().getType())) {
								return worker;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private Tag getOrderTargetTag(UnitOrder order) {
		Optional<Tag> opTag = order.getTargetedUnitTag();
		Tag unitTag = null;
		if (opTag.isPresent()) {
			unitTag = opTag.get();
		}
		return unitTag;
	}
	
	private UnitInPool findWorkerGatheringFrom(UnitInPool unitInPool) {
		for (UnitInPool worker : workers) {
			for (UnitOrder order : worker.unit().getOrders()) {
				if (order.getAbility() == Abilities.HARVEST_GATHER) {
					Tag unitTag = getOrderTargetTag(order);
					if (unitTag != null) {
						UnitInPool target = findUnitInPoolByTag(bot.observation().getUnits(), unitTag);
						if (target != null && target.unit().getPosition().distance(unitInPool.unit().getPosition()) < 2) {
							
							return worker;
						}
					}
				}
			}
		}
		return null;
	}
	
	private UnitInPool findUnitInPoolByTag(List<UnitInPool> units, Tag tag) {
		for (UnitInPool unitInPool : units) {
			if (unitInPool.unit().getTag().hashCode() == tag.hashCode()) {
				return unitInPool;
			}
		}
		return null;
	}
	
	private void assignScout() {
		UnitInPool worker = findWorkerTargetingMinerals();
		workerScouter.setScout(worker);
		workers.remove(worker);
	}
	
	private void assignProxyScout() {
		UnitInPool worker = findWorkerTargetingMinerals();
		workerScouter.setProxyScout(worker);
		workers.remove(worker);
	}
	
	private void clearDefenders() {
		for (UnitInPool worker : defenders) {
			returnToMiningAtClosestHatchery(worker);
		}
		workers.addAll(defenders);
		defenders.clear();
	}
	
	private void workerDefence() {
		if (needToPullWorkers()) {
			for (Threat threat : currentGameState.getThreats()) {
				UnitInPool hatchery = findClosestHatchery(threat.getCentreOfGroup());
				if (hatchery != null) {
					assignDefenders(threat, hatchery);
				}
			}
		}
		int totalThreat = calculateTotalThreatSize(currentGameState.getThreats());
		if (defenders.size() > totalThreat * 2) {
			unassignDefenders(defenders.size() - (totalThreat * 2));
		}
		defend();
	}
	
	private boolean needToPullWorkers() {
		List<Threat> threats = currentGameState.getThreats();
		PlayerState self = getPlayerState(Alliance.SELF);
		PlayerState enemy = getPlayerState(Alliance.ENEMY);
		if (self == null || enemy == null || 
				self.getArmySupply().isEmpty() || enemy.getArmySupply().isEmpty()) {
			return false;
		}
		for (Threat threat : threats) {
			if (threatIsCloseToBase(threat)) {			
				if (bot.observation().getGameLoop() < PROXY_WINDOW_END ||
						self.getArmySupply().get() < enemy.getArmySupply().get() * 2) {
					return true;
				} else if (currentGameState.getCurrentGamePhase() == GamePhase.EARLY) {
					return threatContainsWorkers(threat);
				}
			}
		}
		return false;
	}
	
	private PlayerState getPlayerState(Alliance alliance) {
		Optional<PlayerState> opState = currentGameState.findPlayerState(alliance);
		if (opState.isPresent()) {
			return opState.get();
		} else {
			return null;
		}
	}

	private void assignDefenders(Threat threat, UnitInPool hatchery) {
		float toAdd = threat.getUnits().size();
		List<UnitInPool> localWorkers = findLocalWorkers(hatchery.unit().getPosition());
		int i = 0;
		localWorkers = sortByHighestHealth(localWorkers);
		while (toAdd > 0 && i < localWorkers.size()) {
			UnitInPool worker = localWorkers.get(i);
			if (threat.getCentreOfGroup().distance(worker.unit().getPosition()) < 6 ||
					getHealth(worker) >= MINIMUM_DEFENDER_HP) {
				defenders.add(worker);
				workers.remove(worker);
				toAdd--;
			}
			i++;
		}
	}
	
	private void unassignDefenders(int toRemove) {
		List<UnitInPool> sortedDefenders = sortByLowestHealth(defenders);
		int i = 0;
		while(toRemove > 0 && i < defenders.size()) {
			UnitInPool worker = sortedDefenders.get(0);
			workers.add(worker);
			defenders.remove(worker);
			returnToMiningAtClosestHatchery(worker);
			i++;
			toRemove--;
		}
	}
	
	private void defend() {
		for (Iterator<UnitInPool> workerIterator = defenders.iterator(); workerIterator.hasNext();) {
			UnitInPool worker = workerIterator.next();
			Threat threat = getClosestThreat(worker);
			UnitInPool closestBase = findClosestHatchery(worker.unit().getPosition());
			if (threat != null && closestBase != null) {
				if (withinFightRadius(worker, closestBase) && getHealth(worker) > MINIMUM_DEFENDER_HP) {
					tryToAttack(worker, threat);
				} else {
					bot.actions().unitCommand(
							worker.unit(), 
							Abilities.MOVE, 
							closestBase.unit().getPosition().toPoint2d(), 
							false);
					workers.add(worker);
					workerIterator.remove();
				}
			}
		}
	}
	
	private boolean withinFightRadius(UnitInPool worker, UnitInPool closestBase) {
		if (worker.unit().getPosition().distance(closestBase.unit().getPosition()) < FIGHT_RADIUS) {
			return true;
		}
		return false;
	}
	
	private void tryToAttack(UnitInPool worker, Threat threat) {
		List<UnitOrder> workerOrders = worker.unit().getOrders();
		if (workerOrders.isEmpty() || workerOrders.get(0).getAbility() != Abilities.ATTACK) {
			Point target = threat.getCentreOfGroup();
			bot.actions().unitCommand(worker.unit(), Abilities.ATTACK, target.toPoint2d(), false);
		}
	}
		
	private boolean threatContainsWorkers(Threat threat) {
		for(UnitType unitType : threat.getUnitTypesPresent()) {
			if (UnitData.isWorker(unitType)) {
				return true;
			}
		}
		return false;
	}
 	
	private UnitInPool findClosestHatchery(Point point) {
		UnitInPool closestHatchery = null;
		for (UnitInPool hatchery : hatcheries) {
			if (closestHatchery == null || 
					point.distance(hatchery.unit().getPosition()) < 
					point.distance(closestHatchery.unit().getPosition()) &&
					hatchery.unit().getBuildProgress() == 1.0) {
				closestHatchery = hatchery;
			}
		}
		return closestHatchery;
	}
	
	private List<UnitInPool> findLocalWorkers(Point position) {
		List<UnitInPool> localWorkers = new ArrayList<UnitInPool>();
		for(UnitInPool worker : workers) {
			Point workerPosition = worker.unit().getPosition();
			if (workerPosition.distance(position) < BASE_RADIUS) {
				localWorkers.add(worker);
			}
		}
		return localWorkers;
	}
	
	private Threat getClosestThreat(UnitInPool unitInPool) {
		Threat closestThreat = null;
		Point unitPosition = unitInPool.unit().getPosition();
		for(Threat threat : currentGameState.getThreats()) {
			if (closestThreat == null || 
					unitPosition.distance(threat.getCentreOfGroup()) < 
					unitPosition.distance(closestThreat.getCentreOfGroup())) {
				closestThreat = threat;
			}
		}
		return closestThreat;
	}
	
	private boolean threatIsCloseToBase(Threat threat) {
		for (UnitInPool hatchery : hatcheries) {
			if (hatchery.unit().getPosition().distance(threat.getCentreOfGroup()) < FIGHT_RADIUS) {
				return true;
			}
		}
		return false;
	}
	
	private float getHealth(UnitInPool unitInPool) {
		return unitInPool.unit().getHealth().orElse((float) 0);
	}
	
	private int calculateTotalThreatSize(List<Threat> threats) {
		int total = 0;
		for (Threat threat : threats) {
			total += threat.getUnits().size();
		}
		return total;
	}
	
	private List<UnitInPool> sortByLowestHealth(Collection<UnitInPool> units) {
		List<UnitInPool> unsorted = new ArrayList<UnitInPool>(units);
		List<UnitInPool> sorted = new ArrayList<UnitInPool>();
		for (int i = 0; i < units.size(); i++) {
			UnitInPool lowestHealth = findLowestHealth(unsorted);
			sorted.add(lowestHealth);
			unsorted.remove(lowestHealth);
		}
		return sorted;
	}
	
	private List<UnitInPool> sortByHighestHealth(Collection<UnitInPool> units) {
		List<UnitInPool> unsorted = new ArrayList<UnitInPool>(units);
		List<UnitInPool> sorted = new ArrayList<UnitInPool>();
		for (int i = 0; i < units.size(); i++) {
			UnitInPool highestHealth = findHighestHealth(unsorted);
			sorted.add(highestHealth);
			unsorted.remove(highestHealth);
		}
		return sorted;
	}
	
	private UnitInPool findLowestHealth(List<UnitInPool> units) {
		UnitInPool lowestHealth = null;
		for (UnitInPool unitInPool : units) {
			if (lowestHealth == null ||
					getHealth(unitInPool) < getHealth(lowestHealth)) {
				lowestHealth = unitInPool;
			}
		}
		return lowestHealth;
	}
	
	private UnitInPool findHighestHealth(List<UnitInPool> units) {
		UnitInPool highestHealth = null;
		for (UnitInPool unitInPool : units) {
			if (highestHealth == null ||
					getHealth(unitInPool) > getHealth(highestHealth)) {
				highestHealth = unitInPool;
			}
		}
		return highestHealth;
	}
	
	private void returnToMiningAtClosestHatchery(UnitInPool worker) {
		UnitInPool hatchery = findClosestHatchery(worker.unit().getPosition());
		if (hatchery != null) {
			bot.actions().unitCommand(
					worker.unit(), 
					Abilities.MOVE, 
					hatchery.unit().getPosition().toPoint2d(), 
					false);
		} else {
			returnToMining(worker);
		}
	}
}
