package be.ugent;

import be.ugent.graphs.BasicGraph;


import be.ugent.util.TestFileDatabase;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.BitSet;


public class Main {
	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {

		Option algorithmOption = Option.builder()
				.argName("algorithm")
				.hasArg()
				.required()
				.option("a")
				.longOpt("algorithm")
				.desc("Specify which algorithm should be used to find the maximum clique").build();

		Option filesOption = Option.builder()
				.argName("")
				.hasArg()
				.required()
				.option("f")
				.longOpt("files")
				.desc("Comma-separated list of filenames to be run").build();

		Option maxIterOption = Option.builder()
				.argName("max")
				.hasArg()
				.required()
				.longOpt("max")
				.desc("Maximum amount of iterations").build();

		Options options = new Options();
		options.addOption(algorithmOption);
		options.addOption(filesOption);
		options.addOption(maxIterOption);

		HelpFormatter formatter = new HelpFormatter();


		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			logger.error("Parsing failed.  Reason: {}", e.getMessage());
			formatter.printHelp("ant", options);
			logger.error("Exiting...");
			System.exit(1);
		}

		String[] filePaths = new String[]{};
		String algorithm;
		WeightedVertexCoverAlgorithm weightedVertexCoverAlgorithm = null;

		if (cmd.hasOption("f") && cmd.getOptionValues("f").length > 0) {
			filePaths = cmd.getOptionValues("f");
		} else {
			logger.error("You must specify at least one file to run the algorithm on, using the -f option");
			formatter.printHelp("ant", options);
			logger.error("Exiting...");
			System.exit(1);
		}
		TestFileDatabase testFileDatabase = new TestFileDatabase();

		if (cmd.hasOption("a")) {
			algorithm = cmd.getOptionValue("a");
			switch (algorithm) {
				default:
					logger.error("Invalid algorithm specified");
					logger.error("Exiting...");
					System.exit(1);
			}
		} else {
			logger.error("No algorithm specified");
			formatter.printHelp("ant", options);
			System.exit(1);
		}
		for (String filePath : filePaths) {
			logger.info("Reading graph from file: {}", filePath);
			BasicGraph graph = new BasicGraph(filePath);
			logger.info("Running minimum vertex cover algorithm");
			BitSet minVertexCover = weightedVertexCoverAlgorithm.calculateMinVertexCover(graph);
			logger.info("Minimum vertex cover: {}", minVertexCover);
			logger.info("Is minimum vertex cover a vertex cover: {}", graph.isVertexCover(minVertexCover));
			logger.info("Size of minimum vertex cover: {}", minVertexCover.cardinality());
		}
	}

}
