/*
 * Copyright (c) 2021, Undefine <undefine@undefine.pl>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.undefine.onion3d;

public final class DisplayMode
{
    private final int width, height, bpp, frequency;

    public DisplayMode(int width, int height)
    {
        this(width, height, 0, 0);
    }

    public DisplayMode(int width, int height, int bpp, int freq)
    {
        this.width = width;
        this.height = height;
        this.bpp = bpp;
        this.frequency = freq;
    }

    public int GetWidth()
    {
        return width;
    }

    public int GetHeight()
    {
        return height;
    }

    public int GetBitsPerPixel()
    {
        return bpp;
    }

    public int GetFrequency()
    {
        return frequency;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DisplayMode))
        {
            return false;
        }

        DisplayMode dm = (DisplayMode) obj;
        return dm.width == width
                && dm.height == height
                && dm.bpp == bpp
                && dm.frequency == frequency;
    }

    @Override
    public int hashCode()
    {
        return width ^ height ^ frequency ^ bpp;
    }

    @Override
    public String toString()
    {
        return width +
                " x " +
                height +
                " x " +
                bpp +
                " @" +
                frequency +
                "Hz";
    }
}
