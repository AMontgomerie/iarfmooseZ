package iarfmoose;

public class AssignedQueen {

	private Queen queen;
	private Hatchery hatchery;
	
	public AssignedQueen(Queen queen, Hatchery hatchery) {
		this.queen = queen;
		this.hatchery = hatchery;
	}
	
	public Queen getQueen() {
		return queen;
	}
	
	public Hatchery getHatchery() {
		return hatchery;
	}
	
	public void injectHatchery() throws UnitNotFoundException {
		queen.injectLarva(hatchery.getHatchery());
	}
	
	public boolean spawnLarvaStarted() {
		return hatchery.hasBeenInjected();
	}
}
