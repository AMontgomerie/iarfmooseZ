package iarfmoose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class PlayerState implements State {

	PlayerInfo playerInfo;
	private Alliance alliance;
	private float armySupply;
	private int workerCount;
	private Set<UnitInPool> structures;
	private Set<Upgrade> completedUpgrades;
	
	public PlayerState() {
		playerInfo = null;
		alliance = Alliance.NEUTRAL;
		armySupply = 0;
		workerCount = 0;
		structures = new HashSet<UnitInPool>();
		completedUpgrades = new HashSet<Upgrade>();
		
	}
	
	public PlayerState(
			PlayerInfo playerInfo, 
			Alliance alliance, 
			float armySupply, 
			int workerCount, 
			Set<UnitInPool> structures, 
			Set<Upgrade> upgrades) {
		this.alliance = alliance;
		this.playerInfo = playerInfo;
		this.armySupply = armySupply;
		this.workerCount = workerCount;
		this.structures = structures;
		this.completedUpgrades = upgrades;
	}
	
	@Override
	public Optional<Integer> getWorkerCount() {
		return Optional.of(workerCount);
	}

	@Override
	public Optional<Integer> getBaseCount() {
		java.util.List<UnitType> townhallTypes = UnitData.getTownHallTypeFor(getRace());
		int count = 0;
		for (UnitType townhallType : townhallTypes) {
			count += getCountForStructureType(townhallType);
		}
		return Optional.of(count);
	}

	@Override
	public Optional<Float> getArmySupply() {
		return Optional.of(armySupply);
	}

	@Override
	public Optional<Integer> getGasCount() {
		UnitType gasType = UnitData.getGasTypeFor(getRace());
		return Optional.of(getCountForStructureType(gasType));
	}
	
	public Race getRace() {
		if (playerInfo == null) {
			return Race.NO_RACE;
		} else {
			return playerInfo.getActualRace().orElse(playerInfo.getRequestedRace());
		}
	}
	
	public int getPlayerID() {
		return playerInfo.getPlayerId();
	}
	
	public Alliance getAlliance() {
		return alliance;
	}
	
	public List<UnitInPool> getBases() {
		java.util.List<UnitType> townhallTypes = UnitData.getTownHallTypeFor(getRace());
		List<UnitInPool> bases = new ArrayList<UnitInPool>();
		for (UnitType townhallType : townhallTypes) {
			bases.addAll(getStructuresOfType(townhallType));
		}
		return bases;
	}
	
	public Set<UnitInPool> getStructures() {
		return structures;
	}
	
	public Set<UnitType> getStructureTypes() {
		Set<UnitType> structureTypes = new HashSet<UnitType>();
		for (UnitInPool structure: structures) {
			structureTypes.add(structure.unit().getType());
		}
		return structureTypes;
	}
	
	public int getCountForStructureType(UnitType structureType) {
		int count = 0;
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == structureType) {
				count++;
			}
		}
		return count;
	}
	
	public List<UnitInPool> getStructuresOfType(UnitType structureType) {
		List<UnitInPool> outputStructures = new ArrayList<UnitInPool>();
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == structureType) {
				outputStructures.add(structure);
			}
		}
		return outputStructures;
	}
	
	public Set<Upgrade> getCompletedUpgrades() {
		return completedUpgrades;
	}
	
	public int getProductionFacilityCount() {
		int currentProductionFacilities = 0;
		Set<UnitType> productionFacilities = UnitData.getProductionFacilitiesForRace(getRace());
		for (UnitType structureType : productionFacilities) {
			currentProductionFacilities += getCountForStructureType(structureType);
		}
		return currentProductionFacilities;
	}
	
	public boolean completedStructureExists(UnitType structureType) {
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == structureType && 
					structure.unit().getBuildProgress() == 1.0) {
				return true;
			}
		}
		return false;
	}
}
