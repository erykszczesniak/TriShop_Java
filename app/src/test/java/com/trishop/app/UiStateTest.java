package com.trishop.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.trishop.app.core.UiState;

import org.junit.Test;

public class UiStateTest {

    @Test
    public void loadingStateIsSingleton() {
        UiState<Integer> a = UiState.loading();
        UiState<Integer> b = UiState.loading();
        assertSame(a, b);
    }

    @Test
    public void successStateHoldsData() {
        UiState<Integer> state = UiState.success(42);
        assertEquals(Integer.valueOf(42), ((UiState.Success<Integer>) state).getData());
    }

    @Test
    public void errorStateHoldsMessage() {
        UiState<Integer> state = UiState.error("oops");
        assertEquals("oops", ((UiState.Error<Integer>) state).getMessage());
    }
}
