package org.vitrivr.cineast.core.util;

import java.util.Random;
/**
 * Generates random strings to be used for Ids
 *
 */
public class RandomStringGenerator {

  private RandomStringGenerator() {
  }

  public static final int DEFAULT_LENGTH = 16;
  public static final char[] DEFAULT_ALPHABET = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
      't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
      'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

  private static final Random r = new Random();

  public static String generateRandomString(String prefix, int length, char[] alphabet) {
    return generateRandomString(r, prefix, length, alphabet);
  }

  public static String generateRandomString(int length, char[] alphabet) {
    return generateRandomString("", length, alphabet);
  }

  public static String generateRandomString(int length) {
    return generateRandomString(length, DEFAULT_ALPHABET);
  }

  public static String generateRandomString(String prefix, int length) {
    return generateRandomString(prefix, length, DEFAULT_ALPHABET);
  }
  
  public static String generateRandomString(String prefix){
    return generateRandomString(prefix, DEFAULT_LENGTH);
  }
  
  public static String generateRandomString(){
    return generateRandomString("", DEFAULT_LENGTH, DEFAULT_ALPHABET);
  }

  /**
   * Generates a random string from a given alphabet with a given length. The prefix of the string can be optionally specified. 
   * @param random the random number generator to be used for string generation
   * @param prefix the prefix for the generated string. if <code>null</code> is provided, the string will have no prefix.
   * @param length the length of the string to generate, must be positive
   * @param alphabet the set of characters from which to generate the string
   */
  public static String generateRandomString(Random random, String prefix, int length,
      char[] alphabet) {
    if (random == null) {
      throw new NullPointerException("random cannot be null");
    }
    if (length <= 0) {
      throw new IllegalArgumentException("length must be positive");
    }
    if (prefix.length() >= length) {
      throw new IllegalArgumentException("prefix must be shorter than length of output");
    }
    if (alphabet == null) {
      throw new NullPointerException("alphabet cannot be null");
    }
    if (alphabet.length == 0) {
      throw new IllegalArgumentException("alphabet cannot be empty");
    }

    StringBuilder sb = new StringBuilder();
    if (prefix != null) {
      sb.append(prefix);
    }

    for (int l = sb.length(); l < length; ++l) {
      sb.append(alphabet[random.nextInt(alphabet.length)]);
    }

    return sb.toString();
  }

}
