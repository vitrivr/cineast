package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.rvesse.airline.annotations.restrictions.Required;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.AsrDataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.JsonDataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.LIREImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.OcrDataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.ProtoDataImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.CaptionImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.LSCAllTagsImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.MetaImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.OCRImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.ProcessingMetaImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.VisualConceptTagImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.AudioTranscriptImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.CaptionTextImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.GoogleVisionImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.MLTFeaturesImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.ObjectMetadataImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.TagImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.gvision.GoogleVisionCategory;
import org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis.ClassificationsImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis.ColorlabelImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis.FacesImportHandler;

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

  @Override
  public void run() {
    System.out.println(String.format("Starting import of type %s for '%s'.", this.type, this.input));
    final Path path = Paths.get(this.input);
    final ImportType type = ImportType.valueOf(this.type.toUpperCase());
    DataImportHandler handler = null;
    boolean isGoogleVision = false;
    switch (type) {
      case PROTO:
        handler = new ProtoDataImportHandler(this.threads, this.batchsize);
        break;
      case JSON:
        handler = new JsonDataImportHandler(this.threads, this.batchsize);
        break;
      case LIRE:
        handler = new LIREImportHandler(this.threads, this.batchsize);
        break;
      case ASR:
        handler = new AsrDataImportHandler(this.threads, this.batchsize);
        break;
      case OCR:
        handler = new OcrDataImportHandler(this.threads, this.batchsize);
        break;
      case CAPTIONING:
        handler = new CaptionTextImportHandler(this.threads, this.batchsize);
        break;
      case AUDIO:
        handler = new AudioTranscriptImportHandler(this.threads, this.batchsize);
        break;
      case TAGS:
        handler = new TagImportHandler(this.threads, this.batchsize);
        break;
      case METADATA:
        handler = new ObjectMetadataImportHandler(this.threads, this.batchsize);
        break;
      case AUDIOTRANSCRIPTION:
        handler = new AudioTranscriptImportHandler(this.threads, 15_000);
        break;
      case GOOGLEVISION:
        doVisionImport(path);
        isGoogleVision = true;
        break;
      case V3C1CLASSIFICATIONS:
        handler = new ClassificationsImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case V3C1COLORLABELS:
        /* Be aware that this is metadata which might already be comprised in merged vbs metadata */
        handler = new ColorlabelImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case V3C1FACES:
        handler = new FacesImportHandler(this.threads, this.batchsize);
        break;
      case OBJECTINSTANCE:
        handler = new MLTFeaturesImportHandler(this.threads, this.batchsize, clean);
        break;
      case LSCMETA:
        handler = new MetaImportHandler(this.threads, this.batchsize, this.clean);
        break;
      case LSCCONCEPT:
        handler = new VisualConceptTagImportHandler(this.threads, this.batchsize);
        break;
      case LSCCAPTION:
        handler = new CaptionImportHandler(this.threads, this.batchsize);
        break;
      case LSCX:
        handler = new ProcessingMetaImportHandler(this.threads, this.batchsize, false);
        break;
      case LSCTABLE:
        handler = new ProcessingMetaImportHandler(this.threads, this.batchsize, true);
        break;
      case LSC20TAGS:
        handler = new LSCAllTagsImportHandler(this.threads, this.batchsize, clean);
        break;
      case LSCOCR:
        handler = new OCRImportHandler(this.threads, this.batchsize, clean);
        break;
    }
    if (!isGoogleVision) {
      if (handler == null) {
        throw new RuntimeException("Cannot do import as the handler was not properly registered. Import type: " + type);
      } else {
        handler.doImport(path);
      }
    }

    /* Only attempt to optimize Cottontail entities if we were importing into Cottontail, otherwise an unavoidable error message would be displayed when importing elsewhere. */
    if (!doNotFinalize && Config.sharedConfig().getDatabase().getSelector() == DatabaseConfig.Selector.COTTONTAIL && Config.sharedConfig().getDatabase().getWriter() == DatabaseConfig.Writer.COTTONTAIL) {
      handler.waitForCompletion();
      OptimizeEntitiesCommand.optimizeAllCottontailEntities();
    }

    System.out.printf("Completed import of type %s for '%s'.%n", this.type, this.input);
  }

  private void doVisionImport(Path path) {
    List<GoogleVisionImportHandler> handlers = new ArrayList<>();
    for (GoogleVisionCategory category : GoogleVisionCategory.values()) {
      GoogleVisionImportHandler _handler = new GoogleVisionImportHandler(this.threads, 40_000, category, false);
      _handler.doImport(path);
      handlers.add(_handler);
      if (category == GoogleVisionCategory.LABELS || category == GoogleVisionCategory.WEB) {
        GoogleVisionImportHandler _handlerTrue = new GoogleVisionImportHandler(this.threads, 40_000, category, true);
        _handlerTrue.doImport(path);
        handlers.add(_handlerTrue);
      }
    }
    handlers.forEach(GoogleVisionImportHandler::waitForCompletion);
  }

  /**
   * Enum of the available types of data imports.
   */
  private enum ImportType {
    PROTO, JSON, LIRE, ASR, OCR, AUDIO, TAGS, VBS2020, METADATA, AUDIOTRANSCRIPTION, CAPTIONING, GOOGLEVISION, V3C1CLASSIFICATIONS, V3C1COLORLABELS, V3C1FACES, V3C1ANALYSIS, OBJECTINSTANCE, LSCMETA, LSCCONCEPT, LSCCAPTION, LSCX, LSCTABLE, LSC20TAGS, LSCOCR
  }
}
