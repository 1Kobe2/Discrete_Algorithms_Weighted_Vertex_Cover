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
                if (graph.getAdjacencyBitSet(i).cardinality() == 1 && !removed_nodes.contains(i)) {
                    vertexesOneCardinality.set(i);
                }

            }
            if (graph.getWeight(vertex) <= graph.getWeight(vertexesOneCardinality)
                    && !removed_nodes.contains(vertex)) {
                return vertex;
            }
        }
        return -1;
    }

    private int[] satisfiesDegree2(BasicGraph graph) {
        int[] v = { -1, -1 };
        for (int vi = 0; vi < graph.getNumVertices(); vi++) {
            if (removed_nodes.contains(vi))
                continue;
            for (int vj = vi + 1; vj < graph.getNumVertices(); vj++) {
                if (removed_nodes.contains(vj))
                    continue;
                BitSet commonBitSet = (BitSet) graph.getAdjacencyBitSet(vj).clone();
                commonBitSet.and(graph.getAdjacencyBitSet(vi));

                for (int node = commonBitSet.nextSetBit(0); node >= 0; node = commonBitSet.nextSetBit(node + 1)) {
                    if (graph.getAdjacencyBitSet(node).cardinality() > 2) {
                        commonBitSet.clear(node);
                    }
                }

                if (graph.getWeight(vi) + graph.getWeight(vj) <= graph.getWeight(commonBitSet)) {
                    printBitSet(commonBitSet, graph);
                    System.out.println(vi + ", " + vj);
                    v[0] = vi;
                    v[1] = vj;
                    return v;
                }
            }
        }
        return v;
    }

    private void applyDegree0(BasicGraph graph, int vertex, BitSet cover) {
        graph.removeVertex(vertex);
        removed_nodes.add(vertex);
    }

    private void applyAdjecency(BasicGraph graph, int vertex, BitSet cover) {
        BitSet adjecencyBitSet = graph.getAdjacencyBitSet(vertex);
        cover.or(adjecencyBitSet);
        for (int i = adjecencyBitSet.nextSetBit(0); i >= 0; i = adjecencyBitSet.nextSetBit(i + 1)) {
            graph.removeVertex(i);
            removed_nodes.add(i);
        }
        graph.removeVertex(vertex);
        removed_nodes.add(vertex);
    }

    private void applyDegree1(BasicGraph graph, int vertex, BitSet cover) {
        BitSet adjecencBitSet = graph.getAdjacencyBitSet(vertex);
        cover.set(vertex);
        for (int node = 0; node < graph.getNumVertices(); node++) {
            if (adjecencBitSet.get(node) && graph.getAdjacencyBitSet(node).cardinality() == 1) {
                graph.removeVertex(node);
                removed_nodes.add(node);
            }
        }
        graph.removeVertex(vertex);
        removed_nodes.add(vertex);
    }

    private void applyDegree2(BasicGraph graph, int vi, int vj, BitSet cover) {
        BitSet commonBitSet = (BitSet) graph.getAdjacencyBitSet(vj).clone();
        commonBitSet.and(graph.getAdjacencyBitSet(vi));

        // Filter the common adjecencies on nodes with cardinality 2
        for (int node = commonBitSet.nextSetBit(0); node >= 0; node = commonBitSet.nextSetBit(node + 1)) {
            if (graph.getAdjacencyBitSet(node).cardinality() > 2) {
                commonBitSet.clear(node);
            }
        }
        cover.set(vi);
        cover.set(vj);
        for (int vertex = commonBitSet.nextSetBit(0); vertex >= 0; vertex = commonBitSet.nextSetBit(vertex + 1)) {
            graph.removeVertex(vertex);
            removed_nodes.add(vertex);
        }
        graph.removeVertex(vi);
        graph.removeVertex(vj);
        removed_nodes.add(vi);
        removed_nodes.add(vj);
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
            v = satisfiesDegree2(graph);
            if (v[0] != -1 && v[1] != -1) {
                applyDegree2(graph, v[0], v[1], vertexCover);
            }

        } while (oldGraphSize != (graph.getNumVertices() - removed_nodes.size()));

        return vertexCover;

    }

    private void printBitSet(BitSet set, BasicGraph graph) {
        System.out.print("    [");
        for (int node = 0; node < graph.getNumVertices(); node++) {
            if (set.get(node)) {
                System.out.print(node + ", ");
            }
        }
        System.out.println("]");
    }

    private int calculateLowerBound(ArrayList<BitSet> disjointed, BasicGraph graph) {
        int sum = 0;
        for (BitSet clique : disjointed) {
            int max = Integer.MIN_VALUE;
            for (int i = clique.nextSetBit(0); i >= 0; i = clique.nextSetBit(i + 1)) {
                if (graph.getWeight(i) > max) {
                    max = graph.getWeight(i);
                }
            }
            sum += graph.getWeight(clique) - max;
        }
        return sum;
    }

    private void growDisjointed(int vertex, BitSet disjoint, BasicGraph graph, BitSet visited) {
        disjoint.set(vertex);
        visited.set(vertex);
        for (int i = 0; i < graph.getNumVertices(); i++) {
            if (graph.getAdjacencyBitSet(vertex).get(i) && !visited.get(i)) {
                growDisjointed(i, disjoint, graph, visited);
            }
        }
    }

    public ArrayList<BitSet> findDisjointed(BasicGraph graph) {
        BitSet visited = new BitSet(graph.getNumVertices());

        for (int i : removed_nodes) {
            visited.set(i);
        }
        ArrayList<BitSet> disjointed = new ArrayList<BitSet>();

        for (int vertex = visited.nextClearBit(0); vertex < graph.getNumVertices(); vertex = visited
                .nextClearBit(vertex + 1)) {
            BitSet disjoint = new BitSet(graph.getNumVertices());
            growDisjointed(vertex, disjoint, graph, visited);
            disjointed.add(disjoint);
        }
        return disjointed;
    }

    private int selectVertex(BasicGraph graph) {
        int vertexMinDegree = -1;
        int maxDegree = Integer.MIN_VALUE;
        for (int vertex = 0; vertex < graph.getNumVertices(); vertex++) {
            if (!removed_nodes.contains(vertex)) {
                int degree = graph.getAdjacencyBitSet(vertex).cardinality();
                if (degree > maxDegree) {
                    maxDegree = degree;
                    vertexMinDegree = vertex;
                }
            }
        }
        return vertexMinDegree;
    }

    private BitSet search(BasicGraph graph, BitSet cover, BitSet best) {
        // System.out.print("\n");
        // System.out.print("current: ");
        // printBitSet(cover, graph);
        // System.out.print("best: ");
        // printBitSet(best, graph);

        if (removed_nodes.size() >= graph.getNumVertices()) { // If the vertex cover is complete
            if (graph.getWeight(cover) < graph.getWeight(best)) { // And is better then the current one
                return (BitSet) cover.clone();
            } else {
                return (BitSet) best.clone();
            }
        }

        ArrayList<BitSet> disjointed = findDisjointed(graph);
        // if (calculateLowerBound(disjointed, graph)
        // + graph.getWeight(cover) >= graph.getWeight(best)) {
        if (graph.getWeight(cover) > graph.getWeight(best)) {
            // System.out.println("lowerbound prune");
            return (BitSet) best.clone();
        }

        int vertex = selectVertex(graph);

        BasicGraph newGraph = graph.copy();
        Set<Integer> oldRemovedNodes = new HashSet<>();
        oldRemovedNodes.addAll(removed_nodes);

        removed_nodes.add(vertex);
        newGraph.removeVertex(vertex);
        cover.set(vertex);
        best = search(newGraph, (BitSet) cover.clone(), best);

        removed_nodes = new HashSet<>();
        removed_nodes.addAll(oldRemovedNodes);
        removed_nodes.add(vertex);
        newGraph = graph.copy();
        newGraph.removeVertex(vertex);
        cover.clear(vertex);
        BitSet adjec = graph.getAdjacencyBitSet(vertex);
        for (int node = adjec.nextSetBit(0); node >= 0; node = adjec.nextSetBit(node + 1)) {
            newGraph.removeVertex(node);
            removed_nodes.add(node);
        }
        cover.or(adjec);
        return search(newGraph, (BitSet) cover.clone(), best);
    }

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph,
            IntermediateSolutionReporter intermediateSolutionReporter) {

        BitSet S = reduce(graph);

        System.out.println("Reduced graph by: " + removed_nodes.size() + " nodes");
        // System.out.print("[");
        // for (int node : removed_nodes) {
        // System.out.print(node + ", ");
        // }
        // System.out.println("]");
        // printBitSet(S, graph);

        ArrayList<BitSet> disjointed = findDisjointed(graph);
        // Remember full graph
        Set<Integer> nodesRemovedThroughReduction = new HashSet<>();
        nodesRemovedThroughReduction.addAll(removed_nodes);
        for (BitSet subGraph : disjointed) {
            BitSet Sb = new BitSet(graph.getNumVertices());
            BitSet Si = new BitSet(graph.getNumVertices());
            Sb.set(0, graph.getNumVertices());

            BasicGraph disjoint = graph.copy();
            for (int node = 0; node < graph.getNumVertices(); node++) {
                if (!subGraph.get(node)) {
                    disjoint.removeVertex(node);
                    removed_nodes.add(node);
                }
            }
            BitSet set = search(disjoint, Si, subGraph);
            System.out.print("Solution for graph: ");
            printBitSet(subGraph, graph);
            System.out.print(": ");
            printBitSet(set, graph);
            S.or(set);
            removed_nodes = new HashSet<>();
            removed_nodes.addAll(nodesRemovedThroughReduction);
        }

        return S;
    }

    public static void main(String[] args) {
        BasicGraph graph = new BasicGraph("customgraphs/graph_100_0.05.cwg");
        BMWVC bmwvc = new BMWVC();
        BitSet cover = bmwvc.calculateMinVertexCover(graph.copy(), null);
        System.out.print("[");
        for (int node = 0; node < cover.size(); node++) {
            if (cover.get(node))
                System.out.print("(" + node + ", " + graph.getWeight(node) + ")");
        }
        System.out.println("]");
        System.out.println(cover.cardinality());
        System.out.println(graph.getWeight(cover));
        System.out.println(graph.isVertexCover(cover));
    }
}
