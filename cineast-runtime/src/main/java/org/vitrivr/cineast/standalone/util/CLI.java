package org.vitrivr.cineast.standalone.util;

import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.ParseException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.WriterOutputStream;

/**
 * Helper class that can be used to start an interactive CLI.
 */
public class CLI {

  final static Pattern lineSplitRegex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
  private static final String PROMPT = "cineast> ";
  private static final String LOGO = "################################################################################\u0085#                                                                              #\u0085#                @@@                           @@@                             #\u0085#                @@@                           @@@                             #\u0085#                     @@@@                                                     #\u0085#   @@@     @@@  @@@  @@@@@@@         @@@@@    @@@  @@@     @@@     @@@@@      #\u0085#   @@@@   @@@@  @@@  @@@@          @@@@@@@@@  @@@  @@@@   @@@@   @@@@@@@@@    #\u0085#     @@@@@@@    @@@  @@@@    @@@  @@@@        @@@    @@@@@@@    @@@@          #\u0085#      @@@@@     @@@   @@@@@@@@@@  @@@@        @@@     @@@@@     @@@@          #\u0085#       @@@      @@@     @@@@@     @@@         @@@      @@@      @@@           #\u0085#                                                                              #\u0085################################################################################";


  private CLI() {
  }

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

    final com.github.rvesse.airline.Cli<Runnable> cli = new com.github.rvesse.airline.Cli<>(cliClass);
    final List<String> commandNames = cli.getMetadata().getDefaultGroupCommands().stream()
        .map(CommandMetadata::getName).collect(Collectors.toList());

    final Completer completer = new AggregateCompleter(
        new StringsCompleter("quit", "exit", "stop"),
        new StringsCompleter(commandNames),
        new Completers.FileNameCompleter()
    );

    final LineReader lineReader = LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(completer)
        .build();

    terminal.writer().println(LOGO.replaceAll("\u0085", "\r\n"));
    terminal.writer().println("Welcome to the interactive Cineast CLI.");

    final OutputStream terminalOutput = new WriterOutputStream(terminal.writer(), Charset.defaultCharset());

    try {
      while (true) {

        final String line;
        /* Catch ^D end of file as exit method */
        try {
          line = lineReader.readLine(PROMPT).trim();
        } catch (EndOfFileException e) {
          break;
        }

        if (line.toLowerCase().equals("exit") || line.toLowerCase().equals("quit") || line.toLowerCase().equals("stop")) {
          break;
        }

        /* Try to parse user input. */
        try {
          final String[] args = CLI.splitLine(line);

          ParseResult<Runnable> result = cli.parseWithResult(args);
          if (result.wasSuccessful()) {
            result.getCommand().run();
          } else {
            printlnRed(terminal.writer(), String.format("%d errors ecountered:", result.getErrors().size()));
            int i = 1;
            for (ParseException e : result.getErrors()) {
              printlnRed(terminal.writer(), String.format("%d. %s", i++, e.getMessage()));
            }
            terminal.writer().println();

            com.github.rvesse.airline.help.Help.help(cli.getMetadata(), Arrays.asList(args), terminalOutput);
          }

        } catch (Exception e) {
          printlnRed(terminal.writer(), "Error: ", e.getMessage());
        }
      }
    } catch (IllegalStateException | NoSuchElementException e) {
      System.out.println("System.in was closed; exiting");
    }
  }

  private static void printlnRed(PrintWriter pw, String... msg) {

    final AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));

    for (String s : msg) {
      asb.append(s);
    }
    pw.println(asb.toAnsi());
  }

  //based on https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double/366532
  private static String[] splitLine(String line) {
    if (line == null || line.isEmpty()) {
      return new String[0];
    }
    List<String> matchList = new ArrayList<String>();
    Matcher regexMatcher = lineSplitRegex.matcher(line);
    while (regexMatcher.find()) {
      if (regexMatcher.group(1) != null) {
        // Add double-quoted string without the quotes
        matchList.add(regexMatcher.group(1));
      } else if (regexMatcher.group(2) != null) {
        // Add single-quoted string without the quotes
        matchList.add(regexMatcher.group(2));
      } else {
        // Add unquoted word
        matchList.add(regexMatcher.group());
      }
    }
    return matchList.toArray(new String[matchList.size()]);
  }
}
