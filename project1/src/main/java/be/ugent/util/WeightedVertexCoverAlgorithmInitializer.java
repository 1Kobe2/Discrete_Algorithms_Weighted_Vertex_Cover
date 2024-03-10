package be.ugent.util;

import be.ugent.WeightedVertexCoverAlgorithm;

public interface WeightedVertexCoverAlgorithmInitializer {
	WeightedVertexCoverAlgorithm initialize(int maxCliqueSize);
}
