package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.vitrivr.cineast.standalone.importer.handlers.*;
import org.vitrivr.cineast.standalone.importer.vbs2019.AudioTranscriptImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.CaptionTextImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.GoogleVisionImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.TagImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.gvision.GoogleVisionCategory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A CLI command that can be used to start import of pre-extracted data.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "import", description = "Starts import of pre-extracted data.")
public class ImportCli extends CineastCli {

    @Option(name = { "-t", "--type" }, description = "Type of data import that should be started. Possible values are PROTO, JSON, ASR, OCR, CAPTION, AUDIO, TAGS and VISION.")
    private String type;

    @Option(name = { "-i", "--input" }, description = "The source file or folder for data import. If a folder is specified, the entire content will be considered for import.")
    private String input;

    @Option(name = { "-b", "--batchsize" }, description = "The batch size used for the import. Imported data will be persisted in batches of the specified size.")
    private int batchsize = 500;

    @Override
    public void run() {
        super.run();
        System.out.println(String.format("Starting import of type %s for '%s'.", this.type.toString(), this.input));
        final Path path = Paths.get(this.input);
        final ImportType type = ImportType.valueOf(this.type.toUpperCase());
        DataImportHandler handler;
        switch (type) {
            case PROTO:
                handler = new ProtoDataImportHandler(2, this.batchsize);
                handler.doImport(path);
                break;
            case JSON:
                handler = new JsonDataImportHandler(2, this.batchsize);
                handler.doImport(path);
                break;
            case ASR:
                handler = new AsrDataImportHandler(1, this.batchsize);
                handler.doImport(path);
                break;
            case OCR:
                handler = new OcrDataImportHandler(1, this.batchsize);
                handler.doImport(path);
                break;
            case CAPTION:
                handler = new CaptionTextImportHandler(1, this.batchsize);
                handler.doImport(path);
                break;
            case AUDIO:
                handler = new AudioTranscriptImportHandler(1, this.batchsize);
                handler.doImport(path);
                break;
            case TAGS:
                handler = new TagImportHandler(1, this.batchsize);
                handler.doImport(path);
                break;
            case VISION:
                List<GoogleVisionImportHandler> handlers = new ArrayList<>();
                for (GoogleVisionCategory category : GoogleVisionCategory.values()) {
                    GoogleVisionImportHandler _handler = new GoogleVisionImportHandler(1, batchsize, category, false);
                    _handler.doImport(path);
                    handlers.add(_handler);
                    if (category == GoogleVisionCategory.LABELS || category == GoogleVisionCategory.WEB) {
                        GoogleVisionImportHandler _handlerTrue = new GoogleVisionImportHandler(1, batchsize, category, true);
                        _handlerTrue.doImport(path);
                        handlers.add(_handlerTrue);
                    }
                }
                handlers.forEach(GoogleVisionImportHandler::waitForCompletion);
                break;
        }
        System.out.println(String.format("Completed import of type %s for '%s'.", this.type.toString(), this.input));
    }

    /**
     * Enum of the available types of data imports.
     */
    private enum ImportType {
        PROTO,
        JSON,
        ASR,
        OCR,
        CAPTION,
        AUDIO,
        TAGS,
        VISION
    }
}
