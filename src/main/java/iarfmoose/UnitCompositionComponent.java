package iarfmoose;

import com.github.ocraft.s2client.protocol.data.UnitType;

public class UnitCompositionComponent {
	private UnitType unitType;
	private int percentage;
	
	public UnitCompositionComponent(UnitType unitType, int percentage) {
		this.unitType = unitType;
		this.percentage = percentage;
	}
	
	public UnitType getUnitType() {
		return unitType;
	}
	
	public int getPercentage() {
		return percentage;
	}
}
