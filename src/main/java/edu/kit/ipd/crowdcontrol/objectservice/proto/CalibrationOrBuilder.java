// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: calibration.proto

package edu.kit.ipd.crowdcontrol.objectservice.proto;

public interface CalibrationOrBuilder extends
    // @@protoc_insertion_point(interface_extends:crowdcontrol.Calibration)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string question = 1;</code>
   */
  java.lang.String getQuestion();
  /**
   * <code>optional string question = 1;</code>
   */
  com.google.protobuf.ByteString
      getQuestionBytes();

  /**
   * <code>repeated string answers = 2;</code>
   */
  com.google.protobuf.ProtocolStringList
      getAnswersList();
  /**
   * <code>repeated string answers = 2;</code>
   */
  int getAnswersCount();
  /**
   * <code>repeated string answers = 2;</code>
   */
  java.lang.String getAnswers(int index);
  /**
   * <code>repeated string answers = 2;</code>
   */
  com.google.protobuf.ByteString
      getAnswersBytes(int index);

  /**
   * <code>repeated string accepted_answers = 3;</code>
   */
  com.google.protobuf.ProtocolStringList
      getAcceptedAnswersList();
  /**
   * <code>repeated string accepted_answers = 3;</code>
   */
  int getAcceptedAnswersCount();
  /**
   * <code>repeated string accepted_answers = 3;</code>
   */
  java.lang.String getAcceptedAnswers(int index);
  /**
   * <code>repeated string accepted_answers = 3;</code>
   */
  com.google.protobuf.ByteString
      getAcceptedAnswersBytes(int index);
}
