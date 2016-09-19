package com.rop.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FailureMsg {
  private final String key;
  private final List<Object> params;

  private FailureMsg(String key, List<Object> params) {
    this.key = key;
    this.params = params;
  }

  public static FailureMsg of(String key, List<Object> params) {
    return new FailureMsg(key, params);
  }

  public static FailureMsg of(String key, Object... params) {
    return new FailureMsg(key, Arrays.asList(params));
  }

  public List<FailureMsg> toList() {
    return Collections.singletonList(this);
  }

  public String getKey() {
    return key;
  }

  public List<Object> getParams() {
    return params;
  }

}
