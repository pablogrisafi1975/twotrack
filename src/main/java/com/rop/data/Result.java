package com.rop.data;

public class Result<O, F> {

  private final O ok;
  private final F fail;

  public O ok() {
    return ok;
  }

  public F fail() {
    return fail;
  }

  private Result(O ok, F fail) {
    this.ok = ok;
    this.fail = fail;
  }

  public static <OO, FF> Ok<OO, FF> ok(OO ok) {
    return new Ok<>(ok);
  }

  public static <OO, FF> Fail<OO, FF> fail(FF fail) {
    return new Fail<>(fail);
  }

  public static class Ok<O, F> extends Result<O, F> {

    private Ok(O ok) {
      super(ok, null);
    }

  }

  public static class Fail<O, F> extends Result<O, F> {
    private Fail(F fail) {
      super(null, fail);
    }
  }

}
