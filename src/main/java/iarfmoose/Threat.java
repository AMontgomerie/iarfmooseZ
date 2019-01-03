package iarfmoose;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;

public class Threat extends UnitGroup{
	
	public Threat(S2Agent bot) {
		this.bot = bot;
		this.members = new ArrayList<ActiveUnit>();
	}
	
	public Threat(S2Agent bot, List<UnitInPool> units) {
		this.bot = bot;
		this.members = new ArrayList<ActiveUnit>();
		for (UnitInPool unitInPool: units) {
			this.members.add(new ActiveUnit(bot, unitInPool));
		}
	}
		
	public List<UnitInPool> getUnits() {
		return makeListOfUnitInPool();
	}
	
	public List<UnitType> getUnitTypesPresent() {
		return makeListOfTypes();
	}
	
	public boolean contains(UnitType unitType) {
		for (UnitType presentType : makeListOfTypes()) {
			if (unitType == presentType) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsStructure() {
		for (UnitType unitType : makeListOfTypes()) {
			if (UnitData.isStructure(unitType)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsFlying() {
		for (ActiveUnit member : members) {
			if (member.getUnitInPool().isPresent() &&
					member.getUnitInPool().get().unit().getFlying().orElse(false)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsCloak() {
		for (UnitType unitType : makeListOfTypes()) {
			if (UnitData.canCloak(unitType)) {
				return true;
			}
		}
		return false;
	}
	
	private List<UnitInPool> makeListOfUnitInPool() {
		List<UnitInPool> output = new ArrayList<UnitInPool>();
		for (ActiveUnit activeUnit : members) {
			Optional<UnitInPool> unitInPool = activeUnit.getUnitInPool();
			if (unitInPool.isPresent()) {
				output.add(unitInPool.get());
			}
		}
		return output;
	}

	private List<UnitType> makeListOfTypes() {
		List<UnitType> list = new ArrayList<UnitType>();
		for (ActiveUnit activeUnit : members) {
			Optional<UnitInPool> unitInPool = activeUnit.getUnitInPool();
			if (unitInPool.isPresent()) {
				boolean alreadyAdded = false;
				for (UnitType unitType : list) {
					if (unitType == unitInPool.get().unit().getType()) {
						alreadyAdded = true;
					}
				}
				if (!alreadyAdded) {
					list.add(activeUnit.getLastKnownType());
				}
			}
		}
		return list;
	}
}
