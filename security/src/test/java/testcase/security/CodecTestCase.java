package testcase.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CodecTestCase {
	@Test
	public void genHmacSha1(){
		try {
			String key = "6195130a1c24bb5a4aa109a0813bff51";
			String value = "testsoso";
			String ret = HmacUtils.hmacSha1Hex(key, value);
			String ret1 = hmac_sha1(key, value);
			System.out.println("ret-->"+ret);
			System.out.println("ret1-->"+ret1);
		} catch (Exception e) {
			Assert.fail("gen hmac(sha1)",e);
		}
	}
	private static final String HMAC_SHA1 = "HmacSHA1";
	private String hmac_sha1(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
		Key signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(data.getBytes());
		return new String(Base64.encodeBase64(rawHmac));
	}
}
