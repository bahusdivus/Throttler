package com.price.processor;

import java.util.HashMap;
import java.util.Map;

public class PriceThrottler implements PriceProcessor {
    private final Map<PriceProcessor, FuturesHolder> processors = new HashMap<>();

    @Override
    public void onPrice(String ccyPair, double rate) {
        processors.forEach((processor, holder) -> {
            if (holder.hasActiveCurrent(ccyPair)) {
                holder.runNext(ccyPair, () -> processor.onPrice(ccyPair, rate));
            } else if (holder.hasActiveNext(ccyPair)) {
                holder.moveNextToCurrent(ccyPair);
                holder.runNext(ccyPair, () -> processor.onPrice(ccyPair, rate));
            } else {
                holder.runCurrent(ccyPair, () -> processor.onPrice(ccyPair, rate));
            }
        });
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        processors.putIfAbsent(priceProcessor, new FuturesHolder());
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        processors.remove(priceProcessor);
    }
}
