package be.ugent.algorithms;

import be.ugent.benchmark.IntermediateSolutionReporter;
import be.ugent.graphs.BasicGraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class DLSWCC implements WeightedVertexCoverAlgorithm {
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

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter) {
        this.graph = graph;
        numVertices = graph.getNumVertices();
        initialize();
        iteration = 0;
        int id;
        BitSet vertices;
        while (iteration < maxIterations) {
            while (graph.isVertexCover(currentCover)) {
                upperBound = graph.getWeight(currentCover);
                minimumVertexCover = (BitSet) currentCover.clone();
                id = nextVertex(currentCover);
                updateScores(id);
                currentCover.clear(id);
                wConfig.clear(id);
                updateWConfig(id);
            }
            vertices = (BitSet) currentCover.clone();
            vertices.andNot(tabuList);
            id = nextVertex(vertices);
            if (id != -1) {
                updateScores(id);
                currentCover.clear(id);
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
                updateScores(id);
                currentCover.set(id);
                updateWConfig(id);
                updateEdgeWeights();
                tabuList.set(id);
            }
            iteration++;
        }
        return minimumVertexCover;
    }

    private void initialize() {
        wConfig = new BitSet(numVertices);
        wConfig.set(0, numVertices);
        edgeWeights = new int[numVertices][numVertices];
        vertexScores = new double[numVertices];
        vertexAges = new int[numVertices];
        tabuList = new BitSet(graph.getNumVertices());
        for (int i = 0; i < numVertices; i++) {
            vertexScores[i] = graph.degree(i);
            for (int j = 0; j < numVertices; j++) {
                edgeWeights[i][j] = 1;
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
                edgeWeights[i][j]++;
                edgeWeights[j][i]++;
                wConfig.set(i);
                wConfig.set(j);
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
