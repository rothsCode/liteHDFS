// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/protoc/namenode.proto

package com.rothsCode.litehdfs.common.protoc;

public final class Namenode {

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ProtoNode_descriptor;
  static final
  com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ProtoNode_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ProtoNode_ChildNodesEntry_descriptor;
  static final
  com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ProtoNode_ChildNodesEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_OperateLog_descriptor;
  static final
  com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_OperateLog_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ProtoFileInfo_descriptor;
  static final
  com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ProtoFileInfo_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;

  static {
    String[] descriptorData = {
        "\n(src/main/resources/protoc/namenode.pro" +
            "to\"\276\001\n\tProtoNode\022\014\n\004path\030\001 \001(\t\022\022\n\nisFile" +
            "Node\030\002 \001(\010\022.\n\nchildNodes\030\003 \003(\0132\032.ProtoNo" +
            "de.ChildNodesEntry\022 \n\010fileInfo\030\004 \001(\0132\016.P" +
            "rotoFileInfo\032=\n\017ChildNodesEntry\022\013\n\003key\030\001" +
            " \001(\t\022\031\n\005value\030\002 \001(\0132\n.ProtoNode:\0028\001\"_\n\nO" +
            "perateLog\022\023\n\013operateType\030\001 \001(\t\022\014\n\004path\030\002" +
            " \001(\t\022\014\n\004txId\030\003 \001(\003\022 \n\010fileInfo\030\004 \001(\0132\016.P"
            +
            "rotoFileInfo\"\353\001\n\rProtoFileInfo\022\020\n\010fileSi" +
            "ze\030\001 \001(\005\022\020\n\010fileType\030\002 \001(\t\022\020\n\010fileName\030\003"
            +
            " \001(\t\022\026\n\016parentFileName\030\004 \001(\t\022\024\n\014absolute" +
            "Path\030\005 \001(\t\022\022\n\ncreateTime\030\006 \001(\003\022\022\n\nupdate" +
            "Time\030\007 \001(\003\022\022\n\ncreateUser\030\010 \001(\t\022\022\n\nupdate" +
            "User\030\t \001(\t\022\020\n\010hostName\030\n \001(\t\022\024\n\014blkDataN" +
            "odes\030\013 \003(\tB*\n$com.rothsCode.litehdfs.com" +
            "mon.protocH\001P\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
        .internalBuildGeneratedFileFrom(descriptorData,
            new com.google.protobuf.Descriptors.FileDescriptor[]{
            });
    internal_static_ProtoNode_descriptor =
        getDescriptor().getMessageTypes().get(0);
    internal_static_ProtoNode_fieldAccessorTable = new
        com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ProtoNode_descriptor,
        new String[]{"Path", "IsFileNode", "ChildNodes", "FileInfo",});
    internal_static_ProtoNode_ChildNodesEntry_descriptor =
        internal_static_ProtoNode_descriptor.getNestedTypes().get(0);
    internal_static_ProtoNode_ChildNodesEntry_fieldAccessorTable = new
        com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ProtoNode_ChildNodesEntry_descriptor,
        new String[]{"Key", "Value",});
    internal_static_OperateLog_descriptor =
        getDescriptor().getMessageTypes().get(1);
    internal_static_OperateLog_fieldAccessorTable = new
        com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_OperateLog_descriptor,
        new String[]{"OperateType", "Path", "TxId", "FileInfo",});
    internal_static_ProtoFileInfo_descriptor =
        getDescriptor().getMessageTypes().get(2);
    internal_static_ProtoFileInfo_fieldAccessorTable = new
        com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ProtoFileInfo_descriptor,
        new String[]{"FileSize", "FileType", "FileName", "ParentFileName", "AbsolutePath",
            "CreateTime", "UpdateTime", "CreateUser", "UpdateUser", "HostName", "BlkDataNodes",});
  }

  private Namenode() {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
  getDescriptor() {
    return descriptor;
  }

  // @@protoc_insertion_point(outer_class_scope)
}
