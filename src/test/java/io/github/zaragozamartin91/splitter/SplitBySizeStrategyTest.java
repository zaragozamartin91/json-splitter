package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SplitBySizeStrategyTest {

    @Test
    public void testWithFlattenThrowsIllegalStateExceptionWhenFlattenIsTrue() {
        // GIVEN
        SplitBySizeStrategy strategy = new SplitBySizeStrategy();

        // WHEN / THEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> strategy.withFlatten(true)
        );
        assertEquals("flatten=true is not supported by SplitBySizeStrategy", exception.getMessage());
    }
}
