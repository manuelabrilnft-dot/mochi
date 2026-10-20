import { navigate, BASE_PATH } from "@/router";
import { isAuthenticated } from "@/composables/useAuth";

// We wrap fetch() to avoid repeating logic such as handling unauthorized calls and set some headers without repetition

const defaultHeaders = {
  "Content-Type": "application/json",
};

// Prefix the app's runtime context path so requests resolve correctly both in
// dev (BASE_PATH = '', the Vite proxy handles /api) and in the deployed build
// (BASE_PATH = '/mochi', so /api/... becomes /mochi/api/...).
function buildUrl(url) {
  const path = url.startsWith("/") ? url : `/${url}`;
  return `${BASE_PATH}${path}`;
}

export async function http(url, options = {}) {
  const response = await fetch(buildUrl(url), {
    credentials: "include",
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  });

  if (response.status === 401) {
    isAuthenticated.value = false;
    navigate("/start");
    return;
  }

  return response;
}
