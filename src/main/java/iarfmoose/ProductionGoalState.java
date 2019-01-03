package iarfmoose;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;

public class ProductionGoalState implements State {
	
	int workerCount;
	int baseCount;
	int gasCount;
	private List<UnitType> structures;
	private List<Upgrade> upgrades;
	private UnitComposition unitComp;
	
	public ProductionGoalState() {
		this.workerCount = 0;
		this.baseCount = 0;
		this.gasCount = 0;
		this.structures = new ArrayList<UnitType>();
		this.upgrades = new ArrayList<Upgrade>();
		this.unitComp = new UnitComposition();
	}
	
	public ProductionGoalState(
			int workerCount,
			int baseCount,
			int gasCount,
			List<UnitType> structures,
			List<Upgrade> upgrades,
			UnitComposition unitComp
			) {
		this.workerCount = workerCount;
		this.baseCount = baseCount;
		this.gasCount = gasCount;
		this.structures = structures;
		this.upgrades = upgrades;
		this.unitComp = unitComp;
	}
		
	@Override
	public Optional<Integer> getWorkerCount() {
		return Optional.of(workerCount);
	}

	@Override
	public Optional<Integer> getBaseCount() {
		return Optional.of(baseCount);
	}

	@Override
	public Optional<Float> getArmySupply() {
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getGasCount() {
		return Optional.of(gasCount);
	}
		
	public List<Upgrade> getUpgrades() {
		return upgrades;
	}
		
	public UnitComposition getUnitComposition() {
		return unitComp;
	}

	public List<UnitType> getStructureTypes() {
		return structures;
	}
}
