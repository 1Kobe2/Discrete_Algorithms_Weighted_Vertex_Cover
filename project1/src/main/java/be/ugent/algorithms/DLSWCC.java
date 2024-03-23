package be.ugent.algorithms;

import be.ugent.benchmark.IntermediateSolutionReporter;
import be.ugent.graphs.BasicGraph;

import java.util.BitSet;
import java.util.List;

public class DLSWCC implements WeightedVertexCoverAlgorithm {
    private final int maxIterations;    //maximum amount of iterations
    private BasicGraph graph;           //BasicGraph representation of current graph file
    private int numVertices;            //number of vertices in current graph
    private BitSet wConfig;             //has vertexScores[i] been changed since vertex i was last removed
    private int[][] edgeWeights;        //higher weight means the edge is more often uncovered by currentCover
    private double[] vertexScores;      //higher score means vertex is more likely to be added/removed
    private int[] vertexAges;           //last iteration where vertex was added/removed
    private BitSet tabuList;            //vertices that were added in the last iteration
    private BitSet minimumVertexCover;  //current best found solution
    private BitSet currentCover;        //current working solution
    private int upperBound;             //total weight of current best solution
    private int iteration;              //current iteration
    private int lastImprovement;        //iteration at which the last improvement to the best solution was made

    /*
        Initialize DLSWCC with given number of maxIterations
     */
    public DLSWCC(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /*
        Initialize DLSWCC with default number of maxIterations
    */
    public DLSWCC() {
        this.maxIterations = 10000000;
    }

    /*
        Calculate the minimum weighted vertex cover for the given graph
     */
    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter) {
        initialize(graph);
        while (iteration < maxIterations && iteration - lastImprovement < maxIterations / 10) {
            while (graph.isVertexCover(currentCover)) {
                upperBound = graph.getWeight(currentCover);
                minimumVertexCover = (BitSet) currentCover.clone();
                lastImprovement = iteration;
                //intermediateSolutionReporter.solutionCallback(minimumVertexCover);
                int id = nextVertex(currentCover);
                currentCover.clear(id);
                wConfig.clear(id);
                updateVertices(id);
            }
            BitSet vertices = (BitSet) currentCover.clone();
            vertices.andNot(tabuList);
            int id = nextVertex(vertices);
            if (id != -1) {
                currentCover.clear(id);
                wConfig.clear(id);
                updateVertices(id);
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
                updateVertices(id);
                updateEdgeWeights();
                tabuList.set(id);
            }
            iteration++;
        }
        return minimumVertexCover;
    }

    /*
        Initialize all helper values, arrays and bitsets, and calculate an initial solution
     */
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
            vertexScores[i] = (double) graph.degree(i) / graph.getWeight(i);
            for (int j = i + 1; j < numVertices; j++) {
                if (graph.hasEdge(i, j)) {
                    edgeWeights[i][j] = 1;
                    edgeWeights[j][i] = 1;
                }
            }
        }
        initialMinimumVertexCover();
    }

    /*
        Calculate an initial minimum weighted vertex cover using a greedy approach
     */
    private void initialMinimumVertexCover() {
        currentCover = new BitSet(numVertices);
        List<Integer> vertices = graph.orderByDegree();
        int i = 0;
        while (!graph.isVertexCover(currentCover)) {
            currentCover.set(vertices.get(i));
            i++;
        }
    }

    /*
        Select the vertex with the highest score in the given BitSet, breaking ties in favor of the oldest one
     */
    private int nextVertex(BitSet vertices) {
        int id = vertices.nextSetBit(0);
        for (int i = vertices.nextSetBit(id + 1); i >= 0; i = vertices.nextSetBit(i + 1)) {
            if (vertexScores[i] > vertexScores[id] || (vertexScores[i] == vertexScores[id] && vertexAges[i] < vertexAges[id])) {
                id = i;
            }
        }
        return id;
    }

    /*
        Increase edge weights of all edges not covered by currentCover
     */
    private void updateEdgeWeights() {
        for (int i = currentCover.nextClearBit(0); i < numVertices; i = currentCover.nextClearBit(i + 1)) {
            for (int j = currentCover.nextClearBit(i + 1); j < numVertices; j = currentCover.nextClearBit(j + 1)) {
                if (graph.hasEdge(i, j)) {
                    edgeWeights[i][j]++;
                    edgeWeights[j][i]++;
                    vertexScores[i] += 1.0d / graph.getWeight(i);
                    vertexScores[j] += 1.0d / graph.getWeight(j);
                    wConfig.set(i);
                    wConfig.set(j);
                }
            }
        }
    }

    /*
        Update vertexScores for the current added/removed vertex and its neighbours
     */
    private void updateVertices(int id) {
        vertexScores[id] = -vertexScores[id];
        vertexAges[id] = iteration;
        BitSet adjacentVertices = graph.getAdjacencyBitSet(id);
        for (int i = adjacentVertices.nextSetBit(0); i != -1; i = adjacentVertices.nextSetBit(i + 1)) {
            wConfig.set(i);
            double add = (double) edgeWeights[i][id] / graph.getWeight(i);
            if (currentCover.get(i) != currentCover.get(id)) {
                add = -add;
            }
            vertexScores[i] += add;
        }
    }
}
