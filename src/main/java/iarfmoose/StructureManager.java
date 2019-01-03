package iarfmoose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class StructureManager extends DataManager {

	Set<UnitInPool> structures;
	UnitInPool targetProductionFacility;
	
	public StructureManager(S2Agent bot) {
		super(bot);
		structures = new HashSet<UnitInPool>();
		targetProductionFacility = null;
	}
	
	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		Unit unit = unitInPool.unit();
		if (UnitData.isStructure(unit.getType())) {
			structures.add(unitInPool);
		}	
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		structures.remove(unitInPool);
	}
	
	public List<UnitInPool> getBases() {
		List<UnitInPool> bases = new ArrayList<UnitInPool>();
		for (UnitInPool structure : structures) {
			if (UnitData.isTownHall(structure.unit().getType())) {
				bases.add(structure);
			}
		}
		return bases;
	}
		
	public Set<UnitInPool> getIdleStructures() {
		Set<UnitInPool> idleStructures = new HashSet<UnitInPool>();
		for (UnitInPool structure : structures) {
			if (structure.unit().getOrders().isEmpty() && structure.unit().getBuildProgress() == 1.0) {
				idleStructures.add(structure);
			}
		}
		return idleStructures;
	}

	public Optional<UnitInPool> findStructure(UnitType unitType) {
		UnitType morphType = UnitData.getMorphsIntoType(unitType); //in case the structure has morphed e.g. looking for lair when we have hive
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == unitType) {
				return Optional.of(structure);
			} else if (structure.unit().getType() == morphType) {
				return Optional.of(structure);
			}
		}
		return Optional.empty();
	}
	
	public Optional<UnitInPool> findCompletedStructure(UnitType unitType) {
		UnitType morphType = UnitData.getMorphsIntoType(unitType); //in case the structure has morphed e.g. looking for lair when we have hive
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == unitType &&
				structure.unit().getBuildProgress() == 1.0) {
				return Optional.of(structure);
			} else if (structure.unit().getType() == morphType &&
					structure.unit().getBuildProgress() == 1.0) {
				return Optional.of(structure);
			}
		}
		return Optional.empty();
	}

	public Optional<UnitInPool> findIdleStructure(UnitType unitType) {
		UnitType morphType = UnitData.getMorphsIntoType(unitType); //in case the structure has morphed e.g. looking for lair when we have hive
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == unitType &&
				structure.unit().getOrders().size() == 0 && 
				structure.unit().getBuildProgress() == 1.0) {
				return Optional.of(structure);
			} else if (structure.unit().getType() == morphType &&
					structure.unit().getOrders().size() == 0 && 
					structure.unit().getBuildProgress() == 1.0) {
				return Optional.of(structure);
			}
		}
		return Optional.empty();
	}
		
	public Set<UnitInPool> getAllStructures() {
		return structures;
	}
	
	public Set<UnitInPool> getAllStructuresOfType(UnitType unitType) {
		Set<UnitInPool> matchingStructures = new HashSet<UnitInPool>();
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == unitType) {
				matchingStructures.add(structure);
			}
		}
		return matchingStructures;
	}
	
	public UnitInPool getTargetProductionFacility() {
		return targetProductionFacility;
	}
	
	public void setTargetProductionFacility(UnitInPool structure) {
		targetProductionFacility = structure;
	}
	
	public void clearTargetProductionFacility() {
		targetProductionFacility = null;
	}
	
	public boolean currentlyExpanding() {
		for (UnitInPool structure : structures) {
			if(UnitData.isTownHall(structure.unit().getType()) &&
					structure.unit().getBuildProgress() < 1.0) {
				return true;
			}
		}
		return false;
	}
	
	public void cancelExpansion() {
		UnitInPool toCancel = findExpansionInProduction();
		if (toCancel != null) {
			bot.actions().unitCommand(toCancel.unit(), Abilities.CANCEL, false);
		}
	}
	
	public UnitInPool findExpansionInProduction() {
		for (UnitInPool structure : structures) {
			if (UnitData.isTownHall(structure.unit().getType()) &&
					structure.unit().getBuildProgress() < 1.0) {
				return structure;
			}
		}
		return null;
		
	}
	
	public List<UnitInPool> getTownHalls() {
		List<UnitInPool> townhalls = new ArrayList<UnitInPool>();
		for (UnitInPool structure : structures) {
			if (UnitData.isTownHall(structure.unit().getType()) &&
					structure.unit().getBuildProgress() == 1.0) {
				townhalls.add(structure);
			}
		}
		return townhalls;
	}
}
