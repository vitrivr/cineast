package org.vitrivr.cineast.playground.label;

import sun.font.Decoration;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;

/** Simple Interface for Label providers
 *
 * Created by silvan on 23.08.16.
 */
public interface LabelProvider {

    /**
     * Returns a human-readable label associated with the given Index
     * @param index Should be within [0 ... #classes-1]
     */
    String getLabel(int index);
}

