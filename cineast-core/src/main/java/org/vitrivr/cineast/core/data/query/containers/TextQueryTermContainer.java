package org.vitrivr.cineast.core.data.query.containers;

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
}
