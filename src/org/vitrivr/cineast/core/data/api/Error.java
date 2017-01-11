package org.vitrivr.cineast.core.data.api;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public class Error {
    int code;
    String status;

    public Error(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
