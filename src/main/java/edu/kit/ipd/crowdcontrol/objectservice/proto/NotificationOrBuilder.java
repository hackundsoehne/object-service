// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: notification.proto

package edu.kit.ipd.crowdcontrol.objectservice.proto;

public interface NotificationOrBuilder extends
    // @@protoc_insertion_point(interface_extends:crowdcontrol.Notification)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional int32 id = 1;</code>
   */
  int getId();

  /**
   * <code>optional string name = 2;</code>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 2;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional string description = 3;</code>
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 3;</code>
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>optional string query = 4;</code>
   */
  java.lang.String getQuery();
  /**
   * <code>optional string query = 4;</code>
   */
  com.google.protobuf.ByteString
      getQueryBytes();

  /**
   * <code>optional int32 check_period = 5;</code>
   */
  int getCheckPeriod();

  /**
   * <code>optional .crowdcontrol.Boolean send_once = 6;</code>
   */
  boolean hasSendOnce();
  /**
   * <code>optional .crowdcontrol.Boolean send_once = 6;</code>
   */
  edu.kit.ipd.crowdcontrol.objectservice.proto.Boolean getSendOnce();
  /**
   * <code>optional .crowdcontrol.Boolean send_once = 6;</code>
   */
  edu.kit.ipd.crowdcontrol.objectservice.proto.BooleanOrBuilder getSendOnceOrBuilder();

  /**
   * <code>repeated string emails = 7;</code>
   */
  com.google.protobuf.ProtocolStringList
      getEmailsList();
  /**
   * <code>repeated string emails = 7;</code>
   */
  int getEmailsCount();
  /**
   * <code>repeated string emails = 7;</code>
   */
  java.lang.String getEmails(int index);
  /**
   * <code>repeated string emails = 7;</code>
   */
  com.google.protobuf.ByteString
      getEmailsBytes(int index);
}
