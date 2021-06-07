package com.price.processor;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PriceThrottlerTest {
    @Test
    public void testSubscribeUnsubscribe() throws InterruptedException {
        PriceProcessor mainProcessor = new PriceThrottler();
        TestProcessor receiver = new TestProcessor();

        mainProcessor.subscribe(receiver);
        mainProcessor.onPrice("test", 2);

        TimeUnit.SECONDS.sleep(2);
        assertEquals("test2.0", receiver.getTestValue());

        mainProcessor.unsubscribe(receiver);
        mainProcessor.onPrice("should not be received", 2);

        TimeUnit.SECONDS.sleep(2);
        assertEquals("test2.0", receiver.getTestValue());
    }

    @Test
    public void testSkipCallsOnSlowProcessor() throws InterruptedException {
        PriceProcessor mainProcessor = new PriceThrottler();
        TestProcessor receiver = new TestProcessor(2);
        mainProcessor.subscribe(receiver);
        mainProcessor.onPrice("test1_", 1);
        mainProcessor.onPrice("test1_", 2);
        mainProcessor.onPrice("test1_", 3);

        // first call in progress
        assertNull(receiver.getTestValue());

        // firs call complete, got 1st value
        TimeUnit.SECONDS.sleep(3);
        assertEquals("test1_1.0", receiver.getTestValue());

        // second call should be overwritten by 3rd
        TimeUnit.SECONDS.sleep(3);
        assertEquals("test1_3.0", receiver.getTestValue());

    }

    @Test
    public void testMoveNextCoCurrent() throws InterruptedException {
        PriceProcessor mainProcessor = new PriceThrottler();
        TestProcessor receiver = new TestProcessor(2);
        mainProcessor.subscribe(receiver);

        mainProcessor.onPrice("test1_", 1);
        mainProcessor.onPrice("test1_", 2);
        TimeUnit.SECONDS.sleep(3);
        mainProcessor.onPrice("test1_", 3);

        // firs call complete, got 1st value
        assertEquals("test1_1.0", receiver.getTestValue());

        // second call complete, got 2nd value
        TimeUnit.SECONDS.sleep(2);
        assertEquals("test1_2.0", receiver.getTestValue());

        // final value
        TimeUnit.SECONDS.sleep(2);
        assertEquals("test1_3.0", receiver.getTestValue());

    }

    @Test
    public void testException() throws InterruptedException {
        PriceProcessor mainProcessor = new PriceThrottler();
        TestProcessor receiver = new TestProcessor(1, (String ccyPair, Double rate) -> {
            throw new RuntimeException("Exception");
        });
        mainProcessor.subscribe(receiver);

        mainProcessor.onPrice("test1_", 1);
        TimeUnit.SECONDS.sleep(20);
    }
}
