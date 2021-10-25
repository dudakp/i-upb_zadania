package sk.dudak.upb.ecdc.utils;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadingUtils {

    public static void runTaskInThread(Task<?> task, EventHandler<WorkerStateEvent> onSuccess, EventHandler<WorkerStateEvent> onFailure) {
        task.setOnSucceeded(onSuccess);
        task.setOnFailed(onFailure);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(task);
        executorService.shutdown();
    }
}
