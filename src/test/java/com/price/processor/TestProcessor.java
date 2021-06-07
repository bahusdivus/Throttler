package com.price.processor;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class TestProcessor implements PriceProcessor{

    private String testValue;
    private final int delay;
    private final BiFunction<String, Double, String> function;

    public TestProcessor() {
        this(0);
    }

    public TestProcessor(int delay) {
        this.delay = delay;
        this.function = (String ccyPair, Double rate) -> ccyPair + rate;
    }

    public TestProcessor(int delay, BiFunction<String, Double, String> function) {
        this.delay = delay;
        this.function = function;
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        System.out.printf("%s%s - Entering%n", ccyPair, rate);
        try {
            System.out.printf("%s%s - Going to sleep for %d seconds%n", ccyPair, rate, delay);
            TimeUnit.SECONDS.sleep(delay);
            System.out.printf("%s%s - Wakinge up%n", ccyPair, rate);
        } catch (InterruptedException ignored) {
            System.out.printf("%s%s - Sleep breaked%n", ccyPair, rate);
        }
        System.out.printf("%s%s - Setting The Value%n", ccyPair, rate);
        testValue = function.apply(ccyPair, rate);
        System.out.printf("%s%s - Exiting computation%n", ccyPair, rate);
    }

    public String getTestValue() {
        return testValue;
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {}

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {}
}
