// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: platform.proto

package edu.kit.ipd.crowdcontrol.objectservice.proto;

public final class PlatformOuterClass {
  private PlatformOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_crowdcontrol_Platform_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_crowdcontrol_Platform_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016platform.proto\022\014crowdcontrol\">\n\010Platfo" +
      "rm\022\n\n\002id\030\001 \001(\t\022\014\n\004name\030\002 \001(\t\022\030\n\020has_cali" +
      "brations\030\003 \001(\010B0\n,edu.kit.ipd.crowdcontr" +
      "ol.objectservice.protoP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_crowdcontrol_Platform_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_crowdcontrol_Platform_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_crowdcontrol_Platform_descriptor,
        new java.lang.String[] { "Id", "Name", "HasCalibrations", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
