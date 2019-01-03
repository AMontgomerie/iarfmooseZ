package iarfmoose;

import java.util.ArrayList;
import java.util.List;

public class UnitComposition {
	List<UnitCompositionComponent> composition;
	
	public UnitComposition(List<UnitCompositionComponent> composition) {
		this.composition = composition;
		checkValues();
	}
	
	public UnitComposition() {
		composition = new ArrayList<UnitCompositionComponent>();
	}
	
	public List<UnitCompositionComponent> getComposition() {
		return composition;
	}
	
	public void addComponent(UnitCompositionComponent newComponent) {
		composition.add(newComponent);
	}
	
	public void validate() {
		if (composition.size() > 0) {
			checkValues();
		}
	}
	
	private void checkValues() {
		int total = 0;
		for (UnitCompositionComponent component : composition) {
			total += component.getPercentage();
		}
		if (total > 100) {
			reduceValues(total);
		} else if (total < 100) {
			increaseValues(total);
		}
	}
	
	private void reduceValues(int total) {
		int extra = (total - 100) / composition.size();
		List<UnitCompositionComponent> updatedComposition = new ArrayList<UnitCompositionComponent>();
		for (UnitCompositionComponent component : composition) {
			updatedComposition.add(new UnitCompositionComponent(component.getUnitType(), component.getPercentage() - extra));
		}	
	}
	
	private void increaseValues(int total) {
		int extra = 100 - total/ composition.size();
		List<UnitCompositionComponent> updatedComposition = new ArrayList<UnitCompositionComponent>();
		for (UnitCompositionComponent component : composition) {
			updatedComposition.add(new UnitCompositionComponent(component.getUnitType(), component.getPercentage() + extra));
		}	
	}
}
