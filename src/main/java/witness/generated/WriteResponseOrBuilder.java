// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: witness_v1.proto

package witness.generated;

public interface WriteResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:WriteResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   **
   *if(requestVersion &gt;= localVersion) {
   * writeData
   * update localversion i.e localVersion = requestVersion
   *}
   *else {
   *return localVersion;
   *}
   * </pre>
   *
   * <code>uint64 version = 1;</code>
   */
  long getVersion();
}
