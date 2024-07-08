package org.vitrivr.cineast.core.data.m3d.texturemodel.util;

public class TextureLoadException extends Exception
{
    // Parameterless Constructor
    public TextureLoadException() {}

    // Constructor that accepts a message
    public TextureLoadException(String message)
    {
        super(message);
    }
}