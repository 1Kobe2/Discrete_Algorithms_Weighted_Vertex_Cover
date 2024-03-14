package be.ugent.algorithms;

import be.ugent.benchmark.IntermediateSolutionReporter;
import be.ugent.graphs.BasicGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FixedSetSearch implements WeightedVertexCoverAlgorithm {

    private final int candidateListSize = 5;
    private final int initialPopulationSize = 100;
    private final int maxAmountOfSolutions = 5000;
    private final int stagnationLimit = 100;


    private List<Integer> restrictedCandidateList(BasicGraph graph, BitSet solution) {
        int score;
        BitSet edges;
        Map<Integer, Integer> vertexAndScore = new HashMap<>();
        for (int i = 0; i < graph.getNumEdges(); i++) {
            if (!solution.get(i)) {
                edges = (BitSet) graph.getAdjacencyBitSet(i).clone();
                edges.andNot(solution);
                score = edges.cardinality() / graph.weight(i);
                vertexAndScore.put(i, score);
            }
        }
        List<Integer> vertices = new ArrayList<>(vertexAndScore.keySet());

        // sort vertices based on their score
        vertices.sort(Comparator.comparingInt(vertexAndScore::get));

        return vertices.subList(0, Math.min(this.candidateListSize, vertices.size()));
    }

    private BitSet randomizedGreedyConstruction(BasicGraph graph, BitSet fixedSet) {
        BitSet solution;
        solution = fixedSet == null ? new BitSet(graph.getNumVertices()) : (BitSet) fixedSet.clone();

        Random random = new Random();
        while (!graph.isVertexCover(solution)) {
            List<Integer> candidates = restrictedCandidateList(graph, solution);
            solution.set(candidates.get(random.nextInt(candidates.size())));
        }
        return solution;
    }


    private BitSet randomizedGreedyConstruction(BasicGraph graph) {
        return randomizedGreedyConstruction(graph, null);
    }

    private List<Integer> getElementSwapImprovements(BasicGraph graph, BitSet solution) {
        BitSet uniqueCover;
        int improvement;
        List<Integer> improvements = new ArrayList<>();
        for (int vertex = 0; vertex < graph.getNumVertices(); vertex++) {

            if (solution.get(vertex)) { // can only swap out vertices that are in the current solution
                uniqueCover = (BitSet) graph.getAdjacencyBitSet(vertex).clone();
                uniqueCover.andNot(solution);

                improvement = graph.weight(vertex) - graph.getWeight(uniqueCover);
                if (improvement > 0) {
                    improvements.add(improvement);
                }
            }
        }
        return improvements;
    }

    private List<int[]> getPairSwapImprovements(BasicGraph graph, BitSet solution) {
        BitSet uniqueCover;
        int improvement;
        int[] pair = new int[2];
        List<int[]> improvements = new ArrayList<>();
        for (int vertex1 = 0; vertex1 < graph.getNumVertices(); vertex1++) {
            for (int vertex2 = 0; vertex2 < graph.getNumVertices(); vertex2++) {
                if (vertex1 != vertex2 && solution.get(vertex1) && solution.get(vertex2)
                        && !graph.getAdjacencyBitSet(vertex1).get(vertex2)) {
                    uniqueCover = (BitSet) graph.getAdjacencyBitSet(vertex1).clone();
                    uniqueCover.or(graph.getAdjacencyBitSet(vertex2));
                    uniqueCover.andNot(solution);

                    improvement = graph.weight(vertex1) + graph.weight(vertex2) - graph.getWeight(uniqueCover);
                    if (improvement > 0) {
                        pair[0] = vertex1;
                        pair[1] = vertex2;
                        improvements.add(pair);
                    }
                }
            }
        }
        return improvements;
    }

    private BitSet swap(BasicGraph graph, int vertex, BitSet solution) {
        BitSet newSolution = (BitSet) graph.getAdjacencyBitSet(vertex).clone();
        newSolution.or(solution);
        newSolution.flip(vertex);
        return newSolution;
    }

    private BitSet swap(BasicGraph graph, int vertex1, int vertex2, BitSet solution) {
        BitSet newSolution = (BitSet) graph.getAdjacencyBitSet(vertex1).clone();
        newSolution.or(graph.getAdjacencyBitSet(vertex2));
        newSolution.or(solution);
        newSolution.flip(vertex1);
        newSolution.flip(vertex2);
        return newSolution;
    }

    private BitSet localSearch(BasicGraph graph, BitSet solution) {
        List<Integer> elementSwapImprovements = getElementSwapImprovements(graph, solution);
        List<int[]> pairSwapImprovements = new ArrayList<>();
        Random random = new Random();
        int vertex;
        int[] pair;
        while (elementSwapImprovements.size() + pairSwapImprovements.size() != 0) {
            while (elementSwapImprovements.size() != 0) {
                vertex = elementSwapImprovements.remove(random.nextInt(elementSwapImprovements.size()));
                solution = swap(graph, vertex, solution);
            }

            pairSwapImprovements = getPairSwapImprovements(graph, solution);
            if (pairSwapImprovements.size() != 0) {
                pair = pairSwapImprovements.remove(random.nextInt(pairSwapImprovements.size()));
                solution = swap(graph, pair[0], pair[1], solution);
            }
            elementSwapImprovements = getElementSwapImprovements(graph, solution);
        }
        return solution;
    }

    private Map<BitSet, Integer> getInitialPopulation(BasicGraph graph) {
        BitSet solution;
        Map<BitSet, Integer> solutions = new HashMap<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            solution = randomizedGreedyConstruction(graph);
            solution = localSearch(graph, solution);
            solutions.put(solution, graph.getWeight(solution));
        }
        return solutions;
    }

    private BitSet getFixedSet(BasicGraph graph, BitSet base, List<BitSet> solutions, int size) {

        // Count the vertex occurrences over all solutions
        int[] vertexCounts = new int[graph.getNumVertices()];
        for (BitSet bitSet : solutions) {
            int nextSetBit = bitSet.nextSetBit(0);
            while (nextSetBit != -1) {
                vertexCounts[nextSetBit]++;
                nextSetBit = bitSet.nextSetBit(nextSetBit + 1);
            }
        }

        // sort vertices based on their occurrences
        List<Integer> sortedVertices = IntStream.rangeClosed(0, graph.getNumVertices())
                .boxed().sorted(Comparator.comparingInt(i -> vertexCounts[i])).collect(Collectors.toList());

        // make a fixed set of size with vertices that have the most occurrences and are present in base
        int vertex;
        int index = 0;
        BitSet fixedSet = new BitSet(graph.getNumVertices());
        while (fixedSet.cardinality() != size) {
            vertex = sortedVertices.get(index);
            if (base.get(vertex)) {
                fixedSet.flip(vertex);
            }
        }
        return fixedSet;
    }

    @Override
    public BitSet calculateMinVertexCover(BasicGraph graph, IntermediateSolutionReporter intermediateSolutionReporter) {

        // initial solutions to construct fixed set with
        Map<BitSet, Integer> solutions = getInitialPopulation(graph);

        // variables for the size of the fixed set
        int size;
        int sizeIndex = 1;
        double sizeFactor = (1 - (1 / Math.pow(2, sizeIndex)));

        // used for generating the fixedSet
        BitSet fixedSet;
        BitSet baseSolution;
        List<BitSet> fixedSetSolutions;

        // current solution
        int weight;
        BitSet solution;

        // keep track of the best (minimal) solutions
        int minWeight = 0;
        BitSet minSolution = null;

        // to check if we are stagnating
        int stagnationCounter = 0;

        Random random = new Random();
        while (solutions.size() != maxAmountOfSolutions) {
            // generate subset of all solutions to construct the fixed set with
            fixedSetSolutions = new ArrayList<>(solutions.keySet());
            fixedSetSolutions.sort(Comparator.comparingInt(solutions::get));
            fixedSetSolutions = fixedSetSolutions.subList(0, this.initialPopulationSize);
            if (minSolution == null) {
                minSolution = fixedSetSolutions.get(0);
            }
            Collections.shuffle(fixedSetSolutions);

            // choose the base solution
            baseSolution = fixedSetSolutions.get(random.nextInt(this.initialPopulationSize));

            // get the fixed set of the given size
            size = (int) (baseSolution.cardinality() * sizeFactor);
            fixedSet = getFixedSet(graph, baseSolution, fixedSetSolutions, size);

            // get a greedy construction containing the fixed set and do local search
            solution = randomizedGreedyConstruction(graph, fixedSet);
            solution = localSearch(graph, solution);

            // add the solution
            weight = graph.getWeight(solution);
            solutions.put(solution, weight);

            // check if it is the new best solution
            if (weight < minWeight) {
                minSolution = solution;
                minWeight = weight;
                stagnationCounter = 0;
                intermediateSolutionReporter.solutionCallback(minSolution);
            } else if (stagnationCounter == stagnationLimit) {
                // if sizeIndex became so small that sizeFactor was 1 last round, restart from 0
                sizeIndex = sizeFactor == 1 ? 0 : sizeIndex + 1;
                sizeFactor = (1 - (1 / Math.pow(2, sizeIndex)));
            }
            stagnationCounter++;
        }

        return minSolution;
    }
}
