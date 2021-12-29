/*
 * Copyright (c) 2021, Undefine <undefine@undefine.pl>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.undefine.onion3d;

import org.lwjgl.glfw.GLFW;

class Sync
{
    private static final long NANOS_IN_SECOND = 1000L * 1000L * 1000L;

    private static long nextFrame = 0;
    private static boolean initialised = false;

    private static final RunningAvg sleepDurations = new RunningAvg(10);
    private static final RunningAvg yieldDurations = new RunningAvg(10);

    @SuppressWarnings({"BusyWait", "MethodNameSameAsClassName"})
    public static void Sync(int fps)
    {
        if (fps <= 0) return;
        if (!initialised) Initialize();

        try
        {
            for (long t0 = GetTime(), t1; (nextFrame - t0) > sleepDurations.Average(); t0 = t1)
            {
                Thread.sleep(1);
                sleepDurations.Add((t1 = GetTime()) - t0);
            }

            sleepDurations.DampenForLowResTicker();

            for (long t0 = GetTime(), t1; (nextFrame - t0) > yieldDurations.Average(); t0 = t1)
            {
                Thread.yield();
                yieldDurations.Add((t1 = GetTime()) - t0);
            }
        }
        catch (InterruptedException ignored)
        {
        }

        nextFrame = Math.max(nextFrame + NANOS_IN_SECOND / fps, GetTime());
    }

    private static void Initialize()
    {
        initialised = true;

        sleepDurations.Initialize(1000 * 1000);
        yieldDurations.Initialize(0);

        nextFrame = GetTime();

        String osName = System.getProperty("os.name");

        if (osName.startsWith("Win"))
        {
            Thread timerAccuracyThread = new Thread(() -> {
                try
                {
                    Thread.sleep(Long.MAX_VALUE);
                }
                catch (Exception ignored)
                {
                }
            });

            timerAccuracyThread.setName("LWJGL Timer");
            timerAccuracyThread.setDaemon(true);
            timerAccuracyThread.start();
        }
    }

    private static long GetTime()
    {
        return (long) (GLFW.glfwGetTime() * 1000L * NANOS_IN_SECOND) / 1000L;
    }

    private static class RunningAvg
    {
        private final long[] slots;
        private int offset;

        private static final long DAMPEN_THRESHOLD = 10 * 1000L * 1000L;
        private static final float DAMPEN_FACTOR = 0.9f;

        public RunningAvg(int slotCount)
        {
            this.slots = new long[slotCount];
            this.offset = 0;
        }

        public void Initialize(long value)
        {
            while (this.offset < this.slots.length)
            {
                this.slots[this.offset++] = value;
            }
        }

        public void Add(long value)
        {
            this.slots[this.offset++ % this.slots.length] = value;
            this.offset %= this.slots.length;
        }

        public long Average()
        {
            long sum = 0;
            for (long slot : this.slots)
            {
                sum += slot;
            }
            return sum / this.slots.length;
        }

        public void DampenForLowResTicker()
        {
            if (this.Average() > DAMPEN_THRESHOLD)
            {
                for (int i = 0; i < this.slots.length; i++)
                {
                    this.slots[i] *= DAMPEN_FACTOR;
                }
            }
        }
    }
}