package com.rop.twotrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.rop.data.FailMsg;
import com.rop.data.Result;

@Test
public class TwoTrackFailureMsgListTest {

  @Test
  public void happyPath() {
    // userId = 1 makes everything go perfect
    User request = new User(1L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);

    TwoTrack<User, User, List<FailMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);

    TwoTrack<User, User, List<FailMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);

    TwoTrack<User, String, List<FailMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailMsg>> result = allTT.applyTo(request,
        e -> FailMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.fail());
    Assert.assertEquals(result.ok(), "OK");
  }

  @Test
  public void invalidId() {
    // userId = -1 makes validation fail, this is an expected error
    User request = new User(-1L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailMsg>> result = allTT.applyTo(request,
        e -> FailMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.ok());
    Assert.assertEquals(result.fail().get(0).getKey(), "ID_NON_POSITIVE");
    Assert.assertEquals(result.fail().get(0).getParams().get(0), -1L);
  }

  @Test
  public void invalidEmail() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, "juan", "juan_perez.com");
    TwoTrack<User, User, List<FailMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailMsg>> result = allTT.applyTo(request,
        e -> FailMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.ok());

    Assert.assertEquals(result.fail().get(0).getKey(), "EMAIL_NO_@");
    Assert.assertEquals(result.fail().get(0).getParams().get(0), "juan_perez.com");
  }

  @Test
  public void expectedException() {
    // userId = 2 makes sending email fail, this is an unexpected error in
    // sendEmail, but handled by TwoTraks.fromOneTrack
    User request = new User(2L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailMsg>> result = allTT.applyTo(request,
        e -> FailMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.ok());
    Assert.assertEquals(result.fail().get(0).getKey(), "ERROR_SENDING_MAIL");
    Assert.assertEquals(result.fail().get(0).getParams().get(0), "email bad");
  }

  @Test
  public void unexpectedException() {
    // userId = 3 makes sending email fail, this is an unexpected error in
    // validateRequest, but handled by TwoTraks.applyTo
    User request = new User(3L, "juan", "juan@perez.com");
    TwoTrack<User, User, List<FailMsg>> validateRequestTT = TwoTracks
        .fromSwitch(TwoTrackFailureMsgListTest::validateRequest);
    TwoTrack<User, User, List<FailMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailMsg>> result = allTT.applyTo(request,
        e -> FailMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.ok());
    Assert.assertEquals(result.fail().get(0).getKey(), "UNEXPECTED_ERROR");
    Assert.assertEquals(result.fail().get(0).getParams().get(0), "Not expected at all");
  }

  @Test
  public void invalidNameAndEmail() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, " ", "wrong");
    TwoTrack<User, User, List<FailMsg>> validateRequestTT = composedValidateRequest();
    TwoTrack<User, User, List<FailMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailMsg>> result = allTT.applyTo(request,
        e -> FailMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.ok());
    Assert.assertEquals(result.fail().get(0).getKey(), "NAME_IS_BLANK");
    Assert.assertEquals(result.fail().get(1).getKey(), "EMAIL_NO_@");
    Assert.assertEquals(result.fail().get(1).getParams().get(0), "wrong");
  }

  @Test
  public void happyPathComposed() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, "juan", "email@mi.com");
    TwoTrack<User, User, List<FailMsg>> validateRequestTT = composedValidateRequest();
    TwoTrack<User, User, List<FailMsg>> canonicalizeEmailTT = TwoTracks
        .fromOneTrack(TwoTrackFailureMsgListTest::canonicalizeEmail);
    TwoTrack<User, User, List<FailMsg>> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackFailureMsgListTest::updateDb);
    TwoTrack<User, String, List<FailMsg>> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackFailureMsgListTest::sendEmail,
        e -> FailMsg.of("ERROR_SENDING_MAIL", e.getMessage()).toList());

    TwoTrack<User, String, List<FailMsg>> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT,
        updateDbTT, sendEmailTT);

    Result<String, List<FailMsg>> result = allTT.applyTo(request,
        e -> FailMsg.of("UNEXPECTED_ERROR", e.getMessage()).toList());

    Assert.assertNull(result.fail());
    Assert.assertEquals(result.ok(), "OK");
  }

  private static Result<User, List<FailMsg>> validateRequest(User request) {
    if (request.getId() < 0) {
      return Result.fail(FailMsg.of("ID_NON_POSITIVE", request.getId()).toList());
    }
    if (request.getName() == null || request.getName().trim().length() == 0) {
      return Result.fail(FailMsg.of("NAME_IS_BLANK").toList());
    }
    if (request.getEmail() == null || !request.getEmail().contains("@")) {
      return Result.fail(FailMsg.of("EMAIL_NO_@", request.getEmail()).toList());
    }
    if (request.getId() == 3) {
      throw new RuntimeException("Not expected at all");
    }
    return Result.ok(request);
  }

  private static TwoTrack<User, User, List<FailMsg>> composedValidateRequest() {
    Function<User, Result<User, List<FailMsg>>> validateId = (r -> r.getId() < 0
        ? Result.fail(FailMsg.of("ID_NON_POSITIVE", r.getId()).toList()) : Result.ok(r));
    Function<User, Result<User, List<FailMsg>>> validateName = (r -> r.getName() == null
        || r.getName().trim().length() == 0 ? Result.fail(FailMsg.of("NAME_IS_BLANK").toList())
            : Result.ok(r));
    Function<User, Result<User, List<FailMsg>>> validateEmail = (r -> r.getEmail() == null
        || !r.getEmail().contains("@") ? Result.fail(FailMsg.of("EMAIL_NO_@", r.getEmail()).toList())
            : Result.ok(r));

    TwoTrack<User, User, List<FailMsg>> validateIdTT = TwoTracks.fromSwitch(validateId);
    TwoTrack<User, User, List<FailMsg>> validateNameTT = TwoTracks.fromSwitch(validateName);
    TwoTrack<User, User, List<FailMsg>> validateEmailTT = TwoTracks.fromSwitch(validateEmail);

    BiFunction<User, User, User> successAnd = (u1, u2) -> u1;
    BiFunction<List<FailMsg>, List<FailMsg>, List<FailMsg>> failureAnd = (f1, f2) -> concat(f1, f2);

    return TwoTracks.and(validateIdTT, validateNameTT, validateEmailTT, successAnd, failureAnd);

  }

  private static List<FailMsg> concat(List<FailMsg> f1, List<FailMsg> f2) {
    ArrayList<FailMsg> list = new ArrayList<>();
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
