package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import org.vitrivr.cineast.standalone.config.Config;

public abstract class CineastCli implements Runnable  {
    /** Flag indicating, that the Cineast config was loaded successfully. Make sure, that config is not re-loaded in interactive CLI context. */
    private static volatile boolean CONFIG_LOADED = false;

    @Option(name = { "-c", "--config" }, title = "Config", description = "Path to the Cineast configuration file that should be used. Defaults to ./cineast.json", type = OptionType.GLOBAL)
    protected String config;

    /**
     * Loads the application wide (singleton) {@link Config}. This method makes sure, that this config is only loaded once.
     */
    protected void loadConfig() {
        synchronized (CineastCli.class) {
            if (this.config != null && !CONFIG_LOADED) {
                Config.loadConfig(this.config);
            }
            CONFIG_LOADED = true;
        }
    }
}