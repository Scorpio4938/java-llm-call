package io.github.scorpio4938.LLMCall.core.retry;

import org.junit.jupiter.api.Test;

import io.github.scorpio4938.LLMCall.core.retry.DefaultRetry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRetryTest {

    @Test
    void shouldCreateDefaultConfiguration() {
        DefaultRetry config = new DefaultRetry();

        assertEquals(3, config.getMaxRetries());
        assertEquals(1000, config.getRetryDelayMillis());
        assertEquals(Duration.ofSeconds(30), config.getConnectionTimeout());
    }

    @Test
    void shouldCreateCustomConfiguration() {
        DefaultRetry config = new DefaultRetry(5, 2, TimeUnit.SECONDS);

        assertEquals(5, config.getMaxRetries());
        assertEquals(2000, config.getRetryDelayMillis());
    }

    @Test
    void shouldUpdateConfiguration() {
        DefaultRetry config = new DefaultRetry();
        config.update(10, 500);

        assertEquals(10, config.getMaxRetries());
        assertEquals(500, config.getRetryDelayMillis());
    }

    @Test
    void shouldSetConnectionTimeout() {
        DefaultRetry config = new DefaultRetry();
        config.setConnectionTimeout(Duration.ofSeconds(60));

        assertEquals(Duration.ofSeconds(60), config.getConnectionTimeout());
    }

    @Test
    void shouldValidateParameters() {
        DefaultRetry config = new DefaultRetry();

        assertThrows(IllegalArgumentException.class, () -> config.update(-1, 1000));

        assertThrows(IllegalArgumentException.class, () -> config.update(3, -100));

        assertThrows(IllegalArgumentException.class, () -> config.setConnectionTimeout(Duration.ofSeconds(-10)));
    }

    @Test
    void shouldCreateExponentialBackoff() {
        DefaultRetry config = DefaultRetry.withExponentialBackoff(3, 100, 2.0);

        assertEquals(3, config.getMaxRetries());
        assertEquals(100, config.getRetryDelayMillis());

        // Test exponential growth
        assertEquals(100, config.getDelayForAttempt(1));
        assertEquals(200, config.getDelayForAttempt(2));
        assertEquals(400, config.getDelayForAttempt(3));
    }

    @Test
    void shouldValidateBackoffFactor() {
        assertThrows(IllegalArgumentException.class, () -> DefaultRetry.withExponentialBackoff(3, 100, 0.5));

        assertThrows(IllegalArgumentException.class, () -> DefaultRetry.withExponentialBackoff(3, 100, 1.0));
    }
}