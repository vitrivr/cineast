package org.vitrivr.cineast.standalone.util;

import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Helper class that can be used to start an interactive CLI.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
public class CLI {

    private static final String PROMPT = "cineast> ";

    private CLI() {}

    /**
     * Starts the interactive CLI. This is method will block.
     */
    public static void start(Class<?> cliClass) {

        Terminal terminal = null;
        try {
            terminal = TerminalBuilder.terminal(); //basic terminal
        } catch (IOException e) {
            System.err.println("Could not initialize Terminal: ");
            System.err.println(e.getMessage());
            System.err.println("Exiting...");
            System.exit(-1);
        }

        Completer completer = new AggregateCompleter(
            new Completers.FileNameCompleter() //TODO figure out a way to build a completer based on airline commands
        );

        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();


        terminal.writer().println("Welcome to the interactive Cineast CLI.");

        try {
            while (true) {

                final String line = lineReader.readLine(PROMPT);
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
