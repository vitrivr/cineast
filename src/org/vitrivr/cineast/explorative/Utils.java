package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 14.09.16.
 */
public class Utils {

    public static String listToString(List l){
        if(l.size() == 0) return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Object o : l) {
            sb.append(o.toString() + ", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "");
        sb.append("}");
        return sb.toString();
    }
}
