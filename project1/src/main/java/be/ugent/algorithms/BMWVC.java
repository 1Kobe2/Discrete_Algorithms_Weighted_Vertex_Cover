package be.ugent.algorithms;

import be.ugent.graphs.BasicGraph;
import be.ugent.benchmark.IntermediateSolutionReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.BitSet;

public class BMWVC implements WeightedVertexCoverAlgorithm {

    private static final Logger logger = LogManager.getLogger(BMWVC.class);

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter) {
        BitSet vertexCover = new BitSet(graph.getNumVertices());
        vertexCover.set(0, graph.getNumVertices()/2);
        return vertexCover;
    }
}
