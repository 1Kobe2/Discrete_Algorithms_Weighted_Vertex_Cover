package be.ugent;

import be.ugent.graphs.BasicGraph;
import be.ugent.util.WeightedVertexCoverAlgorithmInitializer;
import be.ugent.util.TestFileDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.BitSet;
import java.util.concurrent.*;

public class Benchmark {

    private static Logger logger = LogManager.getLogger(Benchmark.class);
    private static TestFileDatabase testFileDatabase = new TestFileDatabase();


    public static void main(String[] args) {
        String[] files = {

        };

        WeightedVertexCoverAlgorithmInitializer[] algorithms = {

        };
        Benchmark benchmark = new Benchmark();
        benchmark.process(algorithms, files);
    }

    public BitSet processFile(WeightedVertexCoverAlgorithm algorithm, String fileName) {
        BasicGraph graph = new BasicGraph(fileName);
        return algorithm.calculateMinVertexCover(graph);
    }

    public void process(WeightedVertexCoverAlgorithmInitializer[] algorithms, String[] fileNames) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        for (WeightedVertexCoverAlgorithmInitializer alg : algorithms) {
            logger.info("\n\n Now doing: " + alg.initialize(0).toString());
            for (String fileName : fileNames) {
                long startTime = System.currentTimeMillis(); // Record start time
                Future<BitSet> future = executor.submit(() ->
                        processFile(alg.initialize(Benchmark.testFileDatabase.getMaxClique(fileName)), fileName)
                );

                try {
                    BitSet maxClique = future.get(30, TimeUnit.MINUTES);
                    long endTime = System.currentTimeMillis(); // Record end time
                    logger.info("Processed file {},\tsize: {},\ttime: {} ms", fileName, maxClique.cardinality(),
                            (endTime - startTime));
                } catch (TimeoutException e) {
                    logger.warn("Processing of file {} timed out. Moving to the next file.", fileName);
                    future.cancel(true); // Cancel the task
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e); // Handle other exceptions
                }
            }
        }
        executor.shutdown();
    }
}
