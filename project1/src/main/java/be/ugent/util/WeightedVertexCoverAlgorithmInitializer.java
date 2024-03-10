package be.ugent.util;

import be.ugent.algorithms.WeightedVertexCoverAlgorithm;

public interface WeightedVertexCoverAlgorithmInitializer {
	/**
	 * Initializes the algorithm with the optimal solution size and the maximum number of iterations.
	 *
	 * @param optimalSolutionSize the optimal size of the vertex cover
	 * @param maxIterations the maximum number of iterations
	 * @return the initialized algorithm
	 */
	WeightedVertexCoverAlgorithm initialize(int optimalSolutionSize, int maxIterations);}
