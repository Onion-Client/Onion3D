/*
 * Copyright (c) 2021, Undefine <undefine@undefine.pl>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.undefine.onion3d;

class EventQueue
{
    private final int maxEvents;
    private int eventCount = 0;
    private int currentEventPos = -1;
    private int nextEventPos = 0;

    EventQueue(int maxEvents)
    {
        this.maxEvents = maxEvents;
    }

    void Add()
    {
        eventCount++;
        if (eventCount > maxEvents) eventCount = maxEvents;

        nextEventPos++;
        if (nextEventPos == maxEvents) nextEventPos = 0;

        if (nextEventPos == currentEventPos) currentEventPos++;
        if (currentEventPos == maxEvents) currentEventPos = 0;
    }

    boolean Next()
    {
        if (eventCount == 0) return false;

        eventCount--;
        currentEventPos++;
        if (currentEventPos == maxEvents) currentEventPos = 0;

        return true;
    }

    int GetMaxEvents()
    {
        return maxEvents;
    }

    int GetCurrentPos()
    {
        return currentEventPos;
    }

    int GetNextPos()
    {
        return nextEventPos;
    }
}
