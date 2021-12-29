/*
 * Copyright (c) 2021, Undefine <undefine@undefine.pl>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.undefine.onion3d;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class Display
{
    //TODO: Better logging

    private static long handle;

    private static String windowTitle = "Game";

    private static boolean displayCreated = false;
    private static boolean displayFocused = false;
    private static boolean displayResizable = false;

    private static DisplayMode mode = new DisplayMode(640, 480);
    private static final DisplayMode desktopDisplayMode;

    private static int latestEventKey = 0;

    private static boolean displayResized = false;
    private static int displayWidth = 0;
    private static int displayHeight = 0;

    private static boolean latestResized = false;
    private static int latestWidth = 0;
    private static int latestHeight = 0;

    private static int previousWidth;
    private static int previousHeight;
    private static int previousX;
    private static int previousY;

    private static final int monitorWidth;
    private static final int monitorHeight;
    private static final int monitorRefreshRate;

    public static GLFWKeyCallback keyCallback;
    public static GLFWCharCallback charCallback;
    public static GLFWCursorPosCallback cursorPosCallback;
    public static GLFWMouseButtonCallback mouseButtonCallback;
    public static GLFWWindowFocusCallback windowFocusCallback;
    public static GLFWWindowSizeCallback windowSizeCallback;

    static
    {
        if (!GLFW.glfwInit())
        {
            LogManager.getRootLogger().fatal("[Onion3D] Failed to initialize GLFW!");
            throw new RuntimeException("Failed to initialize GLFW");
        }

        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(monitor);

        if (vidmode == null)
        {
            LogManager.getRootLogger().error("[Onion3D] Failed to obtain video mode!");
            throw new RuntimeException("Failed to obtain video mode");
        }

        monitorWidth = vidmode.width();
        monitorHeight = vidmode.height();
        monitorRefreshRate = vidmode.refreshRate();
        int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();

        desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);
    }

    public static void Create()
    {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, displayResizable ? GL11.GL_TRUE : GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GL11.GL_TRUE);

        handle = GLFW.glfwCreateWindow(mode.GetWidth(), mode.GetHeight(), windowTitle, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == 0L)
        {
            LogManager.getRootLogger().fatal("[Onion3D] Failed to create the window!");
            throw new RuntimeException("Failed to create the window");
        }

        keyCallback = new GLFWKeyCallback()
        {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                latestEventKey = key;

                if (action == GLFW.GLFW_RELEASE || action == GLFW.GLFW_PRESS)
                {
                    Keyboard.AddKeyEvent(key, action == GLFW.GLFW_PRESS);
                }
            }
        };

        charCallback = new GLFWCharCallback()
        {
            @Override
            public void invoke(long window, int codepoint)
            {
                Keyboard.AddCharEvent(latestEventKey, (char) codepoint);
            }
        };

        cursorPosCallback = new GLFWCursorPosCallback()
        {
            @Override
            public void invoke(long window, double x, double y)
            {
                Mouse.AddMoveEvent(x, y);
            }
        };

        mouseButtonCallback = new GLFWMouseButtonCallback()
        {
            @Override
            public void invoke(long window, int button, int action, int mods)
            {
                Mouse.AddButtonEvent(button, action == GLFW.GLFW_PRESS);
            }
        };

        windowFocusCallback = new GLFWWindowFocusCallback()
        {
            @Override
            public void close()
            {
                super.close();
            }

            @Override
            public void callback(long ret, long args)
            {
                super.callback(ret, args);
            }

            @Override
            public void invoke(long l, boolean b)
            {
                displayFocused = b;
            }
        };

        windowSizeCallback = new GLFWWindowSizeCallback()
        {
            @Override
            public void invoke(long window, int width, int height)
            {
                latestResized = true;
                latestWidth = width;
                latestHeight = height;
            }
        };

        SetCallbacks();

        displayWidth = mode.GetWidth();
        displayHeight = mode.GetHeight();

        GLFW.glfwSetWindowPos(
                handle,
                (monitorWidth - mode.GetWidth()) / 2,
                (monitorHeight - mode.GetHeight()) / 2
        );

        GLFW.glfwMakeContextCurrent(handle);
        GL.createCapabilities();

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(handle);

        displayCreated = true;
    }

    private static void SetCallbacks()
    {
        GLFW.glfwSetKeyCallback(handle, keyCallback);
        GLFW.glfwSetCharCallback(handle, charCallback);
        GLFW.glfwSetCursorPosCallback(handle, cursorPosCallback);
        GLFW.glfwSetMouseButtonCallback(handle, mouseButtonCallback);
        GLFW.glfwSetWindowFocusCallback(handle, windowFocusCallback);
        GLFW.glfwSetWindowSizeCallback(handle, windowSizeCallback);
    }

    private static void ReleaseCallbacks()
    {
        keyCallback.free();
        charCallback.free();
        cursorPosCallback.free();
        mouseButtonCallback.free();
        windowFocusCallback.free();
        windowSizeCallback.free();
    }

    public static boolean IsCreated()
    {
        return displayCreated;
    }

    public static boolean IsActive()
    {
        return displayFocused;
    }

    public static void SetVSyncEnabled(boolean sync)
    {
        GLFW.glfwSwapInterval(sync ? 1 : 0);
    }

    public static long GetWindow()
    {
        return handle;
    }

    public static void Update()
    {
        SwapBuffers();
        ProcessMessages();
    }

    public static void ProcessMessages()
    {
        GLFW.glfwPollEvents();
        Keyboard.Poll();
        Mouse.Poll();

        if (latestResized)
        {
            latestResized = false;
            displayResized = true;
            displayWidth = latestWidth;
            displayHeight = latestHeight;
        }
        else
        {
            displayResized = false;
        }
    }

    public static void SwapBuffers()
    {
        GLFW.glfwSwapBuffers(handle);
    }

    public static void Destroy()
    {
        ReleaseCallbacks();
        GLFW.glfwDestroyWindow(handle);

        GLFW.glfwTerminate();
        displayCreated = false;
    }

    public static void SetDisplayMode(DisplayMode dm)
    {
        mode = dm;
    }

    public static DisplayMode GetDisplayMode()
    {
        return mode;
    }

    public static DisplayMode[] GetAvailableDisplayModes()
    {
        IntBuffer count = BufferUtils.createIntBuffer(1);
        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());

        if (modes == null)
        {
            LogManager.getRootLogger().error("[Onion3D] Failed to obtain available display modes!");
            return null;
        }

        DisplayMode[] displayModes = new DisplayMode[count.get(0)];

        for (int i = 0; i < count.get(0); i++)
        {
            modes.position(i * GLFWVidMode.SIZEOF);

            int w = modes.width();
            int h = modes.height();
            int b = modes.redBits() + modes.greenBits()
                    + modes.blueBits();
            int r = modes.refreshRate();

            displayModes[i] = new DisplayMode(w, h, b, r);
        }

        return displayModes;
    }

    public static DisplayMode GetDesktopDisplayMode()
    {
        return desktopDisplayMode;
    }

    public static boolean WasResized()
    {
        return displayResized;
    }

    public static int GetWidth()
    {
        return displayWidth;
    }

    public static int GetHeight()
    {
        return displayHeight;
    }

    public static void SetTitle(String title)
    {
        windowTitle = title;
    }

    public static boolean IsCloseRequested()
    {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public static int SetIcon(Icon[] icons)
    {
        GLFWImage.Buffer imageBuffer = GLFWImage.malloc(1);
        for (Icon icon : icons)
        {
            GLFWImage image = GLFWImage.malloc();
            image.set(icon.GetWidth(), icon.GetHeight(), icon.GetPixels());
            imageBuffer.put(0, image);
        }
        GLFW.glfwSetWindowIcon(handle, imageBuffer);
        return 0;
    }

    public static void SetResizable(boolean resizable)
    {
        displayResizable = resizable;
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GL11.GL_TRUE : GL11.GL_FALSE);
    }

    public static void SetFullscreen(boolean fullscreen)
    {
        if (fullscreen)
        {
            previousWidth = displayWidth;
            previousHeight = displayHeight;
            IntBuffer x = BufferUtils.createIntBuffer(1);
            IntBuffer y = BufferUtils.createIntBuffer(1);
            GLFW.glfwGetWindowPos(handle, x, y);
            previousX = x.get(0);
            previousY = y.get(0);
        }

        GLFW.glfwSetWindowMonitor(handle, fullscreen ? GLFW.glfwGetPrimaryMonitor() : 0, 0, 0, monitorWidth, monitorHeight, monitorRefreshRate);

        if (!fullscreen)
        {
            GLFW.glfwSetWindowSize(handle, previousWidth, previousHeight);
            GLFW.glfwSetWindowPos(handle, previousX, previousY);
        }
    }

    public static void Sync(int fps)
    {
        Sync.Sync(fps);
    }
}
