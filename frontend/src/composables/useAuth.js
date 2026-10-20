import { navigate } from "@/router";
import { registerService, loginService, logoutService } from "@/services/auth";
import { loadUser, clearUser } from "@/composables/useUser";
import { ref } from "vue";

export const isAuthenticated = ref(false);

// invalidates the session server-side (which also clears the httpOnly cookie),
// resets local auth state, and returns to /start
export async function logout() {
  try {
    await logoutService();
  } catch (e) {
    console.error(e);
  }
  isAuthenticated.value = false;
  clearUser();
  navigate("/start");
}

export function useAuth() {
  const isLoading = ref(false);
  const error = ref(null);

  async function register(username, password) {
    isLoading.value = true;
    try {
      await registerService(username, password);

      console.log("Redirecting to /home...");
      isAuthenticated.value = true;
      await loadUser({ force: true });
      navigate("/home");
    } catch (e) {
      error.value = e.message;
    } finally {
      isLoading.value = false;
    }
  }

  async function login(username, password) {
    isLoading.value = true;
    try {
      const data = await loginService(username, password);
      isAuthenticated.value = true;
      await loadUser({ force: true });
      navigate("/home");
    } catch (e) {
      error.value = e.message;
    } finally {
      isLoading.value = false;
    }
  }

  // async function meService() {
  //   isLoading.value = true;
  //   try {
  //     const data = await meService();
  //     isAuthenticated = true;
  //   } catch (e) {
  //     error.value = e.message;
  //     isAuthenticated.value = false;
  //     navigate("/start");
  //   } finally {
  //     isLoading.value = false;
  //   }
  // }

  return { isLoading, error, register, login };
}
