package org.vitrivr.cineast.core.data.query.containers;

public class TextQueryContainer extends QueryContainer {

    /**
     * Text contained in this {@link TextQueryContainer}.
     */
    private final String text;

    /**
     * Constructs a new {@link TextQueryContainer} from the provided text.
     *
     * @param text Text for which to construct a {@link TextQueryContainer}.
     */
    public TextQueryContainer(String text) {
        this.text = (text == null ? "" : text);
    }

    @Override
    public String getText() {
        return this.text;
    }
}
