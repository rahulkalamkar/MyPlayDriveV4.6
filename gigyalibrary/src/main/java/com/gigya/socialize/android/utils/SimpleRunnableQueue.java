package com.gigya.socialize.android.utils;

import java.util.LinkedList;
import java.util.Queue;


public class SimpleRunnableQueue {
    private boolean released = false;
    private Queue<Runnable> queue = new LinkedList<Runnable>();

    public void enqueue(Runnable task) {
        if (!released)
            queue.add(task);
        else
            task.run();
    }

    public void release() {
        released = true;

        Runnable task = queue.poll();
        while (task != null) {
            task.run();
            task = queue.poll();
        }
    }
}
