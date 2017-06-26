package com.rop.twotrack;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.rop.data.Result;
import com.rop.function.ThrowingConsumer;
import com.rop.function.ThrowingFunction;

public class TwoTracks {

  public static <SI, SO, FIO> TwoTrack<SI, SO, FIO> fromSwitch(Function<SI, Result<SO, FIO>> f) {
    return new TwoTrack<SI, SO, FIO>() {
      @SuppressWarnings("unchecked")
      @Override
      public Result<SO, FIO> apply(Result<SI, FIO> ri) {
        if (ri instanceof Result.Fail) {
          return (Result.Fail<SO, FIO>) ri;
        }
        return f.apply(ri.ok());
      }
    };
  };

  public static <SI, SO, FIO> TwoTrack<SI, SO, FIO> fromSwitch(ThrowingFunction<SI, Result<SO, FIO>> f,
      Function<Exception, FIO> exceptionHandler) {
    return new TwoTrack<SI, SO, FIO>() {
      @SuppressWarnings("unchecked")
      @Override
      public Result<SO, FIO> apply(Result<SI, FIO> ri) {
        if (ri instanceof Result.Fail) {
          return (Result.Fail<SO, FIO>) ri;
        }
        try {
          return f.apply(ri.ok());
        } catch (Exception e) {
          return Result.fail(exceptionHandler.apply(e));
        }
      }
    };
  };

  // TODO: add class parameter for failure
  public static <SI, SO, FIO> TwoTrack<SI, SO, FIO> fromOneTrack(Function<SI, SO> f) {
    return new TwoTrack<SI, SO, FIO>() {
      @SuppressWarnings("unchecked")
      @Override
      public Result<SO, FIO> apply(Result<SI, FIO> ri) {
        if (ri instanceof Result.Fail) {
          return (Result.Fail<SO, FIO>) ri;
        }
        return Result.ok(f.apply(ri.ok()));
      }
    };
  }

  public static <SI, SO, FIO> TwoTrack<SI, SO, FIO> fromOneTrack(ThrowingFunction<SI, SO> f,
      Function<Exception, FIO> exceptionHandler) {
    return new TwoTrack<SI, SO, FIO>() {
      @SuppressWarnings("unchecked")
      @Override
      public Result<SO, FIO> apply(Result<SI, FIO> ri) {
        if (ri instanceof Result.Fail) {
          return (Result.Fail<SO, FIO>) ri;
        }
        try {
          return Result.ok(f.apply(ri.ok()));
        } catch (Exception ex) {
          return Result.fail(exceptionHandler.apply(ex));
        }
      }
    };
  }

  ;

  public static <SIO, FIO> TwoTrack<SIO, SIO, FIO> fromDeadEnd(Consumer<SIO> f) {
    return new TwoTrack<SIO, SIO, FIO>() {
      @Override
      public Result<SIO, FIO> apply(Result<SIO, FIO> ri) {
        if (ri instanceof Result.Fail) {
          return (Result.Fail<SIO, FIO>) ri;
        }
        SIO i = ri.ok();
        f.accept(i);
        return Result.ok(i);
      }
    };
  }

  // TODO: add class parameter for failure
  public static <SIO, FIO> TwoTrack<SIO, SIO, FIO> fromDeadEnd(ThrowingConsumer<SIO> f,
      Function<Exception, FIO> exceptionHandler) {
    return new TwoTrack<SIO, SIO, FIO>() {
      @Override
      public Result<SIO, FIO> apply(Result<SIO, FIO> ri) {
        if (ri instanceof Result.Fail) {
          return (Result.Fail<SIO, FIO>) ri;
        }
        SIO i = ri.ok();
        try {
          f.accept(i);
          return Result.ok(i);
        } catch (Exception ex) {
          return Result.fail(exceptionHandler.apply(ex));
        }

      }
    };
  }

  @SuppressWarnings("unchecked")
  public static <S1, S2, S3, F> TwoTrack<S1, S3, F> compose(TwoTrack<S1, S2, F> f1, TwoTrack<S2, S3, F> f2) {
    return (Result<S1, F> i) -> {
      Result<S2, F> o1 = f1.apply(i);
      if (o1 instanceof Result.Fail) {
        return (Result.Fail<S3, F>) o1;
      }
      return f2.apply(o1);
    };
  }

  public static <S1, S2, S3, S4, F> TwoTrack<S1, S4, F> compose(TwoTrack<S1, S2, F> f12, TwoTrack<S2, S3, F> f23,
      TwoTrack<S3, S4, F> f34) {
    TwoTrack<S1, S3, F> f13 = compose(f12, f23);
    return compose(f13, f34);
  }

  public static <S1, S2, S3, S4, S5, F> TwoTrack<S1, S5, F> compose(TwoTrack<S1, S2, F> f12, TwoTrack<S2, S3, F> f23,
      TwoTrack<S3, S4, F> f34, TwoTrack<S4, S5, F> f45) {
    TwoTrack<S1, S4, F> f14 = compose(f12, f23, f34);
    return compose(f14, f45);
  }

  public static <SI, SO, FIO> TwoTrack<SI, SO, FIO> and(TwoTrack<SI, SO, FIO> f1, TwoTrack<SI, SO, FIO> f2,
      BiFunction<SO, SO, SO> successAnd, BiFunction<FIO, FIO, FIO> failureAnd) {
    return (Result<SI, FIO> i) -> {
      Result<SO, FIO> o1 = f1.apply(i);
      Result<SO, FIO> o2 = f2.apply(i);
      if (o1 instanceof Result.Fail && o2 instanceof Result.Fail) {
        return Result.fail(failureAnd.apply(o1.fail(), o2.fail()));
      }
      if (o1 instanceof Result.Fail) {
        return Result.fail(o1.fail());
      }
      if (o2 instanceof Result.Fail) {
        return Result.fail(o2.fail());
      }
      return Result.ok(successAnd.apply(o1.ok(), o2.ok()));
    };
  }

  public static <SI, SO, FIO> TwoTrack<SI, SO, FIO> and(TwoTrack<SI, SO, FIO> f1, TwoTrack<SI, SO, FIO> f2,
      TwoTrack<SI, SO, FIO> f3, BiFunction<SO, SO, SO> successAnd, BiFunction<FIO, FIO, FIO> failureAnd) {
    TwoTrack<SI, SO, FIO> f12 = and(f1, f2, successAnd, failureAnd);
    return and(f12, f3, successAnd, failureAnd);
  }

  public static <SI, SO, FIO> TwoTrack<SI, SO, FIO> and(TwoTrack<SI, SO, FIO> f1, TwoTrack<SI, SO, FIO> f2,
      TwoTrack<SI, SO, FIO> f3, TwoTrack<SI, SO, FIO> f4, BiFunction<SO, SO, SO> successAnd,
      BiFunction<FIO, FIO, FIO> failureAnd) {
    TwoTrack<SI, SO, FIO> f123 = and(f1, f2, f3, successAnd, failureAnd);
    return and(f123, f4, successAnd, failureAnd);
  }
}
