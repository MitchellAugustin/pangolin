package com.mitchellaugustin.pangolin.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

public class PasswordUtils {
	private static final int iterations = 20*2000;
	private static final int saltLength = 32;
	private static final int desiredKeyLength = 512;
	
	public static String getSaltedHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
		byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength);
		
		return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
	}
	
	public static boolean confirmPasswordAuthenticity(String password, String savedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException{
		String[] saltedPassword = savedPassword.split("\\$");
		Log.info(savedPassword);
		if(saltedPassword.length != 2){
			throw new IllegalStateException("The password is not stored in the correct form. The accepted form is \"salt$hash\".");
		}
		String inputHash = hash(password, Base64.decodeBase64(saltedPassword[0]));
		return inputHash.equals(saltedPassword[1]);
	}
	
	private static String hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{
		if(password == null || password.length() == 0){
			throw new IllegalArgumentException("Please enter a password.");
		}
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLength));
		return Base64.encodeBase64String(key.getEncoded());
	}
}
