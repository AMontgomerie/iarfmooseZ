package iarfmoose;

import java.util.ArrayList;
import java.util.List;

public class BuildOrder {
	List<ProductionItem> buildOrder;
	
	public BuildOrder() {
		buildOrder = new ArrayList<ProductionItem>();
	}
	
	public BuildOrder(List<ProductionItem> buildOrder) {
		this.buildOrder = buildOrder;
	}
	
	public List<ProductionItem> getBuildOrder() {
		return buildOrder;
	}
	
	public void addItem(ProductionItem item) {
		buildOrder.add(item);
	}
	
	public void addItemToFront(ProductionItem item) {
		buildOrder.add(0, item);
	}
}
