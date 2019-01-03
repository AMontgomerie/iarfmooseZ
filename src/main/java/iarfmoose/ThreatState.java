package iarfmoose;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.game.Race;

public class ThreatState implements State{
		
	private String name;
	private Integer baseCount;
	private Integer gasCount;
	private Integer workerCount;
	private Float armySupply;
	private Integer productionFacilityCount;
	private Set<Race> enemyRaces;
	private Set<UnitType> keyUnits;
	private TimeWindow timeWindow;
	private ThreatResponse response;
	
	public ThreatState(String name, TimeWindow timeWindow, ThreatResponse response) {
		this.name = name;
		this.baseCount = null;
		this.gasCount  = null;
		this.workerCount = null;
		this.armySupply = null;
		this.productionFacilityCount  = null;
		this.enemyRaces = new HashSet<Race>();
		this.keyUnits = new HashSet<UnitType>();
		this.timeWindow = timeWindow;
		this.response = response;
	}
	
	//GET VALUES:
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public Optional<Integer> getBaseCount() {
		return Optional.ofNullable(baseCount);
	}
	
	@Override
	public Optional<Integer> getGasCount() {
		return Optional.ofNullable(gasCount);
	}
	
	@Override
	public Optional<Integer> getWorkerCount() {
		return Optional.ofNullable(workerCount);
	}

	@Override
	public Optional<Float> getArmySupply() {
		return Optional.ofNullable(armySupply);
	}
	
	public Optional<Integer> getProductionFacilityCount() {
		return Optional.ofNullable(productionFacilityCount);
	}
		
	public Set<Race> getRaces() {
		return enemyRaces;
	}
	
	public Set<UnitType> getKeyUnits() {
		return keyUnits;
	}
	
	public TimeWindow getTimeWindow() {
		return timeWindow;
	}
			
	public ThreatResponse getResponse() {
		return response;
	}
		
	//SET VALUES:
		
	public void addRace(Race enemyRace) {
		this.enemyRaces.add(enemyRace);
	}
	
	public void addKeyUnit(UnitType unitType) {
		this.keyUnits.add(unitType);
	}
		
	public void setProductionFacilityCount(int count) {
		this.productionFacilityCount = count;
	}
	
	public void setBaseCount(int count) {
		this.baseCount = count;
	}
	
	public void setGasCount(int count) {
		this.gasCount = count;
	}
	
	public void respond() {
		response.respond();
	}
}
