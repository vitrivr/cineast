package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;

import org.vitrivr.cineast.standalone.Main;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A CLI command that can be used to start interactive CLI mode.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "interactive", description = "Starts the interactive Cineast CLI.")
public class InteractiveCli extends CineastCli {


    public void run() {
        super.loadConfig();
        final Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the interactive Cineast CLI.");
        try {
            while (true) {
                System.out.println("Please enter a command...");
                final String line = scanner.nextLine();
                if (line.toLowerCase().equals("exit") || line.toLowerCase().equals("quit") ) {
                    break;
                }

                if (line.toLowerCase().equals("interactive")) {
                    System.err.println("You already are in interactive mode!");
                    continue;
                }

                try {/* Try to parse user input. */
                    com.github.rvesse.airline.Cli<Runnable> cli = new com.github.rvesse.airline.Cli<>(Main.class);
                    final Runnable command = cli.parse(line.split(" "));
                    command.run();
                } catch (ParseRestrictionViolatedException e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IllegalStateException | NoSuchElementException e) {
            System.out.println("System.in was closed; exiting");
        }
    }
}
