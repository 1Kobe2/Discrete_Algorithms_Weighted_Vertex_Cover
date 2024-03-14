package be.ugent.algorithms;

import be.ugent.benchmark.IntermediateSolutionReporter;
import be.ugent.graphs.BasicGraph;

import java.util.BitSet;

public class DLSWCC implements WeightedVertexCoverAlgorithm {
    private final int maxIterations;

    private BitSet wConfig;
    private int[][] edgeWeights;
    private int[] vertexScores;
    private int[] vertexAges;
    private BitSet minimumVertexCover;
    private BitSet currentCover;
    private int upperBound;
    private int iteration;
    private BitSet tabuList;

    public DLSWCC(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public DLSWCC() {
        this.maxIterations = 1000000;
    }

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter) {
        initializeWConfig(graph);
        initializeEdgeWeigths(graph);
        initializeVertexScores(graph);
        initializeVertexAges(graph);
        initialMinimumVertexCover(graph);
        initializeTabuList(graph);
        updateUpperBound(graph);
        iteration = 0;
        while (iteration < maxIterations) {
            while (graph.isVertexCover(currentCover)) {
                updateUpperBound(graph);
                minimumVertexCover = (BitSet) currentCover.clone();
                int id = nextVertex();
                updateScores(id, graph);
                currentCover.clear(id);
                wConfig.clear(id);
                updateWConfig(id, graph);
            }
            int id = nextVertexTabu();
            updateScores(id, graph);
            currentCover.clear(id);
            wConfig.clear(id);
            updateWConfig(id, graph);
            tabuList.clear();
            while (!graph.isVertexCover(currentCover)) {
                id = nextVertexWConfig();
                if (currentWeight(graph) + graph.weight(id) >= upperBound) {
                    break;
                }
                updateScores(id, graph);
                currentCover.set(id);
                updateWConfig(id, graph);
                updateEdgeWeights(graph);
                tabuList.set(id);
            }
            iteration++;
        }
        return minimumVertexCover;
    }

    private void initializeWConfig(BasicGraph graph) {
        wConfig = new BitSet(graph.getNumVertices());
        wConfig.set(0, graph.getNumVertices());
    }

    private void initializeEdgeWeigths(BasicGraph graph) {
        edgeWeights = new int[graph.getNumVertices()][graph.getNumVertices()];
        for (int i = 0; i < graph.getNumVertices(); i++) {
            for (int j = 0; j < graph.getNumVertices(); j++) {
                edgeWeights[i][j] = 1;
            }
        }
    }

    private void initializeVertexScores(BasicGraph graph) {
        vertexScores = new int[graph.getNumVertices()];
        for (int i = 0; i < graph.getNumVertices(); i++) {
            vertexScores[i] = graph.degree(i);
        }
    }

    private void initializeVertexAges(BasicGraph graph) {
        vertexAges = new int[graph.getNumVertices()];
    }

    private void initialMinimumVertexCover(BasicGraph graph) {
        minimumVertexCover = new BitSet(graph.getNumVertices());
        //todo
        currentCover = (BitSet) minimumVertexCover.clone();
    }

    private void initializeTabuList(BasicGraph graph) {
        tabuList = new BitSet(graph.getNumVertices());
    }

    private int currentWeight(BasicGraph graph) {
        int total_weight = 0;
        int id = currentCover.nextSetBit(0);
        while (id != -1) {
            total_weight += graph.weight(id);
            id = currentCover.nextSetBit(id + 1);
        }
        return total_weight;
    }

    private void updateUpperBound(BasicGraph graph) {
        upperBound = currentWeight(graph);
    }

    private int nextVertex() {
        int id = currentCover.nextSetBit(0);
        for (int i = 0; i < vertexScores.length; i++) {
            if (currentCover.get(i)) {
                if (vertexScores[i] > vertexScores[id]) {
                    id = i;
                } else if (vertexScores[i] == vertexScores[id]) {
                    if (vertexAges[i] < vertexAges[id]) {
                        id = i;
                    }
                }
            }
        }
        return id;
    }

    private int nextVertexTabu() {
        int id = currentCover.nextSetBit(0);
        for (int i = 0; i < vertexScores.length; i++) {
            if (currentCover.get(i) && !tabuList.get(i)) {
                if (vertexScores[i] > vertexScores[id]) {
                    id = i;
                } else if (vertexScores[i] == vertexScores[id]) {
                    if (vertexAges[i] < vertexAges[id]) {
                        id = i;
                    }
                }
            }
        }
        return id;
    }

    private int nextVertexWConfig() {
        int id = currentCover.nextClearBit(0);
        for (int i = 0; i < vertexScores.length; i++) {
            if (!currentCover.get(i) && wConfig.get(i)) {
                if (vertexScores[i] > vertexScores[id]) {
                    id = i;
                } else if (vertexScores[i] == vertexScores[id]) {
                    if (vertexAges[i] < vertexAges[id]) {
                        id = i;
                    }
                }
            }
        }
        return id;
    }

    private void updateWConfig(int id, BasicGraph graph) {
        BitSet adjacentVertices = graph.getAdjacencyBitSet(id);
        int i = adjacentVertices.nextSetBit(0);
        while (i != -1) {
            wConfig.set(i);
            i = adjacentVertices.nextSetBit(i + 1);
        }
        vertexAges[id] = iteration;
    }

    private void updateEdgeWeights(BasicGraph graph) {
        for (int i = 0; i < graph.getNumVertices(); i++) {
            for (int j = 0; j < graph.getNumVertices(); j++) {
                if (!currentCover.get(i) && !currentCover.get(j)) {
                    edgeWeights[i][j]++;
                    edgeWeights[j][i]++;
                    wConfig.set(i);
                    wConfig.set(j);
                }
            }
        }
    }

    private void updateScores(int id, BasicGraph graph) {
        vertexScores[id] = -vertexScores[id];
        BitSet adjacentVertices = graph.getAdjacencyBitSet(id);
        int i = adjacentVertices.nextSetBit(0);
        while (i != -1) {
            int add = edgeWeights[i][id] / graph.weight(i);
            if (currentCover.get(i) ^ currentCover.get(id)) {
                add = -add;
            }
            vertexScores[i] += add;
            i = adjacentVertices.nextSetBit(i + 1);
        }
        vertexAges[id] = iteration;
    }
}
