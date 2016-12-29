package org.vitrivr.cineast.core.data.audio;

/**
 * @author rgasser
 * @version 1.0
 * @created 30.11.16
 */
public class AudioFrame {

    private final long id;

    private final long length;

    private final byte[] data;


    public AudioFrame(long id, byte[] data){
        this.id = id;
        this.data = data;
        this.length = data.length;
    }

    public long getId(){
        return this.id;
    }

    public long getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public void clear(){

    }
}
