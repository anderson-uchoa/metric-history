package ch.thomsch.example;

import java.io.IOException;

import ch.thomsch.MetricHistory;
import ch.thomsch.export.Reporter;
import ch.thomsch.loader.RefactoringMiner;
import ch.thomsch.metric.CKMetrics;
import ch.thomsch.versioncontrol.GitRepository;

/**
 * @author Thomsch
 */
public final class ToyExample {

    private static final String REVISION_FILE = "../data/revisions/toy-example.csv";
    private static final String REPOSITORY = "../mined-repositories/refactoring-toy-example";
    private static final String RESULTS_FILE = "../data/toy-refactorings-metrics.csv";

    public static void main(String[] args) throws IOException {
        new MetricHistory(new CKMetrics(), new Reporter(), new RefactoringMiner())
                .collect(REVISION_FILE, GitRepository.get(REPOSITORY), RESULTS_FILE);
    }
}
