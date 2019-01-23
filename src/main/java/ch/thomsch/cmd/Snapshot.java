package ch.thomsch.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import ch.thomsch.mining.Analyzer;
import ch.thomsch.mining.Collector;
import ch.thomsch.mining.SourceMeter;
import ch.thomsch.mining.SourceMeterConverter;
import ch.thomsch.versioncontrol.GitVCS;
import picocli.CommandLine;

/**
 * Analyze and build the RAW file for a single version of the project.
 */
@CommandLine.Command(
        name = "snapshot",
        description = "Analyze and build the RAW file for a single version of the project.\nOutput: commitId.csv with collected metrics inside the <output dir>")
public class Snapshot extends Command {

    private static final Logger logger = LoggerFactory.getLogger(Snapshot.class);

    @CommandLine.Parameters(description = "Commit id of the project revision to be analyzed.")
    private String commitId;

    @CommandLine.Parameters(description = "Path to the executable to collect metrics.")
    private String executable;

    @CommandLine.Parameters(description = "Path to the folder containing the source code or the project.")
    private String project;

    @CommandLine.Parameters(description = "Path to the folder containing .git folder. It can also be set to 'same' if it's the same as <project path>.")
    private String repository;

    @CommandLine.Parameters(description = "Path to the folder where the results should be extracted.")
    private String executableOutput;

    @CommandLine.Parameters(description = "Name of the project.")
    private String projectName;

    @Override
    public void run() {
        executable = normalizePath(executable);
        project = normalizePath(project);

        if (repository.equalsIgnoreCase("same")) {
            repository = project;
        } else {
            repository = normalizePath(repository);
        }

        executableOutput = normalizePath(executableOutput);

        try {
            execute();
        } catch (Exception e) {
            logger.error("An error occurred:", e);
        }
    }

    @Override
    public void execute() throws Exception {
        final Analyzer analyzer = new SourceMeter(executable, executableOutput, projectName, project);
        final GitVCS vcs = GitVCS.get(repository);
        final Collector collector = new Collector(analyzer, vcs);

        final String outputFilePath = executableOutput + File.separator + commitId + ".csv";
        final String collectorOutputDirectory = executableOutput + File.separator + projectName;

        logger.info("Output file: {}", outputFilePath);
        logger.info("Processing single revision {}", commitId);

        final long beginning = System.nanoTime();
        try {
            collector.analyzeRevision(commitId, project);
            vcs.close();

            SourceMeterConverter.convert(collectorOutputDirectory, outputFilePath);
        } catch (IOException e) {
            logger.error("Resource access problem", e);
        } finally {
            final long elapsed = System.nanoTime() - beginning;
            logger.info("Snapshot completed in {}", Duration.ofNanos(elapsed));
        }
    }
}
