package be.ugent.algorithms;


import be.ugent.graphs.BasicGraph;
import be.ugent.benchmark.IntermediateSolutionReporter;

import java.util.BitSet;

/**
 * This interface defines the contract for classes that implement a maximum clique finding algorithm.
 * A clique in a graph is a subset of vertices such that every two vertices in the subset are adjacent.
 * A maximum clique is a clique that includes the largest possible number of vertices.
 */
public interface WeightedVertexCoverAlgorithm {

	/**
	 * Finds and returns the minimum vertex cover of the given graph.
	 *
	 * @param graph The graph in which to find the minimum vertex cover.
	 * @param intermediateSolutionReporter A callback that can be used to report intermediate solutions.
	 * @return A BitSet representing the vertices in the minimum vertex cover.
	 */
	public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter);
}
