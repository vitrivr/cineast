package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import org.vitrivr.cineast.core.db.DataSource;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.JsonDataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.LIREImportHandler;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A CLI command that can be used to start import of pre-extracted data.
 */
@Command(name = "import", description = "Starts import of pre-extracted data.")
public class ImportCommand implements Runnable {

    @Required
    @Option(name = {"-t", "--type"}, description = "Type of data import that should be started.")
    private String type;

    @Required
    @Option(name = {"-i", "--input"}, description = "The source file or folder for data import. If a folder is specified, the entire content will be considered for import.")
    private String input;

    @Option(name = {"--threads"}, description = "Level of parallelization for import")
    private int threads = 2;

    @Option(name = {"-b", "--batchsize"}, description = "The batch size used for the import. Imported data will be persisted in batches of the specified size.")
    private int batchsize = 500;

    @Option(name = {"-c", "--clean"}, description = "Cleans, i.e. drops the tables before import. Use with caution, as the already imported data will be lost! Requires the import type to respect this option")
    private boolean clean = false;

    @Option(name = {"--no-finalize"}, title = "Do Not Finalize", description = "If this flag is not set, automatically rebuilds indices & optimizes all entities when writing to cottontail after the import. Set this flag when you want more performance with external parallelism.")
    private boolean doNotFinalize = false;

    @Option(name = {"--no-transactions"}, title = "Do Not Use Transactions", description = "If this flag is not set, the default behavior is used which means transactions are enabled during import. Set this flag when you want more performance and manage transactional aspects yourself.")
    private boolean noTransactions = false;

    @Override
    public void run() {
        System.out.printf("Starting import of type %s for '%s'. Batchsize %d, %d threads. Clean %b, no-finalize %b .%n", this.type, this.input, this.batchsize, this.threads, this.clean, this.doNotFinalize);
        final Path path = Paths.get(this.input);
        if (noTransactions) {
            Config.sharedConfig().getDatabase().setUseTransactions(false);
        }
        final ImportType type = ImportType.valueOf(this.type.toUpperCase());
        DataImportHandler handler = null;
        switch (type) {
            case JSON:
                handler = new JsonDataImportHandler(this.threads, this.batchsize);
                break;
            case LIRE:
                handler = new LIREImportHandler(this.threads, this.batchsize);
                break;
        }
        if (handler == null) {
            throw new RuntimeException("Cannot do import as the handler was not properly registered. Import type: " + type);
        } else {
            handler.doImport(path);
            handler.waitForCompletion();
        }

        /* Only attempt to optimize Cottontail entities if we were importing into Cottontail, otherwise an unavoidable error message would be displayed when importing elsewhere. */
        if (!doNotFinalize && Config.sharedConfig().getDatabase().getSelector() == DataSource.COTTONTAIL && Config.sharedConfig().getDatabase().getWriter() == DataSource.COTTONTAIL) {
            OptimizeEntitiesCommand.optimizeAllCottontailEntities();
        }

        System.out.printf("Completed import of type %s for '%s'.%n", this.type, this.input);
    }

    /**
     * Enum of the available types of data imports.
     */
    private enum ImportType {
        JSON, LIRE
    }
}
