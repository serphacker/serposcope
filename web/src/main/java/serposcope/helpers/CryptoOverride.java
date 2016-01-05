/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.helpers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Field;
import ninja.utils.Crypto;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;

@Singleton
public class CryptoOverride extends Crypto {
    
    Field fieldApplicationSecret;

    @Inject
    public CryptoOverride(NinjaProperties ninjaProperties) throws NoSuchFieldException {
        super(ninjaProperties);
        fieldApplicationSecret = Crypto.class.getDeclaredField("applicationSecret");
        fieldApplicationSecret.setAccessible(true);
    }
    
    public void update(NinjaProperties ninjaProperties) throws IllegalArgumentException, IllegalAccessException  {
        fieldApplicationSecret.set(this, ninjaProperties.getOrDie(NinjaConstant.applicationSecret));
    }
    
    public String getApplicationSecret() throws IllegalArgumentException, IllegalAccessException {
        return (String) fieldApplicationSecret.get(this);
    }

}
