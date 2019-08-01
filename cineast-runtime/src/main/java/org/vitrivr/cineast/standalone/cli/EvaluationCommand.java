package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.evaluation.EvaluationException;
import org.vitrivr.cineast.standalone.evaluation.EvaluationRuntime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * TODO: What does this thing do exactly?
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "evaluation", description = "...")
public class EvaluationCommand implements Runnable {
    @Option(name = { "-i", "--input" }, description = "Path to the evaluation config file used for evaluation.")
    private String input;

    @Override
    public void run() {
        final Path path = Paths.get(this.input);
        try {
            final EvaluationRuntime runtime = new EvaluationRuntime(path, Config.sharedConfig().getDatabase());
            runtime.call();
        } catch (IOException e) {
            System.err.println(String.format("Could not start evaluation with configuration file '%s' due to a IO error.", path.toString()));
            e.printStackTrace();
        } catch (EvaluationException e) {
            System.err.println(String.format("Something went wrong during the evaluation with '%s'.", path.toString()));
            e.printStackTrace();
        }
    }
}
