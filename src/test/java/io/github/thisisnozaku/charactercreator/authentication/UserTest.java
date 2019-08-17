package io.github.thisisnozaku.charactercreator.authentication;

import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Damie on 2/21/2017.
 */
public class UserTest {
    /**
     * Ensure property association between Users and OAuth accounts.
     * @throws Exception
     */
    @Test
    public void getAssociatedAccounts() throws Exception {
        List<OAuthAccountAssociation> associations = new ArrayList<>();
        OAuthAccountAssociation oauthAssociation = new OAuthAccountAssociation("1", "2");
        associations.add(oauthAssociation);
        User user = new User(1L, associations);
        assertEquals(user, oauthAssociation.getUser());
    }
}