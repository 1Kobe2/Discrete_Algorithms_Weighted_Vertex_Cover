package be.ugent.graphs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class BasicGraph {

    private static final Logger logger = LogManager.getLogger(BasicGraph.class);

    // made with chatGPT

    protected BitSet[] adjacencyList;
    protected int numVertices;
    protected int numEdges;
    protected List<Integer> weights;

    public BasicGraph(BasicGraph graph) {
        this.adjacencyList = graph.adjacencyList;
        this.numVertices = graph.numVertices;
        this.weights = graph.weights;
        this.numVertices = this.getNumVertices();
    }

    public BasicGraph(BitSet[] adjacencyList, List<Integer> weights) {
        this.adjacencyList = adjacencyList;
        this.numVertices = adjacencyList.length;
        this.weights = weights;
        this.numVertices = this.getNumVertices();
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
                if (graphFilename.endsWith(".clq")) {
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
                            expectedNumberOfEdges =
                                    Integer.parseInt(st.nextToken()); // Store the expected number of edges
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
                    this.numEdges = this.calculateNumberOfEdges();
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
                } else if (graphFilename.endsWith(".cwg")) {
                    boolean isEdgeSection = false;
                    boolean isWeightSection = false;
                    boolean isConfigSection = false;
                    int vertexCounter = 0;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && line.charAt(0) != '#') {
                            if (line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']' && line.length() > 2) {
                                switch (line) {
                                    case "[edges]" -> {
                                        isEdgeSection = true;
                                        isWeightSection = false;
                                        isConfigSection = false;
                                    }
                                    case "[weights]" -> {
                                        isWeightSection = true;
                                        isEdgeSection = false;
                                        isConfigSection = false;
                                    }
                                    case "[configuration]" -> {
                                        isConfigSection = true;
                                        isEdgeSection = false;
                                        isWeightSection = false;
                                    }
                                    default -> {
                                        // Do nothing
                                    }
                                }
                            } else {

                                if (isConfigSection) {
                                    if (line.startsWith("vertices")) {
                                        this.numVertices = Integer.parseInt(line.split(" ")[1]);
                                        adjacencyList = new BitSet[this.numVertices];
                                        for (int i = 0; i < this.numVertices; i++) {
                                            adjacencyList[i] = new BitSet(this.numVertices);
                                        }
                                        weights = new ArrayList<>(Collections.nCopies(numVertices, 1));
                                    } else if (line.startsWith("edges")) {
                                        this.numEdges = Integer.parseInt(line.split(" ")[1]);
                                    }
                                } else if (isWeightSection) {
                                    if (weights == null) {
                                        throw new IllegalStateException(
                                                "Weights section should come after the configuration section");
                                    }
                                    if (vertexCounter >= this.numVertices) {
                                        throw new IllegalStateException(
                                                "Number of weights exceeds the number of vertices");
                                    }
                                    this.weights.set(vertexCounter, Integer.parseInt(line));
                                    vertexCounter++;
                                } else if (isEdgeSection) {
                                    String[] vertices = line.split(" ");
                                    int source = Integer.parseInt(vertices[0]);
                                    int destination = Integer.parseInt(vertices[1]);
                                    this.addEdge(source, destination);
                                }
                            }
                        }

                    }
                    this.numEdges = this.calculateNumberOfEdges();
                } else if (graphFilename.endsWith(".mtx")) {
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        // Skip comment lines
                        if (line.isEmpty() || line.charAt(0) == '%') {
                            continue;
                        }
                        // Process problem line
                        String[] parts = line.split(" ");
                        if (parts.length == 3) { // This line contains the number of vertices and edges
                            this.numVertices = Integer.parseInt(parts[0]);
                            // Initialize graph with the number of vertices
                            adjacencyList = new BitSet[this.numVertices];
                            for (int i = 0; i < this.numVertices; i++) {
                                adjacencyList[i] = new BitSet(this.numVertices);
                            }
                            weights = new ArrayList<>(Collections.nCopies(numVertices, 1));
                        } else if (parts.length == 2) { // This line contains an edge
                            int source = Integer.parseInt(parts[0]) - 1; // MTX vertices start from 1
                            int destination = Integer.parseInt(parts[1]) - 1; // MTX vertices start from 1
                            addEdge(source, destination);
                        }
                    }
                    this.numEdges = this.calculateNumberOfEdges();

                } else {
                    logger.error("Error: Unsupported file format.");
                    logger.error("Exiting...");
                    System.exit(1);
                }
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
        if (!adjacencyList[source].get(destination)) {
            numEdges++; // Increment the count when a new edge is added
        }

        adjacencyList[source].set(destination);
        adjacencyList[destination].set(source); // Since it's an undirected graph
    }

    public boolean hasEdge(int source, int destination) {
        return adjacencyList[source].get(destination);
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

    // Order vertices based on their relative degree in the given BitSet, smallest to largest
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

    public int calculateNumberOfEdges() {
        int edges = 0;
        for (int i = 0; i < numVertices; i++) {
            edges += adjacencyList[i].cardinality();
        }
        return edges / 2; // Since the graph is undirected
    }

    public int getNumEdges() {
        return this.numEdges;
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
     * Set the weights of the vertices to random values between minWeight and maxWeight
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
     * Get the weight of a vertexSet
     *
     * @param vertexSet BitSet representing multiple vertices
     * @return Weight of the vertexSet
     */
    public int getWeight(BitSet vertexSet) {
        int weight = 0;
        for (int i = vertexSet.nextSetBit(0); i >= 0; i = vertexSet.nextSetBit(i + 1)) {
            weight += weights.get(i);
        }
        return weight;
    }

    /**
     * Get the weight of a vertexS
     *
     * @param vertex Index of the vertex
     * @return Weight of the vertex
     */
    public int getWeight(int vertex) {
        return weights.get(vertex);
    }

    public void exportToCWG(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(
                "out/graphs/" + filename
        ))) {
            // Write configuration
            writer.write("[configuration]\n");
            writer.write("vertices " + numVertices + "\n");
            writer.write("edges " + numEdges + "\n");

            // Write weights
            writer.write("\n[weights]\n");
            for (int i = 0; i < numVertices; i++) {
                writer.write(weights.get(i) + "\n");
            }

            // Write edges
            writer.write("\n[edges]\n");
            for (int i = 0; i < numVertices; i++) {
                BitSet adjacency = adjacencyList[i];
                for (int j = adjacency.nextSetBit(0); j >= 0; j = adjacency.nextSetBit(j + 1)) {
                    if (i < j) { // To avoid duplicate edges
                        writer.write(i + " " + j + "\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
