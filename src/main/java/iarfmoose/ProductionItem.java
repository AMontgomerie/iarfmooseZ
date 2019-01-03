package iarfmoose;

import com.github.ocraft.s2client.protocol.data.Abilities;

public class ProductionItem {
	
	private Abilities ability;
	
	ProductionItem(Abilities ability) {
		this.ability = ability;
	}
	
	public Abilities getAbility() {
		return ability;
	}
}
