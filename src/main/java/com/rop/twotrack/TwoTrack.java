package com.rop.twotrack;

import java.util.function.Function;

import com.rop.data.Result;

public interface TwoTrack<SI, SO, FIO> extends Function<Result<SI, FIO>, Result<SO, FIO>> {

  @Override
  public Result<SO, FIO> apply(Result<SI, FIO> ri);

  public default Result<SO, FIO> applyTo(SI i) {
    return apply(Result.ok(i));
  }

  public default Result<SO, FIO> applyTo(SI i, Function<Exception, FIO> exceptionHandler) {
    try {
      return apply(Result.ok(i));
    } catch (Exception e) {
      return Result.fail(exceptionHandler.apply(e));
    }
  }
}
