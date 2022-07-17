package org.vitrivr.cineast.core.data.query.containers;

import java.util.Objects;

public class TextQueryTermContainer extends AbstractQueryTermContainer {

  /**
   * Text contained in this {@link TextQueryTermContainer}.
   */
  private final String text;

  /**
   * Constructs a new {@link TextQueryTermContainer} from the provided text.
   *
   * @param text Text for which to construct a {@link TextQueryTermContainer}.
   */
  public TextQueryTermContainer(String text) {
    this.text = (text == null ? "" : text);
  }

  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TextQueryTermContainer that = (TextQueryTermContainer) o;
    return Objects.equals(text, that.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), text);
  }
}
