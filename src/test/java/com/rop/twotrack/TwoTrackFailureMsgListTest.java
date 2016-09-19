package com.rop.twotrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.rop.data.FailureMsg;
import com.rop.data.Result;

@Test
public class TwoTrackFailureMsgListTest {

  @Test
  public void happyPath() {
    // userId = 1 makes everything go perfect
    User request = new User(1L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailureMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);

    TwoTrack<User, User, List<FailureMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);

    TwoTrack<User, User, List<FailureMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);

    TwoTrack<User, String, List<FailureMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailureMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailureMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailureMsg>> result = allTT.applyTo(request,
        e -> FailureMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.failure());
    Assert.assertEquals(result.success(), "OK");
  }

  @Test
  public void invalidId() {
    // userId = -1 makes validation fail, this is an expected error
    User request = new User(-1L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailureMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailureMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailureMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailureMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailureMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailureMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailureMsg>> result = allTT.applyTo(request,
        e -> FailureMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure().get(0).getKey(), "ID_NON_POSITIVE");
    Assert.assertEquals(result.failure().get(0).getParams().get(0), -1L);
  }

  @Test
  public void invalidEmail() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, "juan", "juan_perez.com");
    TwoTrack<User, User, List<FailureMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailureMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailureMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailureMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailureMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailureMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailureMsg>> result = allTT.applyTo(request,
        e -> FailureMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.success());

    Assert.assertEquals(result.failure().get(0).getKey(), "EMAIL_NO_@");
    Assert.assertEquals(result.failure().get(0).getParams().get(0), "juan_perez.com");
  }

  @Test
  public void expectedException() {
    // userId = 2 makes sending email fail, this is an unexpected error in
    // sendEmail, but handled by TwoTraks.fromOneTrack
    User request = new User(2L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailureMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailureMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailureMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailureMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailureMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailureMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailureMsg>> result = allTT.applyTo(request,
        e -> FailureMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure().get(0).getKey(), "ERROR_SENDING_MAIL");
    Assert.assertEquals(result.failure().get(0).getParams().get(0), "email bad");
  }

  @Test
  public void unexpectedException() {
    // userId = 3 makes sending email fail, this is an unexpected error in
    // validateRequest, but handled by TwoTraks.applyTo
    User request = new User(3L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailureMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailureMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailureMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailureMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailureMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailureMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailureMsg>> result = allTT.applyTo(request,
        e -> FailureMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure().get(0).getKey(), "UNEXPECTED_ERROR");
    Assert.assertEquals(result.failure().get(0).getParams().get(0), "Not expected at all");
  }

  @Test
  public void invalidNameAndEmail() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, " ", "wrong");
    TwoTrack<User, User, List<FailureMsg>> validateRequestTT = composedValidateRequest();
    TwoTrack<User, User, List<FailureMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailureMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailureMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailureMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailureMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailureMsg>> result = allTT.applyTo(request,
        e -> FailureMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure().get(0).getKey(), "NAME_IS_BLANK");
    Assert.assertEquals(result.failure().get(1).getKey(), "EMAIL_NO_@");
    Assert.assertEquals(result.failure().get(1).getParams().get(0), "wrong");
  }

  @Test
  public void happyPathComposed() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, "juan", "email@mi.com");
    TwoTrack<User, User, List<FailureMsg>> validateRequestTT = composedValidateRequest();
    TwoTrack<User, User, List<FailureMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailureMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailureMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailureMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailureMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailureMsg>> result = allTT.applyTo(request,
        e -> FailureMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.failure());
    Assert.assertEquals(result.success(), "OK");
  }

  private static Result<User, List<FailureMsg>> validateRequest(User request) {
    if (request.getId() < 0) {
      return Result.failure(FailureMsg.of("ID_NON_POSITIVE", request.getId()).toList());
    }
    if (request.getName() == null || request.getName().trim().length() == 0) {
      return Result.failure(FailureMsg.of("NAME_IS_BLANK").toList());
    }
    if (request.getEmail() == null || !request.getEmail().contains("@")) {
      return Result.failure(FailureMsg.of("EMAIL_NO_@", request.getEmail()).toList());
    }
    if (request.getId() == 3) {
      throw new RuntimeException("Not expected at all");
    }
    return Result.success(request);
  }

  private static TwoTrack<User, User, List<FailureMsg>> composedValidateRequest() {
    Function<User, Result<User, List<FailureMsg>>> validateId = (r -> r.getId() < 0
        ? Result.failure(FailureMsg.of("ID_NON_POSITIVE", r.getId()).toList()) : Result.success(r));
    Function<User, Result<User, List<FailureMsg>>> validateName = (r -> r.getName() == null
        || r.getName().trim().length() == 0 ? Result.failure(FailureMsg.of("NAME_IS_BLANK").toList())
            : Result.success(r));
    Function<User, Result<User, List<FailureMsg>>> validateEmail = (r -> r.getEmail() == null
        || !r.getEmail().contains("@") ? Result.failure(FailureMsg.of("EMAIL_NO_@", r.getEmail()).toList())
            : Result.success(r));

    TwoTrack<User, User, List<FailureMsg>> validateIdTT = TwoTracks.fromSwitch(validateId);
    TwoTrack<User, User, List<FailureMsg>> validateNameTT = TwoTracks.fromSwitch(validateName);
    TwoTrack<User, User, List<FailureMsg>> validateEmailTT = TwoTracks.fromSwitch(validateEmail);

    BiFunction<User, User, User> successAnd = (u1, u2) -> u1;
    BiFunction<List<FailureMsg>, List<FailureMsg>, List<FailureMsg>> failureAnd = (f1, f2) -> concat(f1, f2);

    return TwoTracks.and(validateIdTT, validateNameTT, validateEmailTT, successAnd, failureAnd);

  }

  private static List<FailureMsg> concat(List<FailureMsg> f1, List<FailureMsg> f2) {
    ArrayList<FailureMsg> list = new ArrayList<>();
    list.addAll(f1);
    list.addAll(f2);
    return Collections.unmodifiableList(list);
  }

  private static User canonicalizeEmail(User request) {
    request.setEmail(request.getEmail().toLowerCase());
    return request;
  }

  private static void updateDb(User request) {
    System.out.println("Saving user " + request + " to db");
  }

  private static String sendEmail(User request) {
    System.out.println("Sending email to " + request.getEmail());
    if (request.getId() == 2) {
      throw new RuntimeException("email bad");
    }
    return "OK";
  }

}
