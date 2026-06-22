package br.unesp.fct.evcomp.util;

import org.apache.commons.codec.binary.Base32;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TOTPUtil {

    private static final int TIME_STEP_SECONDS = 15;
    private static final int CODE_DIGITS = 6;

    public static String generateTOTP(String secret, long timeMillis) {
        try {
            Base32 base32 = new Base32();
            byte[] key = base32.decode(secret);
            
            // Calculando o "time window" (steps)
            long timeStep = (timeMillis / 1000) / TIME_STEP_SECONDS;
            
            // Convertendo step para array de bytes
            byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
            
            // HMAC-SHA1
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            
            // Truncamento dinâmico (RFC 4226)
            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= Math.pow(10, CODE_DIGITS);
            
            return String.format("%0" + CODE_DIGITS + "d", truncatedHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Erro ao gerar TOTP", e);
        }
    }

    public static boolean validateTOTP(String secret, String codeToValidate, long timeMillisLeitura) {
        String generated = generateTOTP(secret, timeMillisLeitura);

        return generated.equals(codeToValidate);
    }
}
