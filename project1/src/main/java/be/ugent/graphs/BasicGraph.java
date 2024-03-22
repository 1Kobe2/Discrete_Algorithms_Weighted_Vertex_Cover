package be.ugent.graphs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class BasicGraph {

    private static final Logger logger = LogManager.getLogger(BasicGraph.class);

    // made with chatGPT

    protected BitSet[] adjacencyList;
    protected int numVertices;
    protected List<Integer> weights;

    public BasicGraph(BasicGraph graph) {
        this.adjacencyList = graph.adjacencyList;
        this.numVertices = graph.numVertices;
        this.weights = graph.weights;
    }

    public BasicGraph(BitSet[] adjacencyList, List<Integer> weights) {
        this.adjacencyList = adjacencyList;
        this.numVertices = adjacencyList.length;
        this.weights = weights;
    }

    public BasicGraph(String graphFilename) {
        int expectedNumberOfEdges = -1; // Initialize with a sentinel value
        int actualNumberOfEdges;

        URL res = getClass().getClassLoader().getResource(graphFilename);
        File file = null;
        try {
            file = Paths.get(res.toURI()).toFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    // Skip comment lines or empty lines
                    if (line.isEmpty() || line.charAt(0) == 'c') {
                        continue;
                    }
                    // Process problem line
                    if (line.charAt(0) == 'p') {
                        StringTokenizer st = new StringTokenizer(line);
                        st.nextToken(); // skip 'p' token
                        st.nextToken(); // skip problem type (e.g., 'edge')
                        this.numVertices = Integer.parseInt(st.nextToken());
                        expectedNumberOfEdges = Integer.parseInt(st.nextToken()); // Store the expected number of edges
                        // Initialize graph with the number of vertices
                        adjacencyList = new BitSet[this.numVertices];
                        for (int i = 0; i < this.numVertices; i++) {
                            adjacencyList[i] = new BitSet(this.numVertices);
                        }
                    } else if (line.charAt(0) == 'e') {
                        // Process edge line
                        String[] parts = line.split(" ");
                        int source = Integer.parseInt(parts[1]) - 1; // DIMACS vertices start from 1
                        int destination = Integer.parseInt(parts[2]) - 1; // DIMACS vertices start from 1
                        addEdge(source, destination);
                    }
                }
                actualNumberOfEdges = this.getNumEdges();
                if (actualNumberOfEdges != expectedNumberOfEdges) {

                    logger.error(
                            "Error: The actual number of edges ({}) does not match the expected number ({}).",
                            actualNumberOfEdges,
                            expectedNumberOfEdges);
                    logger.error("Exiting...");
                    System.exit(1);
                }
                weights = new ArrayList<>(Collections.nCopies(numVertices, 1));
            } catch (IOException e) {
                logger.error("Error reading file: {}", e.getMessage());
                logger.error("Exiting...");
                System.exit(1);
            }
        } catch (URISyntaxException e) {
            logger.error("Error reading file: {}", e.getMessage());
            logger.error("Exiting...");
            System.exit(1);
        }
    }

    public void addEdge(int source, int destination) {
        adjacencyList[source].set(destination);
        adjacencyList[destination].set(source); // Since it's an undirected graph
    }

    public boolean hasEdge(int source, int destination) {
        return adjacencyList[source].get(destination);
    }

    public BasicGraph copy() {
        BitSet[] newAdjecencyLists = new BitSet[this.adjacencyList.length];
        for (int i = 0; i < this.adjacencyList.length; i++) {
            newAdjecencyLists[i] = (BitSet) this.adjacencyList[i].clone();
        }
        ArrayList<Integer> newWeigths = new ArrayList<>();
        for (int w : weights) {
            newWeigths.add(w);
        }
        return new BasicGraph(newAdjecencyLists, newWeigths);
    }

    public void removeVertex(int vertex) {
        for (int node = 0; node < numVertices; node++) {
            getAdjacencyBitSet(node).clear(node);
        }
    }

    private void growDisjointed(int vertex, BitSet disjoint) {
        disjoint.set(vertex);
        for (int i = 0; i < this.numVertices; i++) {
            if (getAdjacencyBitSet(vertex).get(i) && !disjoint.get(i)) {
                growDisjointed(vertex, disjoint);
            }
        }
    }

    public ArrayList<BitSet> findDisjointed() {
        BitSet removed = new BitSet(this.numVertices);
        ArrayList<BitSet> disjointed = new ArrayList<BitSet>();
        HashSet<Integer> workList = new HashSet<>();
        for (int node = 0; node < this.numVertices; node++) {
            workList.add(node);
        }
        for (int node = 0; node < this.numVertices; node++) {
            if (!removed.get(node)) {
                BitSet disjoint = new BitSet(this.numVertices);
                growDisjointed(node, disjoint);
                removed.xor(disjoint);
                disjointed.add(disjoint);
            }
        }

        return disjointed;
    }

    public void swapVertices(int vertex1, int vertex2) {
        if (vertex1 == vertex2) {
            return; // No need to swap if the vertices are the same
        }
        // Swap weights for vertex1 and vertex2
        Collections.swap(weights, vertex1, vertex2);

        // Swap adjacency information for vertex1 and vertex2
        BitSet temp = adjacencyList[vertex1];
        adjacencyList[vertex1] = adjacencyList[vertex2];
        adjacencyList[vertex2] = temp;

        // Update adjacency information for other vertices
        for (int i = 0; i < numVertices; i++) {
            if (i != vertex1 && i != vertex2) {
                boolean tempBit = adjacencyList[i].get(vertex1);
                adjacencyList[i].set(vertex1, adjacencyList[i].get(vertex2));
                adjacencyList[i].set(vertex2, tempBit);
            }
        }
    }

    // Method to order vertices based on their degrees, largest to smallest
    public List<Integer> orderByDegree() {
        List<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            vertices.add(i);
        }
        vertices.sort(Comparator.comparingInt(this::degree).reversed());
        return vertices;
    }

    // Method to order vertices based on their degrees, smallest to largest
    public List<Integer> orderByUpwardsDegree() {
        List<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            vertices.add(i);
        }
        vertices.sort(Comparator.comparingInt(this::degree));
        return vertices;
    }

    // Order vertices based on their relative degree in the given BitSet, smallest
    // to largest
    public List<Integer> orderByRelativeDegree(BitSet vert) {
        List<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            if (vert.get(i)) {
                vertices.add(i);
            }
        }
        vertices.sort((v1, v2) -> (relativeDegree(v1, vert) - relativeDegree(v2, vert)));
        return vertices;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public int getNumEdges() {
        int edges = 0;
        for (int i = 0; i < numVertices; i++) {
            edges += adjacencyList[i].cardinality();
        }
        return edges / 2; // Since the graph is undirected
    }

    // Method to calculate the degree of a vertex
    public int degree(int vertex) {
        return adjacencyList[vertex].cardinality();
    }

    // Method that returns the weight of a vertex
    public int weight(int vertex) {
        return weights.get(vertex);
    }

    // Method to calculate the degree of a vertex in the given BitSet of vertices
    public int relativeDegree(int vertex, BitSet vertices) {
        BitSet check = (BitSet) vertices.clone();
        check.and(adjacencyList[vertex]);
        return check.cardinality();
    }

    public BitSet getAdjacencyBitSet(int vertex) {
        return adjacencyList[vertex];
    }

    public boolean isVertexCover(BitSet vertices) {
        boolean vertexCover = true;
        int id = 0;
        while (vertexCover && id < numVertices) {
            if (!vertices.get(id)) {
                BitSet check = (BitSet) adjacencyList[id].clone();
                check.and(vertices);
                vertexCover = (check.cardinality() == adjacencyList[id].cardinality());
            }
            id++;
        }

        return vertexCover;
    }

    // Reorders the vertices of the graph in the order given by the List parameter
    public void reorderVertices(List<Integer> vertices) {
        BitSet[] newAdjacencyList = new BitSet[vertices.size()];
        List<Integer> weights = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            newAdjacencyList[i] = new BitSet(vertices.size());
        }
        for (int j = 0; j < vertices.size(); j++) {
            int vertexJ = vertices.get(j);
            weights.add(this.weights.get(j));
            for (int k = 0; k < vertices.size(); k++) {
                int vertexK = vertices.get(k);
                if (isAdjacent(vertexJ, vertexK)) {
                    newAdjacencyList[j].set(k);
                    newAdjacencyList[k].set(j);
                }
            }
        }
        this.adjacencyList = newAdjacencyList;
        this.weights = weights;
    }

    public boolean isAdjacent(int i, int j) {
        return adjacencyList[i].get(j);
    }

    /**
     * Set the weights of the vertices to random values between minWeight and
     * maxWeight
     *
     * @param seed      Random seed
     * @param minWeight Lower bound for the random weights (inclusive)
     * @param maxWeight Upper bound for the random weights (inclusive)
     */
    public void setRandomWeights(int seed, int minWeight, int maxWeight) {
        Random random = new Random(seed);
        for (int i = 0; i < numVertices; i++) {
            this.weights.set(i, random.nextInt(maxWeight - minWeight + 1) + minWeight);
        }
    }

    /**
     * Set the weights of the vertices to random values between 1 and 200
     *
     * @param seed Random seed
     */
    public void setRandomWeights(int seed) {
        setRandomWeights(seed, 1, 200);
    }

    /**
     * Get the weight of a vertex
     *
     * @param vertexSet BitSet representing multiple vertices
     * @return Weight of the vertex
     */
    public int getWeight(BitSet vertexSet) {
        int weight = 0;
        for (int i = vertexSet.nextSetBit(0); i >= 0; i = vertexSet.nextSetBit(i + 1)) {
            weight += weights.get(i);
        }
        return weight;
    }

    public static void main(String[] args) {
        BasicGraph graph = new BasicGraph("DIMACS_subset_ascii/gen400_p0.9_65.clq");
        logger.info("Number of vertices: {}", graph.getNumVertices());
        logger.info("Number of edges: {}", graph.getNumEdges());

        logger.info("Adjacency information:");
        for (int i = 0; i < graph.numVertices; i++) {
            logger.debug("Vertex {}: {}", i, graph.adjacencyList[i]);
        }
        logger.info(graph.orderByDegree());
    }
}
