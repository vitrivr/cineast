package org.vitrivr.cineast.core.data.api;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public class Status {
    private String status;
    public Status(String status) {
        this.status = "OK";
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
