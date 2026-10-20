import { ref } from "vue";
import { getProfile } from "@/services/profile";

export const user = ref(null);

export async function loadUser({ force = false } = {}) {
  if (user.value && !force) return user.value;
  try {
    const res = await getProfile();
    if (!res?.ok) {
      user.value = null;
      return null;
    }
    user.value = await res.json();
    return user.value;
  } catch (e) {
    console.error(e);
    user.value = null;
    return null;
  }
}

export function clearUser() {
  user.value = null;
}

export function useUser() {
  return { user, loadUser, clearUser };
}
