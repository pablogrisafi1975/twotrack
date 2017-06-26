package com.rop.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FailMsg {
  private final String key;
  private final List<Object> params;

  private FailMsg(String key, List<Object> params) {
    this.key = key;
    this.params = params;
  }

  public static FailMsg of(String key, List<Object> params) {
    return new FailMsg(key, params);
  }

  public static FailMsg of(String key, Object... params) {
    return new FailMsg(key, Arrays.asList(params));
  }

  public List<FailMsg> toList() {
    return Collections.singletonList(this);
  }

  public String getKey() {
    return key;
  }

  public List<Object> getParams() {
    return params;
  }

}
