package be.ugent.algorithms;

import be.ugent.benchmark.IntermediateSolutionReporter;
import be.ugent.graphs.BasicGraph;
import org.javatuples.Pair;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PricingMethod implements WeightedVertexCoverAlgorithm {
    BitSet tightVertices;
    int[] vertexPrices;


    private static final Logger logger = LogManager.getLogger(PricingMethod.class.getName());


    public boolean isTight(int vertex, int[][] edgePrices, BasicGraph graph) {
        boolean isTight = this.tightVertices.get(vertex);
        if (!isTight) {
            int vertexPrice = this.getVertexPrice(vertex, edgePrices);
            isTight = vertexPrice == graph.getWeight(vertex);
            if (isTight) {
                this.tightVertices.set(vertex);
            }
        }
        return vertexPrices[vertex] == graph.getWeight(vertex);
    }

    private int getVertexPrice(int u, int[][] edgePrices) {
        return Arrays.stream(edgePrices[u]).sum();
    }

    private void setEdgePrice(int u, int v, int[][] edgePrices, int price) {
        edgePrices[u][v] = price;
        edgePrices[v][u] = price;
        vertexPrices[u] = Arrays.stream(edgePrices[u]).sum(); // Update the sum of edge prices for u
        vertexPrices[v] = Arrays.stream(edgePrices[v]).sum(); // Update the sum of edge prices for v
    }


    private Pair<Integer, Integer> getFirstNonTightVertexPair(int[][] edgePrices, BasicGraph graph) {
        for (int i = 0; i < graph.getNumVertices(); i++) {
            boolean iIsTight = this.isTight(i, edgePrices, graph);
            if (!iIsTight) {
                for (int j = 0; j < graph.getNumVertices(); j++) {
                    if (i != j) {
                        boolean jIsTight = this.isTight(j, edgePrices, graph);
                        if (!jIsTight) {
                            if (graph.hasEdge(i, j)) {
                                return Pair.with(i, j);
                            }

                        } else {
                            this.tightVertices.set(j);
                        }
                    }
                }
            } else {
                this.tightVertices.set(i);
            }
        }
        return null;
    }

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter) {
        int numberOfVertices = graph.getNumVertices();
        this.vertexPrices = new int[numberOfVertices];
        this.tightVertices = new BitSet(numberOfVertices);
        this.tightVertices.clear();
        BitSet vertexCover = new BitSet(numberOfVertices);
        int[][] edgePrices = new int[numberOfVertices][numberOfVertices];

        for (int i = 0; i < numberOfVertices; i++) {
            edgePrices[i] = new int[numberOfVertices];
            for (int j = 0; j < numberOfVertices; j++) {
                edgePrices[i][j] = 0;
            }
        }

        Pair<Integer, Integer> nonTightVertices = this.getFirstNonTightVertexPair(edgePrices, graph);

        while (nonTightVertices != null) {
            int u = nonTightVertices.getValue0();
            int v = nonTightVertices.getValue1();

            int uWeight = graph.getWeight(u);
            int vWeight = graph.getWeight(v);

            int uPrice = this.getVertexPrice(u, edgePrices);
            int vPrice = this.getVertexPrice(v, edgePrices);

            int uDifference = uWeight - uPrice;
            int vDifference = vWeight - vPrice;

            int price = Math.min(uDifference, vDifference);

            if (price == uDifference) {
                vertexCover.set(u);
            } else {
                vertexCover.set(v);
            }

            this.setEdgePrice(u, v, edgePrices, price);
            intermediateSolutionReporter.solutionCallback(vertexCover);
            nonTightVertices = this.getFirstNonTightVertexPair(edgePrices, graph);
        }

        return vertexCover;
    }

    public static void main(String[] args) {
        PricingMethod pricingMethod = new PricingMethod();
        BasicGraph graph = new BasicGraph("customgraphs/square-5.cwg");

        BitSet vertexCover = pricingMethod.calculateMinVertexCover(graph, null);
        logger.info("Vertex cover: " + vertexCover);
        logger.info("Weight of vertex cover: " + graph.getWeight(vertexCover));

    }

}
