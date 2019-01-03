package iarfmoose;

import java.util.ArrayDeque;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.debug.Color;

public class ProductionQueue {
	
	S2Agent bot;
	ArrayDeque<ProductionItem> queue;
	
	ProductionQueue(S2Agent bot) {
		this.bot = bot;
		queue = new ArrayDeque<ProductionItem>();
	}
	
	public ProductionItem getNextItem() {
		if (queue.isEmpty()) {
			return new ProductionItem(Abilities.TRAIN_ROACH);
		} else {
			return queue.getFirst();
		}
	}
	
	public void addItemHighPriority(ProductionItem item) {
		queue.addFirst(item);
		printNextItem();
	}
	
	public void addItemLowPriority(ProductionItem item) {
		queue.addLast(item);
		printNextItem();
	}
	
	public void removeItem() {
		if (!queue.isEmpty()) {
			//System.out.println("removing " + queue.getFirst().getAbility().toString());
			queue.remove();
			if (!queue.isEmpty()) {
				//System.out.println("next item is " + queue.getFirst().getAbility().toString());
			}
		}
		printNextItem();
	}
	
	public void clear() {
		queue.clear();
		printNextItem();
	}
	
	public boolean contains(Abilities ability) {
		for (ProductionItem item : queue) {
			if (item.getAbility() == ability) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty() {
		if (queue.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public void update(BuildOrder buildOrder) {
		queue.clear();
		//System.out.println("new queue: ");
		for (ProductionItem item : buildOrder.getBuildOrder()) {
			queue.add(item);
			//System.out.println(item.getAbility().toString());
		}
		printNextItem();
	}
		
	private void printNextItem() {
		if (!queue.isEmpty()) {
		ProductionItem nextItem = queue.getFirst();
		bot.debug().debugTextOut(nextItem.getAbility().toString(), Color.WHITE);
		bot.debug().sendDebug();
		}
	}
}
