package iarfmoose;

import java.util.Optional;

public interface State {
	
	public Optional<Integer> getWorkerCount();
	public Optional<Integer> getBaseCount();
	public Optional<Float> getArmySupply();
	public Optional<Integer> getGasCount();
}
