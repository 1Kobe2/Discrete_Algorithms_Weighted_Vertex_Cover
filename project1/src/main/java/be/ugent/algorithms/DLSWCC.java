package be.ugent.algorithms;

import be.ugent.benchmark.IntermediateSolutionReporter;
import be.ugent.benchmark.SolutionReporter;
import be.ugent.graphs.BasicGraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class DLSWCC implements WeightedVertexCoverAlgorithm {
    private static final String[] filePaths = {
            "customgraphs/graph3.cwg",
            "customgraphs/square-5.cwg",
            "customgraphs/triangle-4.cwg",
            "customgraphs/graph_5_0.5.cwg",
    };
    private final int maxIterations;
    private BitSet wConfig;
    private int[][] edgeWeights;
    private double[] vertexScores;
    private int[] vertexAges;
    private BitSet minimumVertexCover;
    private BitSet currentCover;
    private int upperBound;
    private int iteration;
    private BasicGraph graph;
    private int numVertices;
    private BitSet tabuList;

    public DLSWCC(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public DLSWCC() {
        this.maxIterations = 1000000;
    }

    public static void main(String[] args) {
        DLSWCC dlswcc = new DLSWCC();
        for (String filePath : filePaths) {
            BasicGraph graph = new BasicGraph(filePath);
            String uniqueIdentifier = String.format("%s-%s-%d", "DLSWCC", filePath, 0);
            BitSet minCover = dlswcc.calculateMinVertexCover(graph, solution -> new SolutionReporter<>().reportSolution(solution, uniqueIdentifier));
            System.out.println(minCover);
            System.out.println(graph.getWeight(minCover));
            System.out.println(graph.isVertexCover(minCover));
        }
    }

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter) {
        initialize(graph);
        while (iteration < maxIterations) {
            while (graph.isVertexCover(currentCover)) {
                upperBound = graph.getWeight(currentCover);
                minimumVertexCover = (BitSet) currentCover.clone();
                int id = nextVertex(currentCover);
                currentCover.clear(id);
                updateScores(id);
                wConfig.clear(id);
                updateWConfig(id);
            }
            BitSet vertices = (BitSet) currentCover.clone();
            vertices.andNot(tabuList);
            int id = nextVertex(vertices);
            if (id != -1) {
                currentCover.clear(id);
                updateScores(id);
                wConfig.clear(id);
                updateWConfig(id);
            }
            tabuList.clear();
            while (!graph.isVertexCover(currentCover)) {
                vertices = (BitSet) wConfig.clone();
                vertices.andNot(currentCover);
                id = nextVertex(vertices);
                if (graph.getWeight(currentCover) + graph.getWeight(id) >= upperBound) {
                    break;
                }
                currentCover.set(id);
                updateScores(id);
                updateWConfig(id);
                updateEdgeWeights();
                tabuList.set(id);
            }
            iteration++;
        }
        return minimumVertexCover;
    }

    private void initialize(BasicGraph graph) {
        this.graph = graph;
        numVertices = graph.getNumVertices();
        wConfig = new BitSet(numVertices);
        wConfig.set(0, numVertices);
        edgeWeights = new int[numVertices][numVertices];
        vertexScores = new double[numVertices];
        vertexAges = new int[numVertices];
        tabuList = new BitSet(graph.getNumVertices());
        iteration = 0;
        for (int i = 0; i < numVertices; i++) {
            vertexScores[i] = graph.degree(i);
            for (int j = i + 1; j < numVertices; j++) {
                if (graph.hasEdge(i, j)) {
                    edgeWeights[i][j] = 1;
                    edgeWeights[j][i] = 1;
                }
            }
        }
        initialMinimumVertexCover();
    }

    private void initialMinimumVertexCover() {
        minimumVertexCover = new BitSet(numVertices);
        List<Double> scores = new ArrayList<>();
        for (double i : vertexScores) {
            scores.add(i);
        }
        List<Double> indices = new ArrayList<>(scores);
        scores.sort(Collections.reverseOrder(Double::compare));
        int i = 0;
        while (!graph.isVertexCover(minimumVertexCover)) {
            int id = indices.indexOf(scores.get(i));
            minimumVertexCover.set(id);
            indices.set(id, null);
            i++;
        }
        currentCover = (BitSet) minimumVertexCover.clone();
    }

    private int nextVertex(BitSet vertices) {
        int id = vertices.nextSetBit(0);
        for (int i = vertices.nextSetBit(id + 1); i >= 0; i = vertices.nextSetBit(i + 1)) {
            if (vertexScores[i] > vertexScores[id] || (vertexScores[i] == vertexScores[id] && vertexAges[i] < vertexAges[id])) {
                id = i;
            }
        }
        return id;
    }

    private void updateWConfig(int id) {
        BitSet adjacentVertices = graph.getAdjacencyBitSet(id);
        for (int i = adjacentVertices.nextSetBit(0); i != -1; i = adjacentVertices.nextSetBit(i + 1)) {
            wConfig.set(i);
        }
        vertexAges[id] = iteration;
    }

    private void updateEdgeWeights() {
        for (int i = currentCover.nextClearBit(0); i < numVertices; i = currentCover.nextClearBit(i + 1)) {
            for (int j = currentCover.nextClearBit(i + 1); j < numVertices; j = currentCover.nextClearBit(j + 1)) {
                if (graph.hasEdge(i, j)) {
                    edgeWeights[i][j]++;
                    edgeWeights[j][i]++;
                    wConfig.set(i);
                    wConfig.set(j);
                }
            }
        }
    }

    private void updateScores(int id) {
        vertexScores[id] = -vertexScores[id];
        BitSet adjacentVertices = graph.getAdjacencyBitSet(id);
        for (int i = adjacentVertices.nextSetBit(0); i != -1; i = adjacentVertices.nextSetBit(i + 1)) {
            double add = edgeWeights[i][id] / (double) graph.getWeight(i);
            if (currentCover.get(i) ^ currentCover.get(id)) {
                add = -add;
            }
            vertexScores[i] += add;
        }
        vertexAges[id] = iteration;
    }
}
