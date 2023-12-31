// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/protoc/namenode.proto

package com.rothsCode.litehdfs.common.protoc;

public interface ProtoFileInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ProtoFileInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 fileSize = 1;</code>
   *
   * @return The fileSize.
   */
  int getFileSize();

  /**
   * <code>string fileType = 2;</code>
   *
   * @return The fileType.
   */
  String getFileType();

  /**
   * <code>string fileType = 2;</code>
   *
   * @return The bytes for fileType.
   */
  com.google.protobuf.ByteString
  getFileTypeBytes();

  /**
   * <code>string fileName = 3;</code>
   *
   * @return The fileName.
   */
  String getFileName();

  /**
   * <code>string fileName = 3;</code>
   *
   * @return The bytes for fileName.
   */
  com.google.protobuf.ByteString
  getFileNameBytes();

  /**
   * <code>string parentFileName = 4;</code>
   *
   * @return The parentFileName.
   */
  String getParentFileName();

  /**
   * <code>string parentFileName = 4;</code>
   *
   * @return The bytes for parentFileName.
   */
  com.google.protobuf.ByteString
  getParentFileNameBytes();

  /**
   * <code>string absolutePath = 5;</code>
   *
   * @return The absolutePath.
   */
  String getAbsolutePath();

  /**
   * <code>string absolutePath = 5;</code>
   *
   * @return The bytes for absolutePath.
   */
  com.google.protobuf.ByteString
  getAbsolutePathBytes();

  /**
   * <code>int64 createTime = 6;</code>
   *
   * @return The createTime.
   */
  long getCreateTime();

  /**
   * <code>int64 updateTime = 7;</code>
   *
   * @return The updateTime.
   */
  long getUpdateTime();

  /**
   * <code>string createUser = 8;</code>
   *
   * @return The createUser.
   */
  String getCreateUser();

  /**
   * <code>string createUser = 8;</code>
   *
   * @return The bytes for createUser.
   */
  com.google.protobuf.ByteString
  getCreateUserBytes();

  /**
   * <code>string updateUser = 9;</code>
   *
   * @return The updateUser.
   */
  String getUpdateUser();

  /**
   * <code>string updateUser = 9;</code>
   *
   * @return The bytes for updateUser.
   */
  com.google.protobuf.ByteString
  getUpdateUserBytes();

  /**
   * <code>string hostName = 10;</code>
   *
   * @return The hostName.
   */
  String getHostName();

  /**
   * <code>string hostName = 10;</code>
   *
   * @return The bytes for hostName.
   */
  com.google.protobuf.ByteString
  getHostNameBytes();

  /**
   * <code>repeated string blkDataNodes = 11;</code>
   *
   * @return A list containing the blkDataNodes.
   */
  java.util.List<String>
  getBlkDataNodesList();

  /**
   * <code>repeated string blkDataNodes = 11;</code>
   *
   * @return The count of blkDataNodes.
   */
  int getBlkDataNodesCount();

  /**
   * <code>repeated string blkDataNodes = 11;</code>
   *
   * @param index The index of the element to return.
   * @return The blkDataNodes at the given index.
   */
  String getBlkDataNodes(int index);

  /**
   * <code>repeated string blkDataNodes = 11;</code>
   *
   * @param index The index of the value to return.
   * @return The bytes of the blkDataNodes at the given index.
   */
  com.google.protobuf.ByteString
  getBlkDataNodesBytes(int index);
}
