import { createApp } from "vue";
import "./styles/globals.css";
import App from "./App.vue";
import { meService } from "./services/auth";
import { isAuthenticated } from "./composables/useAuth";
import { loadUser } from "./composables/useUser";
import { applyAuthGuard } from "./router";
import { initTheme } from "./composables/useTheme";

initTheme();

try {
  const res = await meService();
  if (res.ok) {
    isAuthenticated.value = true;
    await loadUser();
  }
} catch {
  isAuthenticated.value = false;
}

applyAuthGuard();

createApp(App).mount("#app");
