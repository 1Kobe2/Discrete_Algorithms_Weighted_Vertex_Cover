package be.ugent.benchmark;


import java.util.BitSet;

public interface IntermediateSolutionReporter {

	/**
	 * A callback method that can be called by the algorithm to report an intermediate solution.
	 * This intermediate solution will be used for the current optimal
	 *
	 * @param currentOptimalSolution the current optimal solution
	 */
	void solutionCallback(BitSet currentOptimalSolution);
}
