package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.standalone.importer.handlers.AsrDataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.JsonDataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.OcrDataImportHandler;
import org.vitrivr.cineast.standalone.importer.handlers.ProtoDataImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.CaptionImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.MetaImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.VisaulConceptTagImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.VisualConceptTagImporter;
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
 *
 */
@Command(name = "import", description = "Starts import of pre-extracted data.")
public class ImportCommand implements Runnable {

  @Option(name = {"-t", "--type"}, description = "Type of data import that should be started.")
  private String type;

  @Option(name = {"-i", "--input"}, description = "The source file or folder for data import. If a folder is specified, the entire content will be considered for import.")
  private String input;

  @Option(name = {"--threads"}, description = "Level of parallelization for import")
  private int threads = 2;

  @Option(name = {"-b", "--batchsize"}, description = "The batch size used for the import. Imported data will be persisted in batches of the specified size.")
  private int batchsize = 500;

  @Override
  public void run() {
    System.out.println(String.format("Starting import of type %s for '%s'.", this.type, this.input));
    final Path path = Paths.get(this.input);
    final ImportType type = ImportType.valueOf(this.type.toUpperCase());
    DataImportHandler handler;
    switch (type) {
      case PROTO:
        handler = new ProtoDataImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case JSON:
        handler = new JsonDataImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case ASR:
        handler = new AsrDataImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case OCR:
        handler = new OcrDataImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case CAPTIONING:
        handler = new CaptionTextImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case AUDIO:
        handler = new AudioTranscriptImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case TAGS:
        handler = new TagImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case METADATA:
        handler = new ObjectMetadataImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case AUDIOTRANSCRIPTION:
        handler = new AudioTranscriptImportHandler(this.threads, 15_000);
        handler.doImport(path);
        break;
      case GOOGLEVISION:
        doVisionImport(path);
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
        handler.doImport(path);
        break;
      case OBJECTINSTANCE:
        handler = new MLTFeaturesImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case LSCMETA:
        handler = new MetaImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
      case LSCCONCEPT:
        handler = new VisaulConceptTagImportHandler(this.threads,this.batchsize);
        handler.doImport(path);
        break;
      case LSCCAPTION:
        handler = new CaptionImportHandler(this.threads, this.batchsize);
        handler.doImport(path);
        break;
    }
    System.out.println(String.format("Completed import of type %s for '%s'.", this.type.toString(), this.input));
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
    PROTO, JSON, ASR, OCR, AUDIO, TAGS, VBS2020, METADATA, AUDIOTRANSCRIPTION, CAPTIONING, GOOGLEVISION, V3C1CLASSIFICATIONS, V3C1COLORLABELS, V3C1FACES, V3C1ANALYSIS, OBJECTINSTANCE, LSCMETA, LSCCONCEPT, LSCCAPTION
  }
}
