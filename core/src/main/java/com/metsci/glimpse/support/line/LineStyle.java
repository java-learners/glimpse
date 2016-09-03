package com.metsci.glimpse.support.line;

import com.metsci.glimpse.support.color.GlimpseColor;

public class LineStyle
{

    public float[] rgba = GlimpseColor.getBlack( );

    /**
     * The thickness of the ideal bounds of the line. Feathering will encroach into
     * these bounds, by half of {@link #feather_PX}.
     */
    public float thickness_PX = 1.0f;

    /**
     * The thickness of the feather region, across which alpha fades to transparent.
     * Half the feather thickness (the more opaque half) lies inside the ideal bounds
     * of the line, and half (the more transparent half) lies outside.
     */
    public float feather_PX = 0.9f;

    public boolean stippleEnable = false;

    /**
     * The number of pixels, along the length of the line, covered by one bit
     * of {@link #stipplePattern}.
     */
    public float stippleScale = 1.0f;

    /**
     * Least significant bit is drawn at the start of the line. Despite the "int"
     * type, only the bottom 16 bits are used. 1 = opaque, 0 = transparent.
     */
    public int stipplePattern = 0b0101010101010101;

}
