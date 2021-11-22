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
 * register:<br>
 * register(id, hash(pass + id)) -> id,stretch(hash) into the file.<br>
 * <br>
 * authentication:<br>
 * auth(id, hash(pass + id)) -> stretch(hash) == stored hash ?
 *
 * Available hash algorithms: MD5, SHA-1, SHA-256 (default), SHA-512
 */
public class Auth {

  private static final String LINE_SEPARATOR = System.lineSeparator();
  private static final String DELIMITER = "\t";

  private String passFilePath;
  private String hashAlgorithm = "SHA-256";
  private int stretchingN = 0;

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
   * @param stretchingN
   *          number of times to stretch
   */
  public Auth(String filePath, int stretchingN) {
    this.passFilePath = filePath;
    this.stretchingN = stretchingN;
  }

  /**
   * Initialize the module with the file path, hash algorithm, and the number of
   * times to stretch.
   *
   * @param filePath
   *          the path of the user password file
   * @param algorithm
   *          hash algorithm
   * @param stretchingN
   *          number of times to stretch
   */
  public Auth(String filePath, String algorithm, int stretchingN) {
    this.passFilePath = filePath;
    this.hashAlgorithm = algorithm;
    this.stretchingN = stretchingN;
  }

  /**
   * Authentication.
   *
   * @param id
   *          user id
   * @param hash
   *          hash value. hash(pass + id), non-stretched. the value must be lower
   *          case.
   * @return Result status. The caller of this method would be better to make no
   *         distinction the error status except for debugging.
   */
  public String auth(String id, String hash) {
    String[] records;
    try {
      records = loadPasswordFile();
    } catch (IOException e) {
      return "LOAD_USER_FILE_ERROR";
    }

    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      String[] fields = record.split(DELIMITER);
      if (fields.length < 2) {
        continue;
      }
      String uid = fields[0];
      String userHash = fields[1];
      if (uid.equals(id)) {
        String stretchedHash = stretch(hash, stretchingN);
        userHash = userHash.toLowerCase();
        if (stretchedHash.equals(userHash)) {
          return "OK";
        } else {
          return "NG";
        }
      }
    }

    return "NO_SUCH_USER";
  }

  /**
   * Authenticate with a plain text password.
   *
   * @param id
   *          user id
   * @param pass
   *          password
   * @return Result status. The caller of this method would be better to make no
   *         distinction the error status except for debugging.
   */
  public String authByPlainPass(String id, String pass) {
    String hash = getHash(pass, id);
    return auth(id, hash);
  }

  /**
   * Register a password.<br>
   * The given hash will be stretched before save to the file.
   *
   * @param targetId
   *          target user id
   * @param hash
   *          non-stretched hash. hash(pass + id)
   */
  public void register(String id, String hash) {
    hash = stretch(hash, stretchingN);
    String newRecord = id + DELIMITER + hash;
    String[] records;
    try {
      records = loadPasswordFile();
    } catch (IOException e) {
      records = new String[0];
    }

    StringBuilder sb = new StringBuilder();
    boolean found = false;
    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      String[] fields = record.split(DELIMITER);
      String uid = fields[0];
      if (uid.equals(id)) {
        found = true;
        sb.append(newRecord + LINE_SEPARATOR);
      } else {
        sb.append(record + LINE_SEPARATOR);
      }
    }

    if (!found) {
      sb.append(newRecord + LINE_SEPARATOR);
    }

    String newRecords = sb.toString();
    savePasswordFile(newRecords);
  }

  /**
   * Register a password with plain text.
   *
   * @param targetId
   *          target user id
   * @param pass
   *          plain text password
   */
  public void registerByPlainPass(String id, String pass) {
    registerByPlainPass(id, pass, id);
  }

  /**
   * Register a password with plain text.
   *
   * @param targetId
   *          target user id
   * @param pass
   *          plain text password
   * @param salt
   *          salt for hash. Set "" not to use.
   */
  public void registerByPlainPass(String id, String pass, String salt) {
    String hash = getHash(pass, salt);
    register(id, hash);
  }

  /**
   * Delete a user record.
   *
   * @param id
   *          user id
   * @return true if the target is successfully deleted; false otherwise
   */
  public boolean deleteUser(String id) {
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
      if (uid.equals(id)) {
        deleted = true;
      } else {
        sb.append(record + LINE_SEPARATOR);
      }
    }

    String newRecords = sb.toString();
    savePasswordFile(newRecords);

    return deleted;
  }

  /**
   * Returns a hash value for the input.
   *
   * @param input
   *          input string
   * @return hash value
   */
  public String getHash(String input) {
    byte[] b = input.getBytes(StandardCharsets.UTF_8);
    String hash = getHash(b, hashAlgorithm);
    return hash.toLowerCase();
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
  public String getHash(String input, String salt) {
    return getHash(input + salt);
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
  public String stretch(String src, int n) {
    String hash = src;
    for (int i = 0; i < n; i++) {
      hash = getHash(hash);
    }
    return hash;
  }

  /**
   * Returns hash value.
   *
   * @param input
   *          input byte array
   * @param algorithm
   *          hash algorithm (MD5 / SHA-1 / SHA-256 / SHA-512)
   * @return hash value
   */
  private String getHash(byte[] input, String algorithm) {
    String hash = null;
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      byte[] b = md.digest(input);
      hash = DatatypeConverter.printHexBinary(b);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return hash;
  }

  /**
   * Load user password file.
   *
   * @return the array of the read lines
   */
  private String[] loadPasswordFile() throws IOException {
    String[] users = readTextFileAsArray(passFilePath);
    return users;
  }

  /**
   * Save user password file.
   *
   * @param id
   *          target id
   * @param hash
   *          hash value
   */
  private void savePasswordFile(String records) {
    try {
      writeFile(passFilePath, records);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Read a text file as an array.
   *
   * @param path
   * @return text content
   */
  private String[] readTextFileAsArray(String path) throws IOException {
    Path file = Paths.get(path);
    List<String> lines = Files.readAllLines(file, Charset.forName("UTF-8"));
    String[] text = new String[lines.size()];
    lines.toArray(text);
    return text;
  }

  /**
   * Write a text into a file.
   *
   * @param path
   * @param content
   * @throws IOException
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
