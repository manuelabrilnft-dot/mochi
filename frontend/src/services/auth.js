import { http } from "@/services/http";

export async function registerService(username, password) {
  const response = await http("api/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Register Failed");
  }
  return response;
}

export async function loginService(username, password) {
  const response = await http("api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Login Failed");
  }
  return response;
}

export async function meService() {
  const response = await http("api/auth/me", {
    method: "GET",
  });
  return response;
}

// backend path is /auth/auth/logout (method @Path("/auth/logout") inside the
// @Path("/auth") resource). Invalidates the token and clears the cookie.
export async function logoutService() {
  return await http("api/auth/auth/logout", {
    method: "POST",
  });
}
