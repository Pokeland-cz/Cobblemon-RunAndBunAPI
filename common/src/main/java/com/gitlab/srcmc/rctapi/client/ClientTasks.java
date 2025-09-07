/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers API. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctapi.client;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

import com.gitlab.srcmc.rctapi.ModCommon;

import net.minecraft.client.Minecraft;

public class ClientTasks {
    public static final ClientTasks BATTLE_SELECTIONS = new ClientTasks();
    private static final long PREDICATE_CHECK_TIMEOUT = 200;

    private Queue<Thread> tasks = new LinkedList<>();

    public void run(Runnable task) {
        this.addTask(() -> Minecraft.getInstance().execute(task));
    }

    public void runIf(Runnable task, Supplier<Boolean> predicate) {
        this.runIf(task, predicate, 0);
    }

    public void runIf(Runnable task, Supplier<Boolean> predicate, long maxTimeout) {
        this.addTask(() -> {
            try {
                long t = 0;

                for(; t < maxTimeout && !predicate.get(); t += PREDICATE_CHECK_TIMEOUT) {
                    Thread.sleep(PREDICATE_CHECK_TIMEOUT);
                }

                if(t >= maxTimeout) {
                    ModCommon.LOG.warn("FORCED UNLOCK");
                }
            } catch(InterruptedException e) {}
            
            Minecraft.getInstance().execute(task);
        });
    }

    public void sleepThenRun(long millis, Runnable task) {
        this.addTask(() -> {
            try {
                Thread.sleep(millis);
            } catch(InterruptedException e) {}
            
            Minecraft.getInstance().execute(task);
        });
    }

    public void waitThenRun(Runnable task) {
        this.addTask(() -> {
            try {
                this.wait();
            } catch(InterruptedException e) {}

            Minecraft.getInstance().execute(task);
        });
    }

    public void continueCurrent() {
        synchronized(this.tasks) {
            var current = this.tasks.peek();

            if(current != null) {
                current.interrupt();
            }
        }
    }

    public boolean isRunning() {
        int size;

        synchronized(this.tasks) {
            size = this.tasks.size();
        }

        return size > 0;
    }

    private Thread addTask(Runnable task) {
        var t = new Thread(task);

        new Thread(() -> {
            try {
                t.join();
            } catch(InterruptedException e) {}

            synchronized(this.tasks) {
                this.tasks.poll();

                if(!this.tasks.isEmpty()) {
                    this.tasks.peek().start();
                }
            }
        }).start();

        synchronized(this.tasks) {
            this.tasks.offer(t);
    
            if(this.tasks.size() == 1) {
                t.start();
            }
        }

        return t;
    }
}
