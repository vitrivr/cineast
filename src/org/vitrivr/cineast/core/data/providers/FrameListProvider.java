package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public interface FrameListProvider {
    default List<Frame> getFrames() {
        ArrayList<Frame> list = new ArrayList<>(1);
        list.add(Frame.EMPTY_FRAME);
        return list;
    }
}
