package org.vitrivr.cineast.api.doc;

import com.beerboy.spark.typify.provider.GsonProvider;
import com.beerboy.spark.typify.spec.IgnoreSpec;
import com.beerboy.ss.ApiEndpoint;
import com.beerboy.ss.SparkSwagger;
import com.beerboy.ss.Swagger;
import com.beerboy.ss.SwaggerHammer;
import com.beerboy.ss.conf.IpResolver;
import com.beerboy.ss.conf.VersionResolver;
import com.beerboy.ss.descriptor.EndpointDescriptor;
import com.beerboy.ss.descriptor.EndpointDescriptor.Builder;
import com.beerboy.ss.model.Contact;
import com.beerboy.ss.model.ExternalDocs;
import com.beerboy.ss.model.Info;
import com.beerboy.ss.model.License;
import com.beerboy.ss.model.Scheme;
import com.beerboy.ss.rest.Endpoint;
import com.beerboy.ss.rest.EndpointResolver;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Filter;
import spark.HaltException;
import spark.Service;

/**
 * A custom {@link com.beerboy.ss.SparkSwagger} implementation.
 * This was required due to the heavily configurable setup of cineast's API (see {@link org.vitrivr.cineast.standalone.config.Config}
 * <br>
 * This is basically a copy of {@link com.beerboy.ss.SparkSwagger}, with less restrictions in
 * terms of visibility and access.
 *
 *
 * @author loris.sauter
 */
public class CineastSparkSwagger {
  /*
  private static final Logger LOGGER = LogManager.getLogger(CineastSparkSwagger.class);
  public static final String CONF_FILE_NAME = "spark-swagger.conf";
  private String apiPath;
  private Swagger swagger;
  private Service spark;
  private Config config;
  private String version;

  private CineastSparkSwagger(Service spark, String confPath, String version) {
    this.spark = spark;
    this.version = version;
    this.swagger = new Swagger();
    this.config = ConfigFactory.parseResources(confPath != null ? confPath : "spark-swagger.conf");
    this.apiPath = this.config.getString("spark-swagger.basePath");
    this.swagger.setBasePath(this.apiPath);
    this.swagger.setExternalDocs(ExternalDocs.newBuilder().build());
    this.swagger.setHost(this.getHost());
    this.swagger.setInfo(this.getInfo());
    this.configDocRoute();
  }

  private void configDocRoute() {
    String uiFolder = SwaggerHammer.getUiFolder(this.apiPath);
    SwaggerHammer.createDir(SwaggerHammer.getSwaggerUiFolder());
    SwaggerHammer.createDir(uiFolder);
    this.spark.externalStaticFileLocation(uiFolder);
    LOGGER.debug("Spark-Swagger: UI folder deployed at " + uiFolder);
    this.spark.options("/*", (request, response) -> {
      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });
    this.spark.before((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
    });
    LOGGER.debug("Spark-Swagger: CORS enabled and allow Origin *");
  }

  public String getApiPath() {
    return this.apiPath;
  }

  public String getVersion() {
    return this.version;
  }

  public Service getSpark() {
    return this.spark;
  }

  public static CineastSparkSwagger of(Service spark) {
    return new CineastSparkSwagger(spark, (String)null, (String)null);
  }

  public static CineastSparkSwagger of(Service spark, String confPath) {
    return new CineastSparkSwagger(spark, confPath, (String)null);
  }

  public CineastSparkSwagger version(String version) {
    this.version = version;
    return this;
  }

  public CineastSparkSwagger ignores(Supplier<IgnoreSpec> confSupplier) {
    this.swagger.ignores((IgnoreSpec)confSupplier.get());
    GsonProvider.create((IgnoreSpec)confSupplier.get());
    return this;
  }

  public void generateDoc() throws IOException {
    (new SwaggerHammer()).prepareUi(this.config, this.swagger);
  }

  public ApiEndpoint endpoint(Builder descriptorBuilder, Filter filter) {
    Optional.ofNullable(this.apiPath).orElseThrow(() -> {
      return new IllegalStateException("API Path must be specified in order to build REST endpoint");
    });
    EndpointDescriptor descriptor = descriptorBuilder.build();
    this.spark.before(this.apiPath + descriptor.getPath() + "/*", filter);
    ApiEndpoint apiEndpoint = new ApiEndpoint(this, descriptor);
    this.swagger.addApiEndpoint(apiEndpoint);
    return apiEndpoint;
  }

  public CineastSparkSwagger endpoint(Builder descriptorBuilder, Filter filter, Consumer<ApiEndpoint> endpointDef) {
    Optional.ofNullable(this.apiPath).orElseThrow(() -> {
      return new IllegalStateException("API Path must be specified in order to build REST endpoint");
    });
    EndpointDescriptor descriptor = descriptorBuilder.build();
    this.spark.before(this.apiPath + descriptor.getPath() + "/*", filter);
    ApiEndpoint apiEndpoint = new ApiEndpoint(this, descriptor);
    endpointDef.accept(apiEndpoint);
    this.swagger.addApiEndpoint(apiEndpoint);
    return this;
  }

  public CineastSparkSwagger endpoint(Endpoint endpoint) {
    Optional.ofNullable(endpoint).orElseThrow(() -> {
      return new IllegalStateException("API Endpoint cannot be null");
    });
    endpoint.bind(this);
    return this;
  }

  public CineastSparkSwagger endpoints(EndpointResolver resolver) {
    Optional.ofNullable(resolver).orElseThrow(() -> {
      return new IllegalStateException("API Endpoint Resolver cannot be null");
    });
    resolver.endpoints().forEach(this::endpoint);
    return this;
  }

  public CineastSparkSwagger before(Filter filter) {
    this.spark.before(this.apiPath + "/*", filter);
    return this;
  }

  public CineastSparkSwagger after(Filter filter) {
    this.spark.after(this.apiPath + "/*", filter);
    return this;
  }

  public synchronized CineastSparkSwagger exception(Class<? extends Exception> exceptionClass, ExceptionHandler handler) {
    this.spark.exception(exceptionClass, handler);
    return this;
  }

  public HaltException halt() {
    return this.spark.halt();
  }

  public HaltException halt(int status) {
    return this.spark.halt(status);
  }

  public HaltException halt(String body) {
    return this.spark.halt(body);
  }

  public HaltException halt(int status, String body) {
    return this.spark.halt(status, body);
  }

  private String getHost() {
    String host = this.config.getString("spark-swagger.host");
    if (host != null && (!host.contains("localhost") || host.split(":").length == 2)) {
      if (host.contains("localhost")) {
        String[] hostParts = host.split(":");
        host = IpResolver.resolvePublicIp() + ":" + hostParts[1];
      }

      LOGGER.debug("Spark-Swagger: Host resolved to " + host);
      return host;
    } else {
      throw new IllegalArgumentException("Host is required. If host name is 'localhost' you also need to specify port");
    }
  }

  private Info getInfo() {
    Config infoConfig = (Config)Optional.ofNullable(this.config.getConfig("spark-swagger.info")).orElseThrow(() -> {
      return new IllegalArgumentException("'spark-swagger.info' configuration is required");
    });
    Config externalDocConf;
    if (this.version == null) {
      externalDocConf = this.config.getConfig("spark-swagger.info.project");
      if (externalDocConf != null) {
        this.version = VersionResolver
            .resolveVersion(externalDocConf.getString("groupId"), externalDocConf.getString("artifactId"));
      }
    }

    externalDocConf = this.config.getConfig("spark-swagger.info.externalDoc");
    if (externalDocConf != null) {
      ExternalDocs doc = ExternalDocs.newBuilder().withDescription(externalDocConf.getString("description")).withUrl(externalDocConf.getString("url")).build();
      this.swagger.setExternalDocs(doc);
    }

    Info info = new Info();
    info.description(infoConfig.getString("description"));
    info.version(this.version);
    info.title(infoConfig.getString("title"));
    info.termsOfService(infoConfig.getString("termsOfService"));
    List<String> schemeStrings = (List)Optional.ofNullable(infoConfig.getStringList("schemes")).orElseThrow(() -> {
      return new IllegalArgumentException("'spark-swagger.info.schemes' configuration is required");
    });
    List<Scheme> schemes = (List)schemeStrings.stream().filter((s) -> {
      return Scheme.forValue(s) != null;
    }).map(Scheme::forValue).collect(Collectors.toList());
    if (schemes.isEmpty()) {
      throw new IllegalArgumentException("At least one Scheme mus be specified. Use 'spark-swagger.info.schemes' property. spark-swagger.info.schemes =[\"HTTP\"]");
    } else {
      this.swagger.schemes(schemes);
      Config contactConfig = this.config.getConfig("spark-swagger.info.contact");
      if (contactConfig != null) {
        Contact contact = new Contact();
        contact.name(contactConfig.getString("name"));
        contact.email(contactConfig.getString("email"));
        contact.url(contactConfig.getString("url"));
        info.setContact(contact);
      }

      Config licenseConfig = this.config.getConfig("spark-swagger.info.license");
      if (licenseConfig != null) {
        License license = new License();
        license.name(licenseConfig.getString("name"));
        license.url(licenseConfig.getString("url"));
        info.setLicense(license);
      }

      return info;
    }
  }*/
}
