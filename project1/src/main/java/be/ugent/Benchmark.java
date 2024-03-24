package be.ugent;

import be.ugent.algorithms.*;
import be.ugent.benchmark.TestFileDatabase;
import be.ugent.graphs.BasicGraph;
import be.ugent.benchmark.Solution;
import be.ugent.benchmark.SolutionReporter;
import be.ugent.benchmark.Summary;
import be.ugent.util.WeightedVertexCoverAlgorithmInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    private static final boolean RUN_PRICING_METHOD = false;
    private static final boolean RUN_BMWVC = true;
    private static final boolean RUN_FIXED_SET_SEARCH = false;
    private static final boolean RUN_DLSWCC = false;

    // Maximum number of iterations for the inexact algorithms
    private static final int MAX_ITERATIONS = 10_000_000;

    // Random seed for the graph weights
    private static final int RANDOM_SEED = 42;

    // List of algorithms to be run
    private final List<WeightedVertexCoverAlgorithmInitializer> algorithms = new ArrayList<>();

    // List of file paths to be tested
    private final String[] filePaths = {
//            "customgraphs/graph_500_0.01.cwg",
//            "customgraphs/graph_200_0.1.cwg",
//            "customgraphs/graph_200_0.05.cwg",
//            "customgraphs/graph_150_0.05.cwg",
            "customgraphs/graph_100_0.05.cwg",
            "customgraphs/graph_50_0.1.cwg",
            "customgraphs/graph_40_0.5.cwg",
            "customgraphs/graph_20_0.5.cwg",
            "customgraphs/graph_20_0.1.cwg",
            "customgraphs/graph_10_0.5.cwg",
            "customgraphs/graph_5_0.5.cwg",
            "customgraphs/triangle-4.cwg",
            "customgraphs/square-5.cwg",
            "customgraphs/graph3.cwg",


//            "customgraphs/graph_30_0.1.cwg",
//            "customgraphs/graph_30_0.5.cwg",

//            "customgraphs/graph_40_0.05.cwg",
//            "customgraphs/graph_40_0.1.cwg",

//            "customgraphs/graph_50_0.05.cwg",
//            "customgraphs/graph_50_0.5.cwg",

//            "customgraphs/graph_100_0.1.cwg",
//            "customgraphs/graph_100_0.5.cwg",

//            "customgraphs/graph_150_0.1.cwg",
//            "customgraphs/graph_150_0.5.cwg",

//            "customgraphs/graph_200_0.01.cwg",
//            "customgraphs/graph_200_0.5.cwg",

//            "customgraphs/graph_500_0.005.cwg",
//            "customgraphs/graph_500_0.05.cwg",
//            "customgraphs/graph_500_0.1.cwg",
//            "customgraphs/graph_500_0.5.cwg",

//            "customgraphs/graph_1000_0.005.cwg",
//            "customgraphs/graph_1000_0.01.cwg",
//            "customgraphs/graph_1000_0.05.cwg",
//            "customgraphs/graph_1000_0.1.cwg",
//            "customgraphs/graph_1000_0.5.cwg",

//            "customgraphs/graph_2000_0.001.cwg",
//            "customgraphs/graph_2000_0.005.cwg",
//            "customgraphs/graph_2000_0.01.cwg",
//            "customgraphs/graph_2000_0.05.cwg",
//            "customgraphs/graph_2000_0.1.cwg",
//            "customgraphs/graph_2000_0.5.cwg",

//            "customgraphs/graph_5000_5.0E-4.cwg",
//            "customgraphs/graph_5000_0.005.cwg",
//            "customgraphs/graph_5000_0.001.cwg",
//            "customgraphs/graph_5000_0.05.cwg",
//            "customgraphs/graph_5000_0.1.cwg",
//            "customgraphs/graph_5000_0.01.cwg",
//            "customgraphs/graph_5000_0.5.cwg",

//            "customgraphs/graph_10000_5.0E-4.cwg",
//            "customgraphs/graph_10000_0.001.cwg",
//            "customgraphs/graph_10000_0.005.cwg",
//            "customgraphs/graph_10000_0.01.cwg",
//            "customgraphs/graph_10000_0.05.cwg",
//            "customgraphs/graph_10000_0.1.cwg",
//            "customgraphs/graph_10000_0.5.cwg",

//            "customgraphs/graph_15000_5.0E-4.cwg",
//            "customgraphs/graph_15000_0.001.cwg",
//            "customgraphs/graph_15000_0.005.cwg",
//            "customgraphs/graph_15000_0.01.cwg",
//            "customgraphs/graph_15000_0.05.cwg",
//            "customgraphs/graph_15000_0.1.cwg",
//            "customgraphs/graph_15000_0.5.cwg",

//            "customgraphs/graph_20000_1.0E-4.cwg",
//            "customgraphs/graph_20000_5.0E-4.cwg",
//            "customgraphs/graph_20000_0.001.cwg",
//            "customgraphs/graph_20000_0.005.cwg",
//            "customgraphs/graph_20000_0.01.cwg",
//            "customgraphs/graph_20000_0.05.cwg",
//            "customgraphs/graph_20000_0.1.cwg",
//            "customgraphs/graph_20000_0.5.cwg",

//            "customgraphs/graph_25000_1.0E-4.cwg",
//            "customgraphs/graph_25000_5.0E-4.cwg",
//            "customgraphs/graph_25000_0.001.cwg",
//            "customgraphs/graph_25000_0.005.cwg",
//            "customgraphs/graph_25000_0.01.cwg",
//            "customgraphs/graph_25000_0.05.cwg",
//            "customgraphs/graph_25000_0.1.cwg",
//            "customgraphs/graph_25000_0.5.cwg",

//            "customgraphs/graph_50000_5.0E-5.cwg",
//            "customgraphs/graph_50000_1.0E-4.cwg",
    };

    // Map to store the summary for each of the algorithms and files
    private Map<String, List<Summary>> summaries = new HashMap<>();


    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        if (RUN_PRICING_METHOD) {
            benchmark.algorithms.add((int maxVertexCoverSize, int maxIterations) -> new PricingMethod());
        }

        if (RUN_FIXED_SET_SEARCH) {
            benchmark.algorithms.add((int maxVertexCoverSize, int maxIterations) -> new FixedSetSearch());
        }

        if (RUN_DLSWCC) {
            benchmark.algorithms.add((int maxVertexCoverSize, int maxIterations) -> new DLSWCC(maxIterations));

        }

        if (RUN_BMWVC) {
            benchmark.algorithms.add((int maxVertexCoverSize, int maxIterations) -> new BMWVC());
        }


        benchmark.runAlgorithms();
    }

    public void runAlgorithms() {
        SolutionReporter<BitSet> intermediateSolutionReporter = new SolutionReporter<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

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
                                    {
                                        BasicGraph graphCopy = graph.copy();
                                        return algorithmInstance.calculateMinVertexCover(
                                                graphCopy,
                                                solution -> {
//                                                        logger.debug(
//                                                                "Reporting solution for {}, {}",
//                                                                uniqueIdentifier, solution.cardinality());
                                                    intermediateSolutionReporter.reportSolution(
                                                            solution, uniqueIdentifier);
                                                });
                                    });

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


                    int minWeight;
                    String bestSolutionString;
                    if (bestSolution == null) {
                        minWeight = Integer.MAX_VALUE;
                        bestSolutionString = "No solution found";
                    } else {
                        minWeight = graph.getWeight(bestSolution.getValue());
                        bestSolutionString = bestSolution.getValue().cardinality() + "/" + graph.getNumVertices();
                    }

                    summaries.get(key).add(new Summary(executionTime, minWeight));

                    logger.info(
                            "Solution found for {} with algorithm {}: {} vertices with weight {}",
                            filePath,
                            algorithmName,
                            bestSolutionString,
                            minWeight);
                }
            }
        }
        executor.shutdown();

        // Create a map to group the summaries by algorithm and file
        Map<String, Map<String, List<Summary>>> groupedSummaries = new HashMap<>();
        for (Map.Entry<String, List<Summary>> entry : summaries.entrySet()) {
            // Split the key into algorithm and file
            String[] parts = entry.getKey().split("-");
            String algorithm = parts[0];
            String file = parts[1];
            // Add the summaries to the map
            groupedSummaries.putIfAbsent(file, new HashMap<>());
            groupedSummaries.get(file).put(algorithm, entry.getValue());
        }

        // Get the current time in milliseconds
        long startTime = System.currentTimeMillis();
        // Convert the current time to an Instant
        Instant instant = Instant.ofEpochMilli(startTime);
        // Create a DateTimeFormatter with the pattern "yyyy-MM-dd_HH-mm-ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(
                ZoneId.systemDefault());
        // Create the file name
        String fileName = "out/benchmark_summary_" + formatter.format(instant);

        generateSummaryFile(fileName, groupedSummaries);
        generateCSVFile(fileName, groupedSummaries);

    }


    private void generateSummaryFile(String fileName, Map<String, Map<String, List<Summary>>> groupedSummaries) {
        fileName = fileName + ".txt";
        // Create a Path object from the file name
        Path path = Paths.get(fileName);
        try {
            // Create the directories for the file
            Files.createDirectories(path.getParent());
            try (PrintWriter writer = new PrintWriter(fileName)) {

                // Write the summaries to the file
                for (Map.Entry<String, Map<String, List<Summary>>> fileEntry : groupedSummaries.entrySet()) {
                    // Write the name of the file to the file
                    writer.println("File: " + fileEntry.getKey());

                    // Iterate over each file entry in the file's map
                    for (Map.Entry<String, List<Summary>> algorithmEntry : fileEntry.getValue().entrySet()) {
                        // Get the list of summaries for the file
                        List<Summary> summaryList = algorithmEntry.getValue();

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
                        writer.println("\tAlgorithm: " + algorithmEntry.getKey());

                        // Write the mean and standard deviation of the execution time to the file
                        writer.println(
                                "\tTime: " + meanTime + "ms" + String.format(" (±%.2fms)", stdDevTime));

                        // Write the mean, minimum, and maximum weight to the file
                        writer.println("\tWeight:");
                        int optimalWeight = TestFileDatabase.getExpectedResult(algorithmEntry.getKey());
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

    private void generateCSVFile(String fileName, Map<String, Map<String, List<Summary>>> groupedSummaries) {
        int partsPerAlgorithm = 5;

        fileName = fileName + ".csv";
        Path path = Paths.get(fileName);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        DecimalFormat df = (DecimalFormat) nf;
        try {
            Files.createDirectories(path.getParent());
            try (PrintWriter writer = new PrintWriter(fileName)) {
                String[] headerParts = new String[partsPerAlgorithm * algorithms.size() + 1];
                headerParts[0] = "File";
                for (int i = 0; i < algorithms.size(); i++) {
                    String algorithmName = algorithms.get(i).initialize(0, 0).getClass().getSimpleName();
                    headerParts[i * partsPerAlgorithm + 1] = algorithmName + "Mean Execution Time";
                    headerParts[i * partsPerAlgorithm + 2] = algorithmName + "Execution Time StdDev";
                    headerParts[i * partsPerAlgorithm + 3] = algorithmName + "Mean Weight";
                    headerParts[i * partsPerAlgorithm + 4] = algorithmName + "Min Weight";
                    headerParts[i * partsPerAlgorithm + 5] = algorithmName + "Max Weight";
                }
                writer.println(
                        String.join(",", headerParts));
                for (Map.Entry<String, Map<String, List<Summary>>> fileEntry : groupedSummaries.entrySet()) {
                    String[] parts = new String[partsPerAlgorithm * algorithms.size() + 1];
                    int algorithmIndex = 0;
                    for (Map.Entry<String, List<Summary>> algorithmEntry : fileEntry.getValue().entrySet()) {
                        parts[0] = fileEntry.getKey();
                        List<Summary> summaryList = algorithmEntry.getValue();
                        double meanTime = summaryList.stream().mapToLong(Summary::getExecutionTime).average().orElse(
                                0.0);
                        double stdDevTime = Math.sqrt(summaryList.stream().mapToDouble(
                                s -> ((s.getExecutionTime() - meanTime) * (s.getExecutionTime() - meanTime))).average().orElse(
                                0.0));
                        int minWeight = summaryList.stream().mapToInt(Summary::getWeight).min().orElse(0);
                        double meanWeight = summaryList.stream().mapToInt(Summary::getWeight).average().orElse(0.0);
                        int maxWeight = summaryList.stream().mapToInt(Summary::getWeight).max().orElse(0);

                        parts[algorithmIndex * partsPerAlgorithm + 1] = df.format(meanTime);
                        parts[algorithmIndex * partsPerAlgorithm + 2] = df.format(stdDevTime);
                        parts[algorithmIndex * partsPerAlgorithm + 3] = df.format(meanWeight);
                        parts[algorithmIndex * partsPerAlgorithm + 4] = df.format(minWeight);
                        parts[algorithmIndex * partsPerAlgorithm + 5] = df.format(maxWeight);
                        algorithmIndex++;
                    }
                    writer.println(String.join(",", parts));
                }
            } catch (FileNotFoundException e) {
                logger.error(e);
                System.exit(1);
            }
        } catch (
                IOException e) {
            logger.error(e);
            System.exit(1);
        }
    }


}
