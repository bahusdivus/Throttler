package com.price.processor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PriceThrottler implements PriceProcessor {
    private static final Logger LOGGER = Logger.getLogger(PriceThrottler.class.getName());
    private static final int MAX_RETRIES = 3;

    private final Map<PriceProcessor, Map<String, Double>> processors = new ConcurrentHashMap<>();

    @Override
    public void onPrice(String ccyPair, double rate) {
        processors.forEach((processor, rates) -> {
            if (rates.put(ccyPair, rate) == null) {
                throttleAsync(processor, rates, ccyPair, rate, 0);
            }
        });
    }

    private void throttleAsync(PriceProcessor processor, Map<String, Double> rates, String ccyPair, double rate, int retry) {
        CompletableFuture.runAsync(() -> processor.onPrice(ccyPair, rate))
            .exceptionally(e -> {
                // retry 3 times then give up
                LOGGER.severe(e.getLocalizedMessage());
                if (retry <= MAX_RETRIES) throttle(processor, rates, ccyPair, rate, retry + 1);
                return null;
            })
            .thenRun(() -> {
                throttle(processor, rates, ccyPair, rate, 0);
            });
    }

    private void throttle(PriceProcessor processor, Map<String, Double> rates, String ccyPair, double rate, int retry) {
        Double mapValue = rates.get(ccyPair);
        if (mapValue == rate) {
            rates.remove(ccyPair);
        } else {
            // Possible SO?
            throttleAsync(processor, rates, ccyPair, mapValue, retry);
        }
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        processors.putIfAbsent(priceProcessor, new ConcurrentHashMap<>());
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        processors.remove(priceProcessor);
    }
}
