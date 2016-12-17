package com.codeoregonapp.patrickleonard.ribbit.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Adjusted te code to remove warnings
 * Created by Patrick Leonard on 11/19/2015.
 */
public class MD5Util {
        public static String hex(byte[] array) {
            StringBuilder sb = new StringBuilder();
            for(byte b: array) {
                sb.append(Integer.toHexString((b
                        & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        }
        public static String md5Hex (String message) throws NoSuchAlgorithmException,UnsupportedEncodingException {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        }
}

