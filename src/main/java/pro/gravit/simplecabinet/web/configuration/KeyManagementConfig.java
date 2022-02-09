package pro.gravit.simplecabinet.web.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.gravit.simplecabinet.web.service.KeyManagementService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
public class KeyManagementConfig {
    @Value("${keymanagement.save}")
    private boolean isSaveKeys;

    @Value("${keymanagement.path}")
    private Path keysLocation;

    @Bean
    public KeyManagementService getKeyManagementService() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
        Path publicKey = keysLocation.resolve("ecdsa.pub");
        Path privateKey = keysLocation.resolve("ecdsa");
        if (isSaveKeys && Files.exists(privateKey) && Files.exists(publicKey)) {
            var factory = KeyFactory.getInstance("EC");
            ECPrivateKey priv = (ECPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(privateKey)));
            ECPublicKey pub = (ECPublicKey) factory.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(publicKey)));
            return new KeyManagementService(pub, priv);
        } else {
            var keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

            var pair = keyGen.generateKeyPair();
            var priv = (ECPrivateKey) pair.getPrivate();
            var pub = (ECPublicKey) pair.getPublic();
            if (isSaveKeys) {
                try (OutputStream output = new FileOutputStream(privateKey.toFile())) {
                    output.write(priv.getEncoded());
                }
                try (OutputStream output = new FileOutputStream(publicKey.toFile())) {
                    output.write(pub.getEncoded());
                }
            }
            return new KeyManagementService(pub, priv);
        }
    }
}
