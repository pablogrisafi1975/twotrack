package com.rop.data;

public class Result<S, F> {

  private final S success;
  private final F failure;

  public S success() {
    return success;
  }

  public F failure() {
    return failure;
  }

  private Result(S success, F failure) {
    this.success = success;
    this.failure = failure;
  }

  public static <SS, FF> Success<SS, FF> success(SS success) {
    return new Success<>(success);
  }

  public static <SS, FF> Failure<SS, FF> failure(FF failure) {
    return new Failure<>(failure);
  }

  public static class Success<S, F> extends Result<S, F> {

    private Success(S sucess) {
      super(sucess, null);
    }

  }

  public static class Failure<S, F> extends Result<S, F> {
    private Failure(F failure) {
      super(null, failure);
    }
  }

}
