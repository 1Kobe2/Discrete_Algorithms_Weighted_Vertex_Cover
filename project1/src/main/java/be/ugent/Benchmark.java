package be.ugent;

import be.ugent.algorithms.BMWVC;
import be.ugent.algorithms.FixedSetSearch;
import be.ugent.algorithms.PricingMethod;
import be.ugent.algorithms.WeightedVertexCoverAlgorithm;
import be.ugent.benchmark.TestFileDatabase;
import be.ugent.graphs.BasicGraph;
import be.ugent.benchmark.Solution;
import be.ugent.benchmark.SolutionReporter;
import be.ugent.benchmark.Summary;
import be.ugent.util.WeightedVertexCoverAlgorithmInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class Benchmark {

    private static final Logger logger = LogManager.getLogger(Benchmark.class);

    // Timeout in minutes for each test run
    private static final long TIMEOUT = 30;

    // Number of test runs for each algorithm and file
    private static final int TEST_RUNS = 5;

    // Flags to enable or disable the different algorithms
    private static final boolean RUN_PRICING_METHOD = true;
    private static final boolean RUN_BMWVC = false;
    private static final boolean RUN_FIXED_SET_SEARCH = false;
    private static final boolean RUN_INEXACT_2 = false;

    // Maximum number of iterations for the inexact algorithms
    private static final int MAX_ITERATIONS = 10_000_000;

    // Random seed for the graph weights
    private static final int RANDOM_SEED = 42;

    // List of algorithms to be run
    private final List<WeightedVertexCoverAlgorithmInitializer> algorithms = new ArrayList<>();

    // List of file paths to be tested
    private final String[] filePaths = {
            "customgraphs/triangle-4.cwg",
            "customgraphs/graph_0_0.1.cwg",
            "customgraphs/graph_10_0.1.cwg",
            "customgraphs/graph_20_0.02.cwg",
            "customgraphs/graph_40_0.02.cwg",
            "customgraphs/graph_40_0.05.cwg",
            "customgraphs/graph_100_0.01.cwg",
            "customgraphs/graph_100_0.02.cwg",
            "customgraphs/graph_100_0.05.cwg",
            "customgraphs/graph_100_0.1.cwg",
            "customgraphs/graph_100_0.15.cwg",
            "customgraphs/graph_100_0.2.cwg",
            "customgraphs/graph_100_0.25.cwg",
            "customgraphs/graph_100_0.5.cwg",
            "customgraphs/graph_100_0.75.cwg",
            "customgraphs/graph_100_0.9.cwg",
            "customgraphs/graph_100_0.95.cwg",
            "customgraphs/graph_100_0.99.cwg",
            "customgraphs/graph_500_0.01.cwg",
            "customgraphs/graph_500_0.1.cwg",
            "customgraphs/graph_500_0.5.cwg",
            "customgraphs/graph_500_0.9.cwg",
            "customgraphs/graph_100000_1.0E-4.cwg",
    };

    // Map to store the summary for each of the algorithms and files
    private Map<String, List<Summary>> summaries = new HashMap<>();


    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        if (RUN_PRICING_METHOD) {
            benchmark.algorithms.add((int maxVertexCoverSize, int maxIterations) -> new PricingMethod());
        }

        if (RUN_BMWVC) {
            benchmark.algorithms.add((int maxVertexCoverSize, int maxIterations) -> new BMWVC());
        }

        if (RUN_FIXED_SET_SEARCH) {
            benchmark.algorithms.add(
                    (int maxVertexCoverSize, int maxIterations) -> new FixedSetSearch()
            );

        }

        if (RUN_INEXACT_2) {
            // TODO: Add second inexact algorithm
            //      benchmark.algorithms.add(
            //          (int maxVertexCoverSize, int maxIterations) -> new BMWVC()
            //      );

        }

        benchmark.runAlgorithms();
    }

    public void runAlgorithms() {
        SolutionReporter<BitSet> intermediateSolutionReporter = new SolutionReporter<>();
        ExecutorService executor = Executors.newCachedThreadPool();

        for (WeightedVertexCoverAlgorithmInitializer algorithm : this.algorithms) {

            String algorithmName = algorithm.initialize(0, 0).getClass().getSimpleName();
            for (String filePath : this.filePaths) {
                String key = algorithmName + "-" + filePath;
                summaries.putIfAbsent(key, new ArrayList<>());

                BasicGraph graph = new BasicGraph(filePath);
                for (int i = 0; i < TEST_RUNS; i++) {
                    String uniqueIdentifier = String.format("%s-%s-%d", algorithmName, filePath, i);
                    long startTime = System.currentTimeMillis(); // Record start time

                    WeightedVertexCoverAlgorithm algorithmInstance = algorithm.initialize(
                            TestFileDatabase.getExpectedResult(filePath),
                            MAX_ITERATIONS);
                    Future<BitSet> future =
                            executor.submit(
                                    () ->
                                            algorithmInstance.calculateMinVertexCover(
                                                    graph,
                                                    solution -> {
                                                        logger.info(
                                                                "Reporting solution for {}, {}",
                                                                uniqueIdentifier, solution);
                                                        intermediateSolutionReporter.reportSolution(
                                                                solution, uniqueIdentifier);
                                                    }));

                    long executionTime = 0;
                    try {
                        BitSet optimalSolution = future.get(TIMEOUT, TimeUnit.MINUTES);
                        long endTime = System.currentTimeMillis(); // Record end time
                        executionTime = endTime - startTime;

                        intermediateSolutionReporter.reportSolution(optimalSolution, uniqueIdentifier, true);

                        logger.info("Processed file {},\ttime: {} ms", filePath, executionTime);
                    } catch (TimeoutException e) {
                        logger.warn("Processing of file {} timed out.", filePath);
                        future.cancel(true); // Cancel the task
                        executionTime = TIMEOUT * 60 * 1000; // Set execution time to timeout
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e); // Handle other exceptions
                    }

                    Solution<BitSet> bestSolution = intermediateSolutionReporter.getSolution(uniqueIdentifier);

                    int minWeight = graph.getWeight(bestSolution.getValue());

                    summaries.get(key).add(new Summary(executionTime, minWeight));

                    logger.info(
                            "Solution found for {} with algorithm {}: {} with weight {} in ",
                            filePath,
                            algorithmName,
                            bestSolution,
                            minWeight);
                }
            }
        }
        executor.shutdown();
        generateSummaryFile();

    }


    private void generateSummaryFile() {
        // Get the current time in milliseconds
        long startTime = System.currentTimeMillis();
        // Convert the current time to an Instant
        Instant instant = Instant.ofEpochMilli(startTime);
        // Create a DateTimeFormatter with the pattern "yyyy-MM-dd_HH-mm-ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(
                ZoneId.systemDefault());
        // Create the file name
        String fileName = "out/benchmark_summary_" + formatter.format(instant) + ".txt";
        // Create a Path object from the file name
        Path path = Paths.get(fileName);
        try {
            // Create the directories for the file
            Files.createDirectories(path.getParent());
            try (PrintWriter writer = new PrintWriter(new File(fileName))) {
                // Create a map to group the summaries by algorithm and file
                Map<String, Map<String, List<Summary>>> groupedSummaries = new HashMap<>();
                for (Map.Entry<String, List<Summary>> entry : summaries.entrySet()) {
                    // Split the key into algorithm and file
                    String[] parts = entry.getKey().split("-");
                    String algorithm = parts[0];
                    String file = parts[1];
                    // Add the summaries to the map
                    groupedSummaries.putIfAbsent(algorithm, new HashMap<>());
                    groupedSummaries.get(algorithm).put(file, entry.getValue());
                }

                // Write the summaries to the file
                for (Map.Entry<String, Map<String, List<Summary>>> algorithmEntry : groupedSummaries.entrySet()) {
                    // Write the name of the algorithm to the file
                    writer.println("Algorithm: " + algorithmEntry.getKey());

                    // Iterate over each file entry in the algorithm's map
                    for (Map.Entry<String, List<Summary>> fileEntry : algorithmEntry.getValue().entrySet()) {
                        // Get the list of summaries for the file
                        List<Summary> summaryList = fileEntry.getValue();

                        // Calculate the mean execution time for the summaries
                        double meanTime = summaryList.stream().mapToLong(Summary::getExecutionTime).average().orElse(
                                0.0);

                        // Calculate the standard deviation of the execution time for the summaries
                        double stdDevTime = Math.sqrt(summaryList.stream().mapToDouble(
                                s -> ((s.getExecutionTime() - meanTime) * (s.getExecutionTime() - meanTime))).average().orElse(
                                0.0));

                        // Get the minimum weight from the summaries
                        int minWeight = summaryList.stream().mapToInt(Summary::getWeight).min().orElse(0);
                        int minWeightCount = (int) summaryList.stream().filter(s -> s.getWeight() == minWeight).count();

                        // Calculate the mean weight for the summaries
                        double meanWeight = summaryList.stream().mapToInt(Summary::getWeight).average().orElse(0.0);

                        // Get the maximum weight from the summaries
                        int maxWeight = summaryList.stream().mapToInt(Summary::getWeight).max().orElse(0);
                        int maxWeightCount = (int) summaryList.stream().filter(s -> s.getWeight() == maxWeight).count();

                        // Write the file name to the file
                        writer.println("\tFile: " + fileEntry.getKey());

                        // Write the mean and standard deviation of the execution time to the file
                        writer.println(
                                "\tTime: " + meanTime + "ms" + String.format(" (±%.2fms)", stdDevTime));

                        // Write the mean, minimum, and maximum weight to the file
                        writer.println("\tWeight:");
                        int optimalWeight = TestFileDatabase.getExpectedResult(fileEntry.getKey());
                        if (optimalWeight != Integer.MAX_VALUE) {
                            writer.println("\t\toptimal value:\t" + optimalWeight);
                        } else {
                            writer.println("\t\toptimal value:\t" + "unknown");
                        }
                        writer.println("\t\tmean value:\t\t" + meanWeight);
                        writer.println("\t\tmin value:\t\t" + minWeight + "\t(" + minWeightCount + " times)");
                        writer.println("\t\tmax value:\t\t" + maxWeight + "\t(" + maxWeightCount + " times)");


                        // Write a new line to the file
                        writer.println();
                    }
                }
            } catch (FileNotFoundException e) {
                logger.error(e);
                System.exit(1);
            }
        } catch (IOException e) {
            logger.error(e);
            System.exit(1);
        }
    }


}
