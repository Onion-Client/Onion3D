/*
 * Copyright (c) 2021, Undefine <undefine@undefine.pl>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.undefine.onion3d;

import org.lwjgl.glfw.GLFW;

//TODO: Mouse scroll
public class Mouse
{
    private static boolean grabbed = false;

    private static int lastX = 0;
    private static int lastY = 0;

    private static int latestX = 0;
    private static int latestY = 0;

    private static int x = 0;
    private static int y = 0;

    private static final EventQueue queue = new EventQueue(32);

    private static final int[] buttonEvents = new int[queue.GetMaxEvents()];
    private static final boolean[] buttonEventStates = new boolean[queue.GetMaxEvents()];
    private static final int[] xEvents = new int[queue.GetMaxEvents()];
    private static final int[] yEvents = new int[queue.GetMaxEvents()];
    private static final int[] lastXEvents = new int[queue.GetMaxEvents()];
    private static final int[] lastYEvents = new int[queue.GetMaxEvents()];

    private static boolean clipPositionToDisplay = true;

    public static void AddMoveEvent(double mouseX, double mouseY)
    {
        latestX = (int) mouseX;
        latestY = Display.GetHeight() - (int) mouseY;

        lastXEvents[queue.GetNextPos()] = xEvents[queue.GetNextPos()];
        lastYEvents[queue.GetNextPos()] = yEvents[queue.GetNextPos()];

        xEvents[queue.GetNextPos()] = latestX;
        yEvents[queue.GetNextPos()] = latestY;

        buttonEvents[queue.GetNextPos()] = -1;
        buttonEventStates[queue.GetNextPos()] = false;

        queue.Add();
    }

    public static void AddButtonEvent(int button, boolean pressed)
    {
        lastXEvents[queue.GetNextPos()] = xEvents[queue.GetNextPos()];
        lastYEvents[queue.GetNextPos()] = yEvents[queue.GetNextPos()];

        xEvents[queue.GetNextPos()] = latestX;
        yEvents[queue.GetNextPos()] = latestY;

        buttonEvents[queue.GetNextPos()] = button;
        buttonEventStates[queue.GetNextPos()] = pressed;

        queue.Add();
    }

    public static void Poll()
    {
        lastX = x;
        lastY = y;

        if (!grabbed && clipPositionToDisplay)
        {
            if (latestX < 0) latestX = 0;
            if (latestY < 0) latestY = 0;
            if (latestX > Display.GetWidth() - 1) latestX = Display.GetWidth() - 1;
            if (latestY > Display.GetHeight() - 1) latestY = Display.GetHeight() - 1;
        }

        x = latestX;
        y = latestY;
    }

    public static boolean IsCreated()
    {
        return Display.IsCreated();
    }

    public static void SetGrabbed(boolean grab)
    {
        GLFW.glfwSetInputMode(Display.GetWindow(),
                GLFW.GLFW_CURSOR,
                grab ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        grabbed = grab;
    }

    public static boolean IsGrabbed()
    {
        return grabbed;
    }

    public static boolean IsButtonDown(int button)
    {
        return GLFW.glfwGetMouseButton(Display.GetWindow(), button) == GLFW.GLFW_PRESS;
    }

    public static boolean Next()
    {
        return queue.Next();
    }

    public static int GetEventX()
    {
        return xEvents[queue.GetCurrentPos()];
    }

    public static int GetEventY()
    {
        return yEvents[queue.GetCurrentPos()];
    }

    public static int GetEventDX()
    {
        return xEvents[queue.GetCurrentPos()] - lastXEvents[queue.GetCurrentPos()];
    }

    public static int GetEventDY()
    {
        return yEvents[queue.GetCurrentPos()] - lastYEvents[queue.GetCurrentPos()];
    }

    public static int GetEventButton()
    {
        return buttonEvents[queue.GetCurrentPos()];
    }

    public static boolean GetEventButtonState()
    {
        return buttonEventStates[queue.GetCurrentPos()];
    }

    public static int GetEventDWheel()
    {
        return 0;
    }

    public static int GetX()
    {
        return x;
    }

    public static int GetY()
    {
        return y;
    }

    public static int GetDX()
    {
        return x - lastX;
    }

    public static int GetDY()
    {
        return y - lastY;
    }

    public static int GetDWheel()
    {
        return 0;
    }

    public static void SetClipMouseCoordinatesToWindow(boolean clip)
    {
        clipPositionToDisplay = clip;
    }

    public static void SetCursorPosition(int x, int y)
    {
        GLFW.glfwSetCursorPos(Display.GetWindow(), x, y);
    }
}
