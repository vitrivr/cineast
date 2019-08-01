package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import org.vitrivr.cineast.standalone.config.Config;

public abstract class CineastCli implements Runnable  {
    @Option(name = { "-c", "--config" }, title = "Config", description = "Path to the Cineast configuration file that should be used. Defaults to ./cineast.json", type = OptionType.GLOBAL)
    protected String config;


    @Override
    public void run() {
        if (this.config != null) {
            Config.loadConfig(this.config);
        }
    }
}
