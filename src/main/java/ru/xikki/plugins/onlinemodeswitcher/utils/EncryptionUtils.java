package ru.xikki.plugins.onlinemodeswitcher.utils;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class EncryptionUtils {

	private static final SecureRandom RANDOM = new SecureRandom();

	public static byte[] generateVerifyToken() {
		byte[] token = new byte[4];
		RANDOM.nextBytes(token);
		return token;
	}

	@NotNull
	public static KeyPair generateKeys() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			return generator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public static byte[] decrypt(@NotNull byte[] encryptedData, @NotNull PrivateKey key) {
		try {
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(encryptedData);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
				 BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public static byte[] getServerHashRaw(@NotNull String serverId, @NotNull PublicKey publicKey, @NotNull SecretKey secretKey) {
		Hasher hasher = Hashing.sha1().newHasher();

		hasher.putBytes(serverId.getBytes(StandardCharsets.ISO_8859_1));
		hasher.putBytes(secretKey.getEncoded());
		hasher.putBytes(publicKey.getEncoded());

		return hasher.hash().asBytes();
	}

	@NotNull
	public static String getServerHash(@NotNull String serverId, @NotNull PublicKey publicKey, @NotNull SecretKey secretKey) {
		return (new BigInteger(EncryptionUtils.getServerHashRaw(serverId, publicKey, secretKey))).toString(16);
	}


}
