package org.vitrivr.cineast.standalone.util;

import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Helper class that can be used to start an interactive CLI.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
public class CLI {

    private CLI() {}

    /**
     * Starts the interactive CLI. This is method will block.
     */
    public static void start(Class<?> cliClass) {
        final Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the interactive Cineast CLI.");
        try {
            while (true) {
                System.out.println("Please enter a command...");
                final String line = scanner.nextLine();
                if (line.toLowerCase().equals("exit") || line.toLowerCase().equals("quit") ) {
                    break;
                }

                /* Try to parse user input. */
                try {
                    com.github.rvesse.airline.Cli<Runnable> cli = new com.github.rvesse.airline.Cli<>(cliClass);
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
