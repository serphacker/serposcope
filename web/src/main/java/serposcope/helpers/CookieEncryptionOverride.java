/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.helpers;

import com.google.common.base.Optional;
import java.lang.reflect.Field;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;
import ninja.utils.CookieEncryption;
import static ninja.utils.CookieEncryption.ALGORITHM;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CookieEncryptionOverride extends CookieEncryption{
    
    private static final Logger LOG = LoggerFactory.getLogger(CookieEncryptionOverride.class);
    
    Field fieldEncryptionEnabled;
    Field fieldSecretKeySpec;

    @Inject
    public CookieEncryptionOverride(NinjaProperties properties) throws NoSuchFieldException {
        super(properties);
        fieldEncryptionEnabled = CookieEncryption.class.getDeclaredField("encryptionEnabled");
        fieldEncryptionEnabled.setAccessible(true);
        fieldSecretKeySpec = CookieEncryption.class.getDeclaredField("secretKeySpec");
        fieldSecretKeySpec.setAccessible(true);
    }
    
    public void update(NinjaProperties properties) throws IllegalArgumentException, IllegalAccessException{
        Optional<SecretKeySpec> secretKeySpec = Optional.absent();

        if (properties.getBooleanWithDefault(NinjaConstant.applicationCookieEncrypted, false)) {
            
            setEncryptionEnabled(true);

            String secret = properties.getOrDie(NinjaConstant.applicationSecret);
            try {
                int maxKeyLengthBits = Cipher.getMaxAllowedKeyLength(ALGORITHM);
                if (maxKeyLengthBits == Integer.MAX_VALUE) {
                    maxKeyLengthBits = 256;
                }

                secretKeySpec = Optional.of(
                        new SecretKeySpec(secret.getBytes(), 0, maxKeyLengthBits / Byte.SIZE, ALGORITHM));
                
                LOG.info("Ninja session encryption is using {} / {} bit.", secretKeySpec.get().getAlgorithm(), maxKeyLengthBits);

            } catch (Exception exception) {
                LOG.error("Can not create class to encrypt cookie.", exception);
                throw new RuntimeException(exception);
            }

        } else {
            setEncryptionEnabled(false);
            secretKeySpec = Optional.absent();
        }        
        
        setSecretKeySpec(secretKeySpec);
    }
    
    
    public void setEncryptionEnabled(boolean encryptionEnabled) throws IllegalArgumentException, IllegalAccessException{
        fieldEncryptionEnabled.set(this, encryptionEnabled);
    }
    
    public void setSecretKeySpec(Optional<SecretKeySpec> secretKeySpec) throws IllegalArgumentException, IllegalAccessException{
        fieldSecretKeySpec.set(this, secretKeySpec);
    }
    

}
