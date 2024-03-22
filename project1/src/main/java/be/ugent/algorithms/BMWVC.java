package be.ugent.algorithms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import be.ugent.benchmark.IntermediateSolutionReporter;
import be.ugent.benchmark.SolutionReporter;
import be.ugent.graphs.BasicGraph;

public class BMWVC implements WeightedVertexCoverAlgorithm {

    private static final Logger logger = LogManager.getLogger(BMWVC.class);

    // Set to keep track of removed nodes
    Set<Integer> removed_nodes = new HashSet<Integer>();

    public BMWVC() {

    }

    private int satisfiesDegree0(BasicGraph graph) {
        for (int vertex = 0; vertex < graph.getNumVertices(); vertex++) {
            if (graph.degree(vertex) == 0 && !removed_nodes.contains(vertex)) {
                return vertex;
            }
        }
        return -1;
    }

    private int satisfiesAdjecency(BasicGraph graph) {
        for (int vertex = 0; vertex < graph.getNumVertices(); vertex++) {
            if (graph.weight(vertex) >= graph.getWeight(graph.getAdjacencyBitSet(vertex))
                    && !removed_nodes.contains(vertex)) {
                return vertex;
            }
        }
        return -1;
    }

    private int satisfiesDegree1(BasicGraph graph) {
        for (int vertex = 0; vertex < graph.getNumVertices(); vertex++) {
            BitSet adjecency = graph.getAdjacencyBitSet(vertex);
            BitSet vertexesOneCardinality = new BitSet(graph.getNumVertices());
            for (int i = adjecency.nextSetBit(0); i >= 0; i = adjecency.nextSetBit(i + 1)) {
                if (graph.getAdjacencyBitSet(i).cardinality() == 1) {
                    vertexesOneCardinality.set(i);
                }

            }
            if (graph.weight(vertex) <= graph.getWeight(vertexesOneCardinality)
                    && !removed_nodes.contains(vertex)) {
                return vertex;
            }
        }
        return -1;
    }

    private int[] satisfiesDegree2(BasicGraph graph) {
        int[] v = { -1, -1 };

        return v;
    }

    private void applyDegree0(BasicGraph graph, int vertex, BitSet cover) {
        cover.set(vertex);
        graph.removeVertex(vertex);
        removed_nodes.add(vertex);
    }

    private void applyAdjecency(BasicGraph graph, int vertex, BitSet cover) {
        BitSet adjecencyBitSet = graph.getAdjacencyBitSet(vertex);
        cover.or(adjecencyBitSet);
        for (int i = 0; i < graph.getNumVertices(); i++) {
            if (adjecencyBitSet.get(i)) {
                graph.removeVertex(i);
                removed_nodes.add(i);
            }
        }
        graph.removeVertex(vertex);
        removed_nodes.add(vertex);
    }

    private void applyDegree1(BasicGraph graph, int vertex, BitSet cover) {

        BitSet adjecencBitSet = graph.getAdjacencyBitSet(vertex);
        cover.set(vertex);
        for (int node = 0; node < graph.getNumVertices(); node++) {
            if (adjecencBitSet.get(node)) {
                graph.removeVertex(node);
                removed_nodes.add(node);
            }
        }
        graph.removeVertex(vertex);
        removed_nodes.add(vertex);
    }

    private void applyDegree2(BasicGraph graph, int vi, int vj, BitSet cover) {

    }

    private BitSet reduce(BasicGraph graph) {
        BitSet vertexCover = new BitSet(graph.getNumVertices());

        int oldGraphSize;
        int vi;
        int[] v;

        do {
            oldGraphSize = graph.getNumVertices() - removed_nodes.size();

            vi = satisfiesDegree0(graph);
            if (vi != -1) {
                applyDegree0(graph, vi, vertexCover);
            }
            vi = satisfiesAdjecency(graph);
            if (vi != -1) {
                applyAdjecency(graph, vi, vertexCover);
            }
            vi = satisfiesDegree1(graph);
            if (vi != -1) {
                applyDegree1(graph, vi, vertexCover);
            }
            // v = satisfiesDegree2(graph);
            // if (v[0] != -1 && v[1] != -1) {
            // applyDegree2(graph, v[0], v[1], vertexCover);
            // }

        } while (oldGraphSize != (graph.getNumVertices() - removed_nodes.size()));

        return vertexCover;

    }

    private BitSet search(BasicGraph graph, BitSet cover, BitSet best, Set<Integer> currentRemovedNodes) {
        if (currentRemovedNodes.size() >= graph.getNumVertices()) { // If the vertex cover is complete
            return (BitSet) cover.clone();
        } else if (graph.getWeight(cover) >= graph.getWeight(best)) {
            // ArrayList<BitSet> disjointed = graph.findDisjointed();
            System.out.println(graph.getNumVertices() - currentRemovedNodes.size());
            return (BitSet) best.clone();
        }

        int vertex = -1;
        int maxDegree = Integer.MIN_VALUE;
        for (int node = 0; node < graph.getNumVertices(); node++) {
            if (!currentRemovedNodes.contains(node)) {
                int degree = graph.getAdjacencyBitSet(node).cardinality();
                if (degree > maxDegree) {
                    maxDegree = degree;
                    vertex = node;
                }
            }
        }
        assert (vertex != -1); // should always be the case

        BasicGraph newGraph = graph.copy();
        Set<Integer> newCurrentRemovedNodes = new HashSet<>();
        newCurrentRemovedNodes.addAll(currentRemovedNodes);

        newCurrentRemovedNodes.add(vertex);
        newGraph.removeVertex(vertex);
        cover.set(vertex);
        best = search(newGraph, (BitSet) cover.clone(), best, newCurrentRemovedNodes);

        newCurrentRemovedNodes = new HashSet<>();
        newCurrentRemovedNodes.addAll(currentRemovedNodes);
        newCurrentRemovedNodes.add(vertex);
        newGraph = graph.copy();
        newGraph.removeVertex(vertex);
        cover.clear(vertex);
        BitSet adjec = graph.getAdjacencyBitSet(vertex);
        for (int node = adjec.nextSetBit(0); node >= 0; node = adjec.nextSetBit(node + 1)) {
            newGraph.removeVertex(node);
            newCurrentRemovedNodes.add(node);
        }
        cover.or(adjec);

        return search(newGraph, (BitSet) cover.clone(), best, newCurrentRemovedNodes);
    }

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph,
            IntermediateSolutionReporter intermediateSolutionReporter) {

        BitSet S = reduce(graph);
        // BitSet S = new BitSet();

        System.out.println("Reduced graph by: " + removed_nodes.size() + " nodes");
        System.out.println("[");
        for (int node = 0; node < S.size(); node++) {
            if (S.get(node))
                System.out.print(node + ", ");
        }
        System.out.println("\n]");
        BitSet Sb = new BitSet(graph.getNumVertices());
        BitSet Si = new BitSet(graph.getNumVertices());
        Sb.set(0, graph.getNumVertices());
        S.or(search(graph, Si, Sb, removed_nodes));
        return S;
    }

    public static void main(String[] args) {
        BasicGraph graph = new BasicGraph("DIMACS_subset_ascii/C500.9.clq");
        graph.setRandomWeights(0);
        BMWVC bmwvc = new BMWVC();
        BitSet cover = bmwvc.calculateMinVertexCover(graph, null);
        System.out.println("[");
        for (int node = 0; node < cover.size(); node++) {
            if (cover.get(node))
                System.out.print(node + ", ");
        }
        System.out.println("\n]");
        System.out.println(cover.cardinality());
        System.out.println(graph.isVertexCover(cover));
    }
}
