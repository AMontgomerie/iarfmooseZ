package iarfmoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class DefenceManager extends ResponsiveManager {

	private static final float EARLY_DEFENCE_RADIUS = 12;
	
	private PlayerState self;
	private ArmyUnitTypeHandler armyUnitTypeHandler;
	private List<ActiveUnit> unassignedUnitPool;
	private List<Squad> unassignedSquads;
	private List<Squad> squads;
	private List<AssignedSquad> assignedSquads;
	
	public DefenceManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		self = new PlayerState();
		armyUnitTypeHandler = new ArmyUnitTypeHandler(bot);
		unassignedUnitPool = new ArrayList<ActiveUnit>();
		squads = new ArrayList<Squad>();
		assignedSquads = new ArrayList<AssignedSquad>();
	}
	
	@Override
	public void onStep(GameState currentState) {
		self = currentState.findPlayerState(Alliance.SELF).orElse(new PlayerState());
		removeEmptySquads();
		checkForMorphedUnits();
	}

	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		ArmyUnit armyUnit = armyUnitTypeHandler.assignBehaviourType(unitInPool);
		unassignedUnitPool.add(armyUnit);
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		removeUnassignedUnit(unitInPool);
		for (Squad squad : squads) {
			squad.removeMember(unitInPool);
		}
	}
	
	private void checkForMorphedUnits() {
		List<ActiveUnit> toAdd = new ArrayList<ActiveUnit>();
		for (Iterator<ActiveUnit> armyIterator = unassignedUnitPool.iterator(); armyIterator.hasNext();) {
			ArmyUnit armyUnit = (ArmyUnit) armyIterator.next();
			if (!armyUnitTypeHandler.isCorrectType(armyUnit)) {
				try {
					UnitInPool unitInPool = armyUnit.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
					armyIterator.remove();
					toAdd.add(armyUnitTypeHandler.assignBehaviourType(unitInPool));
				} catch (UnitNotFoundException e) {
					armyIterator.remove();
				}
			}
		}
		unassignedUnitPool.addAll(toAdd);
	}
	
	public void removeUnassignedUnit(UnitInPool unitInPool) {
		for (Iterator<ActiveUnit> memberIterator = unassignedUnitPool.iterator();
				memberIterator.hasNext();) {
			ActiveUnit member = memberIterator.next();
			Optional<UnitInPool> memberUnitInPool = member.getUnitInPool();
			if (memberUnitInPool.isPresent() && memberUnitInPool.get() == unitInPool) {
				memberIterator.remove();
			}
		}
	}
	
	public void handleThreats(List<Threat> threats) {
		if (threats.isEmpty()) {
			for (AssignedSquad squad : assignedSquads) {
				squad.getSquad().returnHome();
			}
			assignedSquads.clear();
			squads.clear();
			return;
		}
		assignedSquads = assignSquads(threats);
		balanceSquadSizes();
		attackTargets();
	}
	
	private List<AssignedSquad> assignSquads(List<Threat> threats) {
		List<AssignedSquad> assignedSquads = new ArrayList<AssignedSquad>();
		unassignedSquads = squads;
		for (Threat threat : threats) {
			if (threat.getUnits().size() > 0) {
				Squad squad = findClosestSquad(threat);
				if (squad == null) {
					squad = new Squad(bot);
					squads.add(squad);
				}
				AssignedSquad assignedSquad = new AssignedSquad(squad, threat);
				assignedSquads.add(assignedSquad);
				unassignedSquads.remove(squad);
			}
		}
		return assignedSquads;
	}
	
	private Squad findClosestSquad(Threat threat) {
		Point threatLocation = threat.getCentreOfGroup();
		Squad closestSquad = null;
		for (Squad squad : squads) {
			if (closestSquad == null || 
					squad.getCentreOfGroup().distance(threatLocation) < 
					closestSquad.getCentreOfGroup().distance(threatLocation)) {
				closestSquad = squad;
			}
		}
		return closestSquad;
	}
		
	private void balanceSquadSizes() {
		if (assignedSquads.isEmpty()) {
			return;
		}
		dissolveUnassignedSquads();
		splitOversizedSquads();
		addUnassignedUnitsToUndersizedSquads();
	}
		
	private void dissolveUnassignedSquads() {
		for (Squad unassignedSquad : unassignedSquads) {
			unassignedUnitPool.addAll(unassignedSquad.getMembers());
		}
		unassignedSquads.clear();
	}
	
	private void splitOversizedSquads() {
		//separate the squads which have too many units
		List<AssignedSquad> oversizedSquads = new ArrayList<AssignedSquad>();
		for (AssignedSquad assignedSquad : assignedSquads) {
			if (assignedSquad.getSquad().getTotalSupply() > assignedSquad.getThreat().getTotalSupply() * 2) {
				oversizedSquads.add(assignedSquad);
			}
		}
		assignedSquads.removeAll(oversizedSquads);
		//cut the oversized squads in half and put the extra units in the pool
		for (AssignedSquad oversizedSquad : oversizedSquads) {
			List<ActiveUnit> squadUnits = oversizedSquad.getSquad().getMembers();
			int limit = squadUnits.size() / 2;
			int i = 0;
			for (i = 0; i < limit; i++) {
				unassignedUnitPool.add(squadUnits.get(i));
				squadUnits.remove(i);
			}
			//add the resized squad back into the list
			AssignedSquad assignedSquad = new AssignedSquad(new Squad(bot, squadUnits), oversizedSquad.getThreat());
			assignedSquads.add(assignedSquad);
		}
	}
	
	private void addUnassignedUnitsToUndersizedSquads() {
		//separate the squads that are too small
		List<AssignedSquad> undersizedSquads = new ArrayList<AssignedSquad>();
		for (AssignedSquad assignedSquad : assignedSquads) {
			if (assignedSquad.getSquad().getTotalSupply() < assignedSquad.getThreat().getTotalSupply()) {
				undersizedSquads.add(assignedSquad);
			}
		}
		assignedSquads.removeAll(undersizedSquads);
		//add units from the unassigned pool to each squad
		for (AssignedSquad undersizedSquad : undersizedSquads) {
			List<ActiveUnit> squadUnits = undersizedSquad.getSquad().getMembers();
			float difference = undersizedSquad.getThreat().getTotalSupply() - 
					undersizedSquad.getSquad().getTotalSupply();
			float supplyAdded = 0;
			Iterator<ActiveUnit> unitIterator = unassignedUnitPool.iterator();
			while (unitIterator.hasNext() && supplyAdded < difference) {
				ActiveUnit unassignedUnit = unitIterator.next();
				try {
					UnitInPool unitInPool = unassignedUnit.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
					squadUnits.add(unassignedUnit);
					supplyAdded += bot.observation()
							.getUnitTypeData(false)
							.get(unitInPool.unit().getType())
							.getFoodRequired().orElse((float) 0);
				} catch (UnitNotFoundException e) {
					unitIterator.remove();
				}
			}
			//add the resized squad back into the list
			AssignedSquad assignedSquad = new AssignedSquad(new Squad(bot, squadUnits), undersizedSquad.getThreat());
			assignedSquads.add(assignedSquad);
		}
		if (unassignedUnitPool.size() > 0) {
			AssignedSquad weakestSquad = getWeakestSquad();
			assignedSquads.remove(weakestSquad);
			weakestSquad.addUnits(unassignedUnitPool);
			assignedSquads.add(weakestSquad);
		}
	}
	
	private AssignedSquad getWeakestSquad() {
		AssignedSquad weakestSquad = null;
		for (AssignedSquad assignedSquad : assignedSquads) {
			if (weakestSquad == null) {
				weakestSquad = assignedSquad;
			} else {
				float difference = assignedSquad.getThreat().getTotalSupply() - assignedSquad.getSquad().getTotalSupply();
				float weakestDifference = weakestSquad.getThreat().getTotalSupply() - weakestSquad.getSquad().getTotalSupply();
				if (difference > weakestDifference) {
					weakestSquad = assignedSquad;
				}
			}
		}
		return weakestSquad;
	}
		
	private void attackTargets() {
		for (AssignedSquad assignedSquad : assignedSquads) {
			float threatSize = assignedSquad.getThreat().getTotalSupply();
			float squadSize = assignedSquad.getSquad().getTotalSupply();
			Point home = assignedSquad.getSquad().getHomeLocation();
			UnitInPool closestEnemy = getClosestUnit(home, assignedSquad.getThreat().getUnits());
			if (self.getBaseCount().orElse(0) > 2) {
				assignedSquad.attackThreat();
			} else if (squadSize > threatSize || 
					closestEnemy.unit().getPosition().distance(home) < EARLY_DEFENCE_RADIUS) {
				assignedSquad.attackThreat();
			} else {
				assignedSquad.getSquad().returnHome();
			}
		}
	}
	
	private void removeEmptySquads() {
		for (Iterator<Squad> squadIterator = squads.iterator(); squadIterator.hasNext();) {
			Squad squad = squadIterator.next();
			if (squad.getTotalSupply() == 0) {
				squadIterator.remove();
			}
		}
	}
	
	private UnitInPool getClosestUnit(Point point, List<UnitInPool> group) {
		UnitInPool closestUnit = null;
		for (UnitInPool unitInPool : group) {
			Point unitPosition = unitInPool.unit().getPosition();
			if (closestUnit == null || 
					unitPosition.distance(point) <
					closestUnit.unit().getPosition().distance(point)) {
				closestUnit = unitInPool;
			}		
		}
		return closestUnit;
	}
}
