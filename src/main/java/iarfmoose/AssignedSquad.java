package iarfmoose;

import java.util.List;

public class AssignedSquad {
	private Squad squad;
	private Threat threat;
	
	public AssignedSquad(Squad squad, Threat threat) {
		this.squad = squad;
		this.threat = threat;
	}
	
	public Squad getSquad() {
		return squad;
	}
	
	public Threat getThreat() {
		return threat;
	}
	
	public void addUnits(List<ActiveUnit> units) {
		squad.addUnits(units);
	}
	
	public void addUnits(Squad squad) {
		squad.addUnits(squad);
	}
	
	public void attackThreat() {
		squad.attack(threat.getCentreOfGroup().toPoint2d());
	}
	
}
