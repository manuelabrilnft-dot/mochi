package com.example.mochiapi.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        String path = containerRequestContext.getUriInfo().getPath();

        if (path.equals("auth/login") ||
                path.equals("auth/register")) {
            return;
        }

        Cookie cookie = containerRequestContext.getCookies().get("session_token");

        if (cookie == null) {
            containerRequestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build()
            );
            return;
        }

        String token = cookie.getValue();
        TokenInfo info;
        if(TokenManager.isTokenValid(token)) {
            info = TokenManager.getInfo(token);
            int userId = info.getUser_id();
            containerRequestContext.setProperty("userId", userId);
        }else{
            TokenManager.deleteToken(token);
            containerRequestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build()
            );
        }
    }
}
