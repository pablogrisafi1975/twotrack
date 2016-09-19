package com.rop.function;

public interface ThrowingFunction<I, O> {

  O apply(I input) throws Exception;
}