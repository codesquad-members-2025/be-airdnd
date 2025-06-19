package rice_monkey.member.util;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Component
public class PasswordEncoderUtil {

    // PBKDF2 파라미터
    private static final int ITERATIONS = 185000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * 솔트 생성 (byte 배열을 Base64로 인코딩해 문자열로 저장)
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 원본 비밀번호와 솔트를 받아 PBKDF2 해시를 생성해 Base64 문자열로 반환
     */
    public static String hashPassword(String rawPassword, String base64Salt) {
        try {
            byte[] salt = Base64.getDecoder().decode(base64Salt);
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 로그인 시 사용: 입력된 원본(raw) 비밀번호와 DB에 저장된 salt/hash 를 비교
     */
    public static boolean verifyPassword(String rawPassword, String base64Salt, String storedHash) {
        String computedHash = hashPassword(rawPassword, base64Salt);
        return computedHash.equals(storedHash);
    }

}
