package com.autogeneratedby.polymorph;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import javax.crypto.Cipher;

public class allfieldsonce {
  static MessageDigest SHA1;

  static MessageDigest SHA256;

  static MessageDigest MD5;

  static Cipher AESEncrypt;

  static Cipher AESDecrypt;

  static {
    try {
      AESEncrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
      AESDecrypt = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
      SHA1 = java.security.MessageDigest.getInstance("SHA-1");
      SHA1.reset();
      SHA256 = java.security.MessageDigest.getInstance("SHA-256");
      SHA256.reset();
      MD5 = java.security.MessageDigest.getInstance("MD5");
      MD5.reset();
      AESEncrypt.init(javax.crypto.Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec("rgxcpqnxgbiobqktaxjvzfgr".getBytes(), "AES"));
      AESDecrypt.init(javax.crypto.Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec("rgxcpqnxgbiobqktaxjvzfgr".getBytes(), "AES"));
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
  }

  public P1.allfieldsonce.Builder wrappedMessage;

  public allfieldsonce build() {
    wrappedMessage.build();
    return this;
  }

  public static allfieldsonce newBuilder() {
    var newMsg = new allfieldsonce();
    newMsg.wrappedMessage = P1.allfieldsonce.newBuilder();
    return newMsg;
  }

  public allfieldsonce clear() {
    wrappedMessage.clear();
    return this;
  }

  @Override
  public String toString() {
    return wrappedMessage.toString();
  }

  public void setSomeDouble(double someDouble) {
    wrappedMessage.setSomeDouble(someDouble);
  }

  public double getSomeDouble() {
    return wrappedMessage.getSomeDouble();
  }

  public void setSomeFloat(float someFloat) {
    var asInt = Float.floatToIntBits(someFloat);
    wrappedMessage.setSomeFloatP1((int)asInt >> 16);
    wrappedMessage.setSomeFloatP2((int)asInt & 0xFFFF);
  }

  public float getSomeFloat() {
    var asInt = (wrappedMessage.getSomeFloatp1() << 16 | (wrappedMessage.getSomeFloatp2()) & 0xFFFF);
    return Float.intBitsToFloat(asInt);
  }

  public void setSomeInt32(int someInt32) {
    wrappedMessage.setSomeInt32P1((int)someInt32 >> 16);
    wrappedMessage.setSomeInt32P2((int)someInt32 & 0xFFFF);
  }

  public int getSomeInt32() {
    return (wrappedMessage.getSomeInt32P1() << 16 | (wrappedMessage.getSomeInt32P2()) & 0xFFFF);
  }

  public void setSomeInt64(long someInt64) {
    wrappedMessage.setSomeInt64(-someInt64);
  }

  public long getSomeInt64() {
    return -(wrappedMessage.getSomeInt64());
  }

  public void setSomeUint32(int someUint32) {
    wrappedMessage.setSomeUint32(someUint32);
  }

  public int getSomeUint32() {
    return wrappedMessage.getSomeUint32();
  }

  public void setSomeUint64(long someUint64) {
    wrappedMessage.setSomeUint64(String.valueOf(someUint64));
  }

  public long getSomeUint64() {
    return Long.parseUnsignedLong(wrappedMessage.getSomeUint64());
  }

  public void setSomeSint32(int someSint32) {
    wrappedMessage.setSomeSint32(-someSint32);
  }

  public int getSomeSint32() {
    return -(wrappedMessage.getSomeSint32());
  }

  public void setSomeSint64(long someSint64) {
    wrappedMessage.setSomeSint64(-someSint64);
  }

  public long getSomeSint64() {
    return -(wrappedMessage.getSomeSint64());
  }

  public void setSomeFixed32(int someFixed32) {
    wrappedMessage.setSomeFixed32(String.valueOf(someFixed32));
  }

  public int getSomeFixed32() {
    return Integer.parseUnsignedInt(wrappedMessage.getSomeFixed32());
  }

  public void setSomeFixed64(long someFixed64) {
    wrappedMessage.setSomeFixed64(someFixed64);
  }

  public long getSomeFixed64() {
    return wrappedMessage.getSomeFixed64();
  }

  public void setSomeSfixed32(int someSfixed32) {
    wrappedMessage.setSomeSfixed32(someSfixed32);
  }

  public int getSomeSfixed32() {
    return wrappedMessage.getSomeSfixed32();
  }

  public void setSomeSfixed64(long someSfixed64) {
    wrappedMessage.setSomeSfixed64P1((int)someSfixed64 >> 32);
    wrappedMessage.setSomeSfixed64P2((int)someSfixed64 & 0xFFFFFFFFL);
  }

  public long getSomeSfixed64() {
    return (wrappedMessage.getSomeSfixed64P1() << 32 | (wrappedMessage.getSomeSfixed64P2()) & 0xFFFFFFFFL);
  }

  public void setSomeBoolean(boolean someBoolean) {
    wrappedMessage.setSomeBoolean(String.valueOf(someBoolean));
  }

  public boolean getSomeBoolean() {
    return Boolean.parseBoolean(wrappedMessage.getSomeBoolean());
  }

  public void setSomeString(String someString) {
    try {
      wrappedMessage.setSomeString(com.google.protobuf.ByteString.copyFrom(someString.getBytes("UTF-32")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getSomeString() {
    try {
      return new String(wrappedMessage.getSomeString().toByteArray(), "UTF-32");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void setSomeBytes(ByteString someBytes) {
    wrappedMessage.setSomeBytesP1(someBytes.substring(someBytes.size() / 2));
    wrappedMessage.setSomeBytesP2(someBytes.substring(0, someBytes.size() / 2));
  }

  public ByteString getSomeBytes() {
    return (wrappedMessage.getSomeBytesP2().concat(wrappedMessage.getSomeBytesP1()));
  }

  public void setFieldHashes() {
    try {
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static com.autogenerated.bypolymorph.allfieldsonce parseFrom(InputStream data) throws
      IOException {
    try {
      var decryptedData = AESDecrypt.doFinal(data.readAllBytes());
      data = new java.io.ByteArrayInputStream(decryptedData);
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    var parseMsg = com.autogenerated.bypolymorph.P1.allfieldsonce.parseFrom(data);
    var mergeMsg = com.autogenerated.bypolymorph.P1.allfieldsonce.newBuilder();
    mergeMsg.mergeFrom((com.google.protobuf.Message)parseMsg);
    var newMsg = new allfieldsonce();
    newMsg.wrappedMessage = mergeMsg;
    return newMsg;
  }

  public static com.autogenerated.bypolymorph.allfieldsonce parseFrom(byte[] data) throws
      InvalidProtocolBufferException, IOException {
    try {
      data = AESDecrypt.doFinal(data);
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    var parseMsg = com.autogenerated.bypolymorph.P1.allfieldsonce.parseFrom(data);
    var mergeMsg = com.autogenerated.bypolymorph.P1.allfieldsonce.newBuilder();
    mergeMsg.mergeFrom((com.google.protobuf.Message)parseMsg);
    var newMsg = new allfieldsonce();
    newMsg.wrappedMessage = mergeMsg;
    return newMsg;
  }

  public void writeTo(OutputStream output) throws IOException {
    setFieldHashes();
    var encryptedBytes = this.toByteArray();
    output.write(encryptedBytes);
  }

  public byte[] toByteArray() throws IOException {
    setFieldHashes();
    try {
      var encryptedBytes = AESEncrypt.doFinal(wrappedMessage.build().toByteArray());
      return encryptedBytes;
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    return null;
  }
}
