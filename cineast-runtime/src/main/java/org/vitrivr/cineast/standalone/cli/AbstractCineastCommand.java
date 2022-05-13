package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.HelpOption;
import javax.inject.Inject;

/**
 * The base command for al Cineast CLI commands.
 *
 * @author loris.sauter
 * @version 1.0
 */
public abstract class AbstractCineastCommand implements Runnable{

  /**
   * The help option available to derived classes.
   */
  @Inject
  protected HelpOption<Runnable> help;

  /**
   * The main method of all commands which will be executed by the CLI.
   */
  @Override
  public void run() {
    if(!help.showHelpIfRequested()){
      this.execute();
    }
  }

  /**
   * The actual command logic to be executed.
   */
  protected abstract void execute();

}
