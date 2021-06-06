package com.price.processor;

import java.util.concurrent.TimeUnit;

public class TestProcessor implements PriceProcessor{

    private String testValue;
    private final int delay;

    public TestProcessor() {
        this(0);
    }

    public TestProcessor(int delay) {
        this.delay = delay;
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        System.out.printf("%s%s - Enters%n", ccyPair, rate);
        try {
            System.out.printf("%s%s - Going to sleep for %d seconds%n", ccyPair, rate, delay);
            TimeUnit.SECONDS.sleep(delay);
            System.out.printf("%s%s - Woke up%n", ccyPair, rate);
        } catch (InterruptedException ignored) {
            System.out.printf("%s%s - Sleep break%n", ccyPair, rate);
        }
        System.out.printf("%s%s - Setting The Value%n", ccyPair, rate);
        testValue = ccyPair + rate;
        System.out.printf("%s%s - Exits computation%n", ccyPair, rate);
    }

    public String getTestValue() {
        return testValue;
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {}

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {}
}
