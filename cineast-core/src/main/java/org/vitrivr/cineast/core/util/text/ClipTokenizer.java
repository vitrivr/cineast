package org.vitrivr.cineast.core.util.text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;

/**
 * Port of https://github.com/openai/CLIP/blob/573315e83f07b53a61ff5098757e8fc885f1703e/clip/simple_tokenizer.py
 */
public class ClipTokenizer {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final HashMap<Integer, Character> byte_encoder = bytes_to_unicode();
  private static final HashMap<Character, Integer> byte_decoder = new HashMap<>();
  private static final ArrayList<String> vocab = new ArrayList<>(49500);
  private static final HashMap<Pair<String, String>, Integer> bpe_ranks = new HashMap<>();
  private static final HashMap<String, Integer> encoder = new HashMap<>();

  static {
    for (int i : byte_encoder.keySet()) {
      byte_decoder.put(byte_encoder.get(i), i);
    }
  }

  private HashMap<String, String> cache = new HashMap<>();
  private Pattern pat = Pattern.compile("<\\|startoftext\\|>|<\\|endoftext\\|>|'s|'t|'re|'ve|'m|'ll|'d|[\\p{L}]+|[\\p{N}]|[^\\s\\p{L}\\p{N}]+", Pattern.CASE_INSENSITIVE);
  public ClipTokenizer() {
    init();
    cache.put("<|startoftext|>", "<|startoftext|>");
    cache.put("<|endoftext|>", "<|endoftext|>");
  }

  private static HashMap<Integer, Character> bytes_to_unicode() {
    //33 - 126, 161 - 172, 174 - 255
    char[] CHARS = new char[]{
        'Ā', 'ā', 'Ă', 'ă', 'Ą', 'ą', 'Ć', 'ć', 'Ĉ', 'ĉ', 'Ċ', 'ċ', 'Č', 'č', 'Ď', 'ď', 'Đ', 'đ', 'Ē', 'ē', 'Ĕ', 'ĕ', 'Ė', 'ė', 'Ę', 'ę', 'Ě', 'ě', 'Ĝ', 'ĝ', 'Ğ', 'ğ',
        'Ġ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', 'ġ', 'Ģ',
        'ģ', 'Ĥ', 'ĥ', 'Ħ', 'ħ', 'Ĩ', 'ĩ', 'Ī', 'ī', 'Ĭ', 'ĭ', 'Į', 'į', 'İ', 'ı', 'Ĳ', 'ĳ', 'Ĵ', 'ĵ', 'Ķ', 'ķ', 'ĸ', 'Ĺ', 'ĺ', 'Ļ', 'ļ', 'Ľ', 'ľ', 'Ŀ', 'ŀ', 'Ł', 'ł',
        '¡', '¢', '£', '¤', '¥', '¦', '§', '¨', '©', 'ª', '«', '¬', 'Ń', '®', '¯', '°', '±', '²', '³', '´', 'µ', '¶', '·', '¸', '¹', 'º', '»', '¼', '½', '¾', '¿', 'À',
        'Á', 'Â', 'Ã', 'Ä', 'Å', 'Æ', 'Ç', 'È', 'É', 'Ê', 'Ë', 'Ì', 'Í', 'Î', 'Ï', 'Ð', 'Ñ', 'Ò', 'Ó', 'Ô', 'Õ', 'Ö', '×', 'Ø', 'Ù', 'Ú', 'Û', 'Ü', 'Ý', 'Þ', 'ß', 'à',
        'á', 'â', 'ã', 'ä', 'å', 'æ', 'ç', 'è', 'é', 'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ð', 'ñ', 'ò', 'ó', 'ô', 'õ', 'ö', '÷', 'ø', 'ù', 'ú', 'û', 'ü', 'ý', 'þ', 'ÿ'
    };
    HashMap<Integer, Character> map = new HashMap<>();

    for (int i = 0; i < 256; ++i) {
      map.put(i, CHARS[i]);
    }
    return map;
  }

  private static Set<Pair<String, String>> get_pairs(List<String> word) {
    HashSet<Pair<String, String>> set = new HashSet<>();
    if (word.isEmpty()) {
      return set;
    }

    for (int i = 0; i < word.size() - 1; ++i) {
      set.add(new Pair<>(word.get(i) + "", word.get(i + 1)));
    }

    return set;
  }

  private static String whitespace_clean(String text) {
    return text.replaceAll("\\s+", " ").strip();
  }

  private static String clean(String text) {
    return StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeXml(text));
  }

  private static void init() {

    if (!vocab.isEmpty()) {
      return;
    }

    for (char c : byte_decoder.keySet()) {
      vocab.add("" + c);
    }
    for (int i = 0; i < 256; ++i) {
      vocab.add(vocab.get(i) + "</w>");
    }

    try {
      List<String> f = Files.readAllLines(Path.of("resources/CLIP/bpe_simple_vocab_16e6.txt"), StandardCharsets.UTF_8);
      for (int i = 1; i < 49152 - 256 - 2 + 1; ++i) {

        String s = f.get(i);
        vocab.add(s.replaceAll(" ", ""));

        String[] split = s.split(" ");
        bpe_ranks.put(new Pair<>(split[0], split[1]), i - 1);

      }
    } catch (IOException e) {
      LOGGER.error("Cannot load vocabulary {}", e);
    }

    vocab.add("<|startoftext|>");
    vocab.add("<|endoftext|>");

    for (int i = 0; i < vocab.size(); ++i) {
      encoder.put(vocab.get(i), i);
    }

  }

  String bpe(String token) {

    if (cache.containsKey(token)) {
      return cache.get(token);
    }

    ArrayList<String> word = new ArrayList<>(token.length());
    for (int j = 0; j < token.length() - 1; j++) {
      word.add(token.charAt(j) + "");
    }
    word.add(token.charAt(token.length() - 1) + "</w>");

    Set<Pair<String, String>> pairs = get_pairs(word);

    if (pairs.isEmpty()) {
      return token + "</w>";
    }

    while (true) {

      Pair<String, String> bigram = pairs.stream().map(p -> new Pair<>(p, bpe_ranks.getOrDefault(p, Integer.MAX_VALUE))).min(Comparator.comparingInt(p -> p.second)).get().first;

      if (!bpe_ranks.containsKey(bigram)) {
        break;
      }

      String first = bigram.first, second = bigram.second;

      int i = 0;

      ArrayList<String> new_word = new ArrayList<>();

      while (i < word.size()) {

        int j = word.subList(i, word.size()).indexOf(first) + i;
        if (j < i) {
          new_word.addAll(word.subList(i, word.size()));
          break;
        }
        new_word.addAll(word.subList(i, j));
        i = j;

        if (word.get(i).equals(first) && i < word.size() - 1 && word.get(i + 1).equals(second)) {
          new_word.add(first + second);
          i += 2;
        } else {
          new_word.add(word.get(i));
          ++i;
        }
      }

      word = new_word;
      if (word.size() == 1) {
        break;
      }
      pairs = get_pairs(word);

    }

    String bpe = word.stream().reduce("", (a, b) -> a + " " + b).trim();
    cache.put(token, bpe);
    return bpe;

  }

  public ArrayList<Integer> encode(String text) {
    ArrayList<Integer> bpe_tokens = new ArrayList<Integer>();
    String clean = whitespace_clean(clean(text)).toLowerCase(Locale.ROOT);
    Matcher m = pat.matcher(clean);
    while (m.find()) {
      String t = m.group();
      StringBuilder sb = new StringBuilder();
      for (byte b : t.getBytes(StandardCharsets.UTF_8)) {
        sb.append(byte_encoder.get((int) b));
      }
      for (String s : bpe(sb.toString()).split(" ")) {
        bpe_tokens.add(encoder.get(s));
      }
    }
    return bpe_tokens;

  }

  public long[] clipTokenize(String text) {
    int start = encoder.get("<|startoftext|>");
    int end = encoder.get("<|endoftext|>");
    ArrayList<Integer> tokens = encode(text);
    long[] arr = new long[77];
    arr[0] = start;
    int tokenCount = Math.min(tokens.size(), arr.length - 1);
    for (int i = 0; i < tokenCount; ++i) {
      arr[i + 1] = (long) tokens.get(i);
    }
    if (tokenCount + 1 < arr.length) {
      arr[tokenCount + 1] = end;
    }
    return arr;
  }

}
