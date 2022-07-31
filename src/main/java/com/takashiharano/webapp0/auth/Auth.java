/**
 * MIT License
 *
 * Copyright (c) 2020 Takashi Harano
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.takashiharano.webapp0.auth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

/**
 * TinyAuth
 *
 * <pre>
 * Registration:
 *   register(user, hash(user + pass))
 *     hash = stretch(hash, n)
 *     user,hash will be stored into the file.
 *
 * Authentication:
 *   auth(user, hash(pass + user))
 *     stretch(hash) == registered hash ?
 *
 * Data File:
 * username1[TAB]hash1[LF]
 * username2[TAB]hash2[LF]
 * ...
 * </pre>
 *
 * Available hash algorithms: MD5, SHA-1, SHA-256 (default), SHA-512
 */
public class Auth {

  private static final String LINE_SEPARATOR = "\n";
  private static final String DELIMITER = "\t";
  private static final Charset ENCODING = StandardCharsets.UTF_8;

  private String passFilePath;
  private String hashAlgorithm = "SHA-256";
  private int stretching;

  /**
   * Initializes the module with the file path.
   *
   * @param filePath
   *          the path of the user password file
   */
  public Auth(String filePath) {
    this.passFilePath = filePath;
  }

  /**
   * Initializes the module with the file path and hash algorithm.
   *
   * @param filePath
   *          the path of the user password file
   * @param algorithm
   *          hash algorithm
   */
  public Auth(String filePath, String algorithm) {
    this.passFilePath = filePath;
    this.hashAlgorithm = algorithm;
  }

  /**
   * Initializes the module with the file path and the number of times to stretch.
   *
   * @param filePath
   *          the path of the user password file
   * @param stretching
   *          number of times to stretch
   */
  public Auth(String filePath, int stretching) {
    this.passFilePath = filePath;
    this.stretching = stretching;
  }

  /**
   * Initialize the module with the file path, hash algorithm, and the number of
   * times to stretch.
   *
   * @param filePath
   *          the path of the user password file
   * @param algorithm
   *          hash algorithm
   * @param stretching
   *          number of times to stretch
   */
  public Auth(String filePath, String algorithm, int stretching) {
    this.passFilePath = filePath;
    this.hashAlgorithm = algorithm;
    this.stretching = stretching;
  }

  /**
   * Authentication.
   *
   * @param user
   *          user id
   * @param hash
   *          the hash string. hash(pass + user), non-stretched.<br>
   *          it must be lower case.
   * @return Result status. The caller of this method should not distinguish the
   *         error status except for debugging.
   */
  public String auth(String user, String hash) {
    String[] records;
    try {
      records = loadPasswordFile();
    } catch (IOException e) {
      return "PASSWORD_FILE_LOAD_ERROR";
    }

    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      String[] fields = record.split(DELIMITER);
      if (fields.length < 2) {
        continue;
      }
      String uid = fields[0];
      if (uid.equals(user)) {
        String userHash = fields[1];
        if (checkHash(hash, userHash, stretching)) {
          return "OK";
        } else {
          return "PASSWORD_MISMATCH";
        }
      }
    }

    return "PW_DATA_NOT_FOUND";
  }

  /**
   * Authenticate with a plain text password.
   *
   * @param user
   *          user id
   * @param pass
   *          password
   * @return Result status. The caller of this method would be better to make no
   *         distinction the error status except for debugging.
   */
  public String authByPlainPass(String user, String pass) {
    String hash = getHashString(pass, user);
    return auth(user, hash);
  }

  /**
   * Returns a hash value for the input.
   *
   * @param input
   *          input string
   * @return hash value
   */
  public String getHashString(String input) {
    byte[] b = input.getBytes(ENCODING);
    String hashString = null;
    try {
      byte[] hash = getHashBytes(b, hashAlgorithm);
      hashString = DatatypeConverter.printHexBinary(hash);
      hashString = hashString.toLowerCase();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return hashString;
  }

  /***
   * Returns a hash value for the input with salt.
   *
   * @param input
   *          input string
   * @param salt
   *          salt value
   * @return hash value
   */
  public String getHashString(String input, String salt) {
    return getHashString(input + salt);
  }

  /**
   * Returns a hash string for storage.
   *
   * @param user
   *          user id
   * @param pass
   *          the plain password
   * @return a hash string. it may be stretched depending on the stretching value.
   */
  public String getHashStringForStorage(String user, String pass) {
    return getHashStringForStorage(user, pass, user);
  }

  /**
   * Returns a hash string for storage.
   *
   * @param user
   *          user id
   * @param pass
   *          the plain password
   * @param salt
   *          the salt
   * @return a hash string. it may be stretched depending on the stretching value.
   */
  public String getHashStringForStorage(String user, String pass, String salt) {
    String hash = getHashString(pass, salt);
    hash = stretch(hash, stretching);
    return hash;
  }

  /**
   * Returns the password file path.
   *
   * @return the password file path
   */
  public String getPassFilePath() {
    return passFilePath;
  }

  /**
   * Sets the password file path.
   *
   * @param passFilePath
   *          the password file path
   */
  public void setPassFilePath(String passFilePath) {
    this.passFilePath = passFilePath;
  }

  /**
   * Returns the hash algorithm.
   *
   * @return the hash algorithm
   */
  public String getHashAlgorithm() {
    return hashAlgorithm;
  }

  /**
   * Sets the hash algorithm.
   *
   * @param hashAlgorithm
   *          the hash algorithm
   */
  public void setHashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  /**
   * Returns the number of stretching.
   *
   * @return the number of stretching
   */
  public int getStretching() {
    return stretching;
  }

  /**
   * Sets the number of stretching.
   *
   * @param stretching
   *          the number of stretching
   */
  public void setStretching(int stretching) {
    this.stretching = stretching;
  }

  /**
   * Register a password with plain text.
   *
   * @param user
   *          target user id
   * @param pass
   *          plain text password
   * @return 0=added / 1=updated / -1=error
   */
  public int registerByPlainPass(String user, String pass) {
    return registerByPlainPass(user, pass, user);
  }

  /**
   * Register a password with plain text.
   *
   * @param user
   *          target user id
   * @param pass
   *          plain text password
   * @param salt
   *          salt for hash. Set "" not to use.
   * @return 0=added / 1=updated / -1=error
   */
  public int registerByPlainPass(String user, String pass, String salt) {
    String hash = getHashStringForStorage(user, pass, salt);
    return register(user, hash);
  }

  /**
   * Register a password with a hash.<br>
   * The given hash will be stretched before save to the file.
   *
   * @param user
   *          target user id
   * @param hash
   *          the hash
   * @return 0=added / 1=updated / -1=error
   */
  public int registerByHash(String user, String hash) {
    hash = stretch(hash, stretching);
    return register(user, hash);
  }

  /**
   * Remove a user record.
   *
   * @param user
   *          user id
   * @return true if the target is successfully deleted; false otherwise
   */
  public boolean remove(String user) {
    String[] records;
    try {
      records = loadPasswordFile();
    } catch (IOException e) {
      return false;
    }

    StringBuilder sb = new StringBuilder();
    boolean deleted = false;
    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      String[] fields = record.split(DELIMITER);
      String uid = fields[0];
      if (uid.equals(user)) {
        deleted = true;
      } else {
        sb.append(record + LINE_SEPARATOR);
      }
    }

    String newRecords = sb.toString();
    try {
      savePasswordFile(newRecords);
    } catch (IOException ioe) {
      deleted = false;
      ioe.printStackTrace();
    }

    return deleted;
  }

  /**
   * Register a password.
   *
   * @param user
   *          target user id
   * @param hash
   *          non-stretched hash. hash(pass + user as salt)
   * @return 0=added / 1=updated / -1=error
   */
  private int register(String user, String hash) {
    if ((user == null) || (hash == null)) {
      return -1;
    }
    String newRecord = user + DELIMITER + hash;
    String[] records;
    try {
      records = loadPasswordFile();
    } catch (IOException e) {
      records = new String[0];
    }

    StringBuilder sb = new StringBuilder();
    int result = 0;
    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      String[] fields = record.split(DELIMITER);
      String uid = fields[0];
      if (uid.equals(user)) {
        // update
        result = 1;
        sb.append(newRecord + LINE_SEPARATOR);
      } else {
        sb.append(record + LINE_SEPARATOR);
      }
    }

    if (result == 0) {
      // new
      sb.append(newRecord + LINE_SEPARATOR);
    }

    String newRecords = sb.toString();

    try {
      savePasswordFile(newRecords);
    } catch (IOException ioe) {
      result = -1;
      ioe.printStackTrace();
    }

    return result;
  }

  /**
   * Returns whether the hash values match.
   *
   * @param inputHash
   *          input hash
   * @param userHash
   *          user hash
   * @param stretchingN
   *          number of stretching
   * @return true if the hash values match; false otherwise
   */
  private boolean checkHash(String inputHash, String userHash, int stretchingN) {
    String stretchedHash = stretch(inputHash, stretchingN);
    userHash = userHash.toLowerCase();
    if (stretchedHash.equals(userHash)) {
      return true;
    }
    return false;
  }

  /**
   * Returns hash value.
   *
   * @param input
   *          input byte array
   * @param algorithm
   *          hash algorithm (MD5 / SHA-1 / SHA-256 / SHA-512)
   * @return hash value
   * @throws NoSuchAlgorithmException
   *           if no Provider supports aMessageDigestSpi implementation for the
   *           specified algorithm.
   */
  private byte[] getHashBytes(byte[] input, String algorithm) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(algorithm);
    byte[] hash = md.digest(input);
    return hash;
  }

  /**
   * Stretches the hash.
   *
   * @param src
   *          the source value
   * @param n
   *          number of times to stretch
   * @return stretched value
   */
  private String stretch(String src, int n) {
    String hash = src;
    for (int i = 0; i < n; i++) {
      hash = getHashString(hash);
    }
    return hash;
  }

  /**
   * Load user password file.
   *
   * @return the array of the read lines
   * @throws IOException
   *           If an I/O error occurs
   */
  private String[] loadPasswordFile() throws IOException {
    String[] users = readTextFileAsArray(passFilePath);
    return users;
  }

  /**
   * Save user password file.
   *
   * @param records
   *          the records to save
   * @throws IOException
   *           if I/O error occurs
   */
  private void savePasswordFile(String records) throws IOException {
    writeFile(passFilePath, records);
  }

  /**
   * Read a text file as an array.
   *
   * @param path
   *          file path
   * @return text content
   */
  private String[] readTextFileAsArray(String path) throws IOException {
    Path file = Paths.get(path);
    List<String> lines = Files.readAllLines(file, ENCODING);
    String[] text = new String[lines.size()];
    lines.toArray(text);
    return text;
  }

  /**
   * Write a text into a file.
   *
   * @param path
   *          file path
   * @param content
   *          text content
   * @throws IOException
   *           If an I/O error occurs
   */
  private void writeFile(String path, String content) throws IOException {
    File file = new File(path);
    try (FileWriter fw = new FileWriter(file); BufferedWriter bw = new BufferedWriter(fw);) {
      bw.write(content);
    } catch (IOException e) {
      throw e;
    }
  }

}
