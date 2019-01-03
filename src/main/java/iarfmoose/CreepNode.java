package iarfmoose;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;

public class CreepNode {
	UnitInPool node;
	boolean active;
	
	public CreepNode (UnitInPool node) {
		this.node = node;
		if (UnitData.isCreepTumor(node.unit().getType())) {
			active = true;
		} else {
			active = false;
		}
	}
	
	public UnitInPool getNode() {
		return node;
	}
	
	public boolean isActive() {
		return active;
	}
	
	/*
	public void deactivate() {
		active = false;
	}
	*/
}
