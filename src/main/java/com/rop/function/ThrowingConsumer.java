package com.rop.function;

public interface ThrowingConsumer<I> {

  void accept(I input) throws Exception;
}