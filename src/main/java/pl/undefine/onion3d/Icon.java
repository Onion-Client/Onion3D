/*
 * Copyright (c) 2021, Undefine <undefine@undefine.pl>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.undefine.onion3d;

import java.nio.ByteBuffer;

public record Icon(int width, int height, ByteBuffer pixels)
{
    public int GetWidth()
    {
        return width;
    }

    public int GetHeight()
    {
        return height;
    }

    public ByteBuffer GetPixels()
    {
        return pixels;
    }
}
