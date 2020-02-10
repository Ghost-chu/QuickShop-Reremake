package org.maxgamer.quickshop.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.jetbrains.annotations.NotNull;

public class IOUtils {
  /**
   * Read the file to the String
   *
   * @param file Target file.
   * @return Target file's content.
   */
  public static String readToString(final @NotNull File file) {
    final long filelength = file.length();
    final byte[] filecontent = new byte[(int) filelength];
    try {
      FileInputStream in = new FileInputStream(file);
      in.read(filecontent);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new String(filecontent, StandardCharsets.UTF_8);
  }
  /**
   * Calc the string md5
   *
   * @param s string
   * @return md5
   */
  @NotNull
  public static String md5(final String s) {
    try {
      final MessageDigest instance = MessageDigest.getInstance("MD5");
      instance.update(s.getBytes(StandardCharsets.UTF_8));
      final byte[] digest = instance.digest();
      final StringBuilder sb = new StringBuilder();
      for (int b : digest) {
        int n = b;
        if (n < 0) {
          n += 256;
        }
        if (n < 16) {
          sb.append("0");
        }
        sb.append(Integer.toHexString(n));
      }
      return sb.toString().toLowerCase();
    } catch (Exception ex) {
      return "";
    }
  }
}
