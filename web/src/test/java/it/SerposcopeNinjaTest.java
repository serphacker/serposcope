/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package it;

import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.User;
import java.util.HashMap;
import java.util.Map;
import ninja.NinjaTest;


public abstract class SerposcopeNinjaTest extends NinjaTest {
    
    
    protected User createAdmin() throws Exception {
        BaseDB baseDB = getInjector().getInstance(BaseDB.class);
        User user = new User();
        user.setAdmin(true);
        user.setEmail("email@email.com");
        String password = "password";
        user.setPassword(password);
        baseDB.user.insert(user); 
        return user;
    }
    
    protected boolean login(String email, String pass){
        Map<String, String> form = new HashMap<>();
        form.put("email", email);
        form.put("password", pass);
        return ninjaTestBrowser.makePostRequestWithFormParameters(getServerAddress() + "/login", null, form)
            .contains("<title>Home</title>");
    }

}
