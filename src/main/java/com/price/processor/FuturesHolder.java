package com.price.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FuturesHolder {
    private final Map<String, CompletableFuture<Void>> currentFutures = new HashMap<>();
    private final Map<String, CompletableFuture<Void>> nextFutures = new HashMap<>();

    public boolean hasActiveCurrent(String key) {
        return currentFutures.containsKey(key) && !currentFutures.get(key).isDone();
    }

    public boolean hasActiveNext(String key) {
        return nextFutures.containsKey(key) && !nextFutures.get(key).isDone();
    }

    public void runCurrent(String key, Runnable runnable) {
        currentFutures.put(key, CompletableFuture.runAsync(runnable));
    }

    public void runNext(String key, Runnable runnable) {
        Optional.ofNullable(nextFutures.get(key)).map(f -> f.cancel(true));
        nextFutures.put(key, currentFutures.get(key).thenRun(runnable));
    }

    public void moveNextToCurrent(String key) {
        currentFutures.put(key, nextFutures.remove(key));
    }
}
