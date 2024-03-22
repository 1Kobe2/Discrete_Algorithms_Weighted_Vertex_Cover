package be.ugent.util;

import be.ugent.graphs.BasicGraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

public class GraphGenerator {

    public static BasicGraph generate(int vertexCount, float edgeProbability) {
        BitSet[] adjacencyList = new BitSet[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            adjacencyList[i] = new BitSet(vertexCount);
        }
        BasicGraph graph = new BasicGraph(adjacencyList, new ArrayList<>(Collections.nCopies(vertexCount, 1)));
        graph.setRandomWeights(42 + vertexCount);
        Random random = new Random((long) 42 + vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            for (int j = i + 1; j < vertexCount; j++) {
                if (random.nextFloat() < edgeProbability) {
                    graph.addEdge(i, j);
                }
            }
        }
        return graph;
    }

    public static void main(String[] args) {
        int[] vertexCounts = {
                5, 10, 20, 30, 40, 50,
                100, 150, 200, 500,
                1_000, 2_000, 5_000,
                10_000, 15_000, 20_000, 25_000, 50_000, 75_000,
                100_000,
        };
        float[] edgeProbabilities = {
                0.000_000_01f, 0.000_000_05f,
                0.000_000_1f, 0.000_000_5f,
                0.000_001f, 0.000_005f,
                0.000_01f, 0.000_05f,
                0.0001f, 0.0005f,
                0.001f, 0.005f,
                0.01f, 0.05f,
                0.1f, 0.5f,
        };

        for (int count : vertexCounts) {
            for (float edgeProbability : edgeProbabilities) {
                if (count * edgeProbability >= 2) {
                    BasicGraph graph = generate(count, edgeProbability);
                    graph.exportToCWG("graph_" + count + "_" + edgeProbability + ".cwg");
                }
            }

        }
    }

}
