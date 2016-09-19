package com.rop.twotrack;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.rop.data.Result;

@Test
public class TwoTrackStringTest {

  @Test
  public void happyPath() {
    // userId = 1 makes everything go perfect
    User request = new User(1L, "juan", "juan@perez.com");
    TwoTrack<User, User, String> validateRequestTT = TwoTracks.fromSwitch(TwoTrackStringTest::validateRequest);
    TwoTrack<User, User, String> canonicalizeEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::canonicalizeEmail);
    TwoTrack<User, User, String> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackStringTest::updateDb);
    TwoTrack<User, String, String> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::sendEmail,
        e -> "Error enviando el mail " + e.getMessage());

    TwoTrack<User, String, String> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT, updateDbTT,
        sendEmailTT);

    Result<String, String> result = allTT.applyTo(request, e -> "Really unexpected error");

    Assert.assertNull(result.failure());
    Assert.assertEquals(result.success(), "OK");
  }

  @Test
  public void invalidId() {
    // userId = -1 makes validation fail, this is an expected error
    User request = new User(-1L, "juan", "juan@perez.com");
    TwoTrack<User, User, String> validateRequestTT = TwoTracks.fromSwitch(TwoTrackStringTest::validateRequest);
    TwoTrack<User, User, String> canonicalizeEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::canonicalizeEmail);
    TwoTrack<User, User, String> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackStringTest::updateDb);
    TwoTrack<User, String, String> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::sendEmail,
        e -> "Error enviando el mail " + e.getMessage());

    TwoTrack<User, String, String> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT, updateDbTT,
        sendEmailTT);

    Result<String, String> result = allTT.applyTo(request, e -> "Really unexpected error");

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure(), "id should be positive");
  }

  @Test
  public void invalidName() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, " ", "juan@perez.com");
    TwoTrack<User, User, String> validateRequestTT = TwoTracks.fromSwitch(TwoTrackStringTest::validateRequest);
    TwoTrack<User, User, String> canonicalizeEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::canonicalizeEmail);
    TwoTrack<User, User, String> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackStringTest::updateDb);
    TwoTrack<User, String, String> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::sendEmail,
        e -> "Error enviando el mail " + e.getMessage());

    TwoTrack<User, String, String> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT, updateDbTT,
        sendEmailTT);

    Result<String, String> result = allTT.applyTo(request, e -> "Really unexpected error");

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure(), "name can not be blank");
  }

  @Test
  public void expectedException() {
    // userId = 2 makes sending email fail, this is an unexpected error in
    // sendEmail, but handled by TwoTraks.fromOneTrack
    User request = new User(2L, "juan", "juan@perez.com");
    TwoTrack<User, User, String> validateRequestTT = TwoTracks.fromSwitch(TwoTrackStringTest::validateRequest);
    TwoTrack<User, User, String> canonicalizeEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::canonicalizeEmail);
    TwoTrack<User, User, String> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackStringTest::updateDb);
    TwoTrack<User, String, String> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::sendEmail,
        e -> "Error enviando el mail: " + e.getMessage());

    TwoTrack<User, String, String> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT, updateDbTT,
        sendEmailTT);

    Result<String, String> result = allTT.applyTo(request, e -> "Really unexpected error");

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure(), "Error enviando el mail: email bad");
  }

  @Test
  public void unexpectedException() {
    // userId = 3 makes sending email fail, this is an unexpected error in
    // validateRequest, but handled by TwoTraks.applyTo
    User request = new User(3L, "juan", "juan@perez.com");
    TwoTrack<User, User, String> validateRequestTT = TwoTracks.fromSwitch(TwoTrackStringTest::validateRequest);
    TwoTrack<User, User, String> canonicalizeEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::canonicalizeEmail);
    TwoTrack<User, User, String> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackStringTest::updateDb);
    TwoTrack<User, String, String> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::sendEmail,
        e -> "Error enviando el mail: " + e.getMessage());

    TwoTrack<User, String, String> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT, updateDbTT,
        sendEmailTT);

    Result<String, String> result = allTT.applyTo(request, e -> "Really unexpected error: " + e.getMessage());

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure(), "Really unexpected error: Not expected at all");
  }

  @Test
  public void invalidNameAndEmail() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, " ", "    ");
    TwoTrack<User, User, String> validateRequestTT = composedValidateRequest();
    TwoTrack<User, User, String> canonicalizeEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::canonicalizeEmail);
    TwoTrack<User, User, String> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackStringTest::updateDb);
    TwoTrack<User, String, String> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::sendEmail,
        e -> "Error enviando el mail " + e.getMessage());

    TwoTrack<User, String, String> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT, updateDbTT,
        sendEmailTT);

    Result<String, String> result = allTT.applyTo(request, e -> {
      e.printStackTrace();
      return "Really unexpected error";
    });

    Assert.assertNull(result.success());
    Assert.assertEquals(result.failure(), "name can not be blank, email can not be blank");
  }

  @Test
  public void happyPathComposed() {
    // userName empty makes validation fail, this is an expected error
    User request = new User(1L, "juan", "email@mi.com");
    TwoTrack<User, User, String> validateRequestTT = composedValidateRequest();
    TwoTrack<User, User, String> canonicalizeEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::canonicalizeEmail);
    TwoTrack<User, User, String> updateDbTT = TwoTracks.fromDeadEnd(TwoTrackStringTest::updateDb);
    TwoTrack<User, String, String> sendEmailTT = TwoTracks.fromOneTrack(TwoTrackStringTest::sendEmail,
        e -> "Error enviando el mail " + e.getMessage());

    TwoTrack<User, String, String> allTT = TwoTracks.compose(validateRequestTT, canonicalizeEmailTT, updateDbTT,
        sendEmailTT);

    Result<String, String> result = allTT.applyTo(request, e -> "Really unexpected error");

    Assert.assertNull(result.failure());
    Assert.assertEquals(result.success(), "OK");
  }

  private static Result<User, String> validateRequest(User request) {
    if (request.getId() < 0) {
      return Result.failure("id should be positive");
    }
    if (request.getName() == null || request.getName().trim().length() == 0) {
      return Result.failure("name can not be blank");
    }
    if (request.getEmail() == null || request.getEmail().trim().length() == 0) {
      return Result.failure("email can not be blank");
    }
    if (request.getId() == 3) {
      throw new RuntimeException("Not expected at all");
    }
    return Result.success(request);
  }

  private static TwoTrack<User, User, String> composedValidateRequest() {
    Function<User, Result<User, String>> validateId = (r -> r.getId() < 0 ? Result.failure("id should be positive")
        : Result.success(r));
    Function<User, Result<User, String>> validateName = (r -> r.getName() == null || r.getName().trim().length() == 0
        ? Result.failure("name can not be blank") : Result.success(r));
    Function<User, Result<User, String>> validateEmail = (r -> r.getEmail() == null || r.getEmail().trim().length() == 0
        ? Result.failure("email can not be blank") : Result.success(r));

    TwoTrack<User, User, String> validateIdTT = TwoTracks.fromSwitch(validateId);
    TwoTrack<User, User, String> validateNameTT = TwoTracks.fromSwitch(validateName);
    TwoTrack<User, User, String> validateEmailTT = TwoTracks.fromSwitch(validateEmail);

    BiFunction<User, User, User> successAnd = (u1, u2) -> u1;
    BiFunction<String, String, String> failureAnd = (s1, s2) -> s1 + ", " + s2;

    return TwoTracks.and(validateIdTT, validateNameTT, validateEmailTT, successAnd, failureAnd);

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
