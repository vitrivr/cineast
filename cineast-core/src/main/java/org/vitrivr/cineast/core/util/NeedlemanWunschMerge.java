package org.vitrivr.cineast.core.util;

public class NeedlemanWunschMerge {
    private String string1;
    private String string2;
    private int match_reward = 1;
    private int mismatch_penalty = -1;
    private int gap_penalty = -2;

    /**
     * @param string1 The first string to be merged
     * @param string2 The second string to be merged
     */
    public NeedlemanWunschMerge(String string1, String string2) {
        this.string1 = string1;
        this.string2 = string2;
    }

    /**
     * execute takes the two strings supplied and returns a substring of commonality
     * @return The substring of string1 and string2 which are similar
     */
    public String execute() {
        int[][] main_matrix = new int[string1.length()+1][string2.length()+1];
        int[][] match_checker_matrix = new int[string1.length()][string2.length()];

        for (int i=0; i<string1.length(); i++) {
            for (int j=0; j<string2.length(); j++) {
                match_checker_matrix[i][j] = string1.charAt(i) == string2.charAt(j) ? match_reward : mismatch_penalty;
            }
        }

        for (int i=0; i<string1.length()+1; i++) {
            main_matrix[i][0] = i*gap_penalty;
        }
        for (int j=0; j<string2.length()+1; j++) {
            main_matrix[0][j] = j*gap_penalty;
        }
        for (int i=1; i<string1.length()+1; i++) {
            for (int j=1; j<string2.length()+1; j++) {
                main_matrix[i][j] = Math.max(Math.max(main_matrix[i-1][j-1] + match_checker_matrix[i-1][j-1],
                        main_matrix[i-1][j]+gap_penalty), main_matrix[i][j-1]+gap_penalty);
            }
        }

        StringBuilder final_string = new StringBuilder();
        int ti = string1.length();
        int tj = string2.length();

        while (ti > 0 && tj > 0) {
            if (main_matrix[ti][tj] == main_matrix[ti-1][tj-1] + match_checker_matrix[ti-1][tj-1]) {
                final_string.insert(0, string1.charAt(ti-1));
                ti--;
                tj--;
            } else if (main_matrix[ti][tj] == main_matrix[ti-1][tj] + gap_penalty) {
                ti--;
            } else {
                tj--;
            }
        }
        return final_string.toString();
    }
}
