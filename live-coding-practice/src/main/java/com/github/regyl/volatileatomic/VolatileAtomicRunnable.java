package com.github.regyl.volatileatomic;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class VolatileAtomicRunnable implements Callable<Integer> {

    private final State state;

    @Override
    public Integer call() throws Exception {
        state.v++;
        return state.v;
    }
}
