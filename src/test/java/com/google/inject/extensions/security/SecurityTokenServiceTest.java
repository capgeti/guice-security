package com.google.inject.extensions.security;

import org.junit.Before;
import org.junit.Test;

import static com.google.inject.extensions.security.SecurityTokenServiceImpl.createSalt;
import static org.junit.Assert.*;

public class SecurityTokenServiceTest {

    private SecurityTokenServiceImpl service = new SecurityTokenServiceImpl(100, 500, createSalt(), 4) {
        @Override protected long now() {
            return timer;
        }
    };
    private long timer = 0;

    @Before
    public void cleanUp() {
        timer = 0;
        service.getUserCache().clear();
        service.getTokenCache().clear();
    }

    @Test
    public void testCreateTokenUsingCache() {

        String token = service.createToken("1234");
        timer = 99;

        String cachedToken = service.createToken("1234");
        assertEquals(token, cachedToken);

        assertEquals(1, service.getUserCache().size());
        assertEquals(token, service.getUserCache().get("1234").token);

        assertEquals(1, service.getTokenCache().size());
        assertEquals("1234", service.getTokenCache().get(token).user);
    }

    @Test
    public void testCreateNewToken() {

        String token = service.createToken("1234");
        timer = 100;

        String newToken = service.createToken("1234");
        assertNotEquals(token, newToken);

        assertEquals(1, service.getUserCache().size());
        assertEquals(newToken, service.getUserCache().get("1234").token);

        assertEquals(1, service.getTokenCache().size());
        assertEquals("1234", service.getTokenCache().get(newToken).user);
    }

    @Test
    public void testCacheRemoval() {
        String token1 = service.createToken("1234");
        timer = 501;
        String otherToken = service.createToken("other");


        assertEquals(1, service.getUserCache().size());
        assertEquals(otherToken, service.getUserCache().get("other").token);

        assertEquals(1, service.getTokenCache().size());
        assertEquals("other", service.getTokenCache().get(otherToken).user);
    }

    @Test
    public void testCacheOnValidation() {
        String token = service.createToken("1234");
        cleanUp();

        timer = 150;

        service.validateToken(token);
        assertEquals(1, service.getUserCache().size());
        assertEquals(token, service.getUserCache().get("1234").token);

        assertEquals(1, service.getTokenCache().size());
        assertEquals("1234", service.getTokenCache().get(token).user);
    }

    @Test
    public void testKeepNewerToken() {
        String token = service.createToken("1234");
        cleanUp();

        timer = 150;
        String newerToken = service.createToken("1234");
        cleanUp();

        assertNotEquals(token, newerToken);

        service.validateToken(token);
        service.validateToken(newerToken);

        assertEquals(1, service.getUserCache().size());
        assertEquals(newerToken, service.getUserCache().get("1234").token);

        assertEquals(1, service.getTokenCache().size());
        assertEquals("1234", service.getTokenCache().get(newerToken).user);

        cleanUp();
        service.validateToken(newerToken);
        service.validateToken(token);

        assertEquals(1, service.getUserCache().size());
        assertEquals(newerToken, service.getUserCache().get("1234").token);

        assertEquals(1, service.getTokenCache().size());
        assertEquals("1234", service.getTokenCache().get(newerToken).user);


    }

    @Test
    public void testInvalidToken() {
        String token = service.createToken("1234");
        cleanUp();

        timer = 501;
        try {
            service.validateToken(token);
            fail();
        } catch (TokenExpiredException expired) {
        }

        assertEquals(0, service.getUserCache().size());
    }

}
