import { computed, ref, defineAsyncComponent } from "vue";
import { isAuthenticated } from "@/composables/useAuth";

const routes = [
  {
    path: "/start",
    component: defineAsyncComponent(() => import("./views/LoginView.vue")),
    guestOnly: true,
  },
  {
    path: "/profile",
    component: defineAsyncComponent(() => import("./views/MyMochiView.vue")),
    requiresAuth: true,
  },
  {
    path: "/calendar",
    component: defineAsyncComponent(() => import("./views/CalendarView.vue")),
    requiresAuth: true,
  },
  {
    path: "/friends",
    component: defineAsyncComponent(() => import("./views/FriendsView.vue")),
    requiresAuth: true,
  },
  {
    path: "/challenges",
    component: defineAsyncComponent(() => import("./views/ChallengesView.vue")),
    requiresAuth: true,
  },
  {
    path: "/ideal-day",
    component: defineAsyncComponent(() => import("./views/IdealDayView.vue")),
    requiresAuth: true,
  },
  {
    path: "/achievements",
    component: defineAsyncComponent(
      () => import("./views/AchievementsView.vue"),
    ),
    requiresAuth: true,
  },
  {
    path: "/home",
    component: defineAsyncComponent(() => import("./views/HomeView.vue")),
    requiresAuth: true,
  },
  {
    path: "/goals",
    component: defineAsyncComponent(() => import("./views/GoalsView.vue")),
    requiresAuth: true,
  },
  {
    path: "/profile-page",
    component: defineAsyncComponent(() => import("./views/ProfileView.vue")),
    requiresAuth: true,
  },
];

export const BASE_PATH = (() => {
  const full = window.location.pathname;
  const match = routes.find((r) => full.endsWith(r.path));
  if (match) return full.slice(0, full.length - match.path.length);
  return full.endsWith("/") ? full.slice(0, -1) : "";
})();

function getAppPath() {
  const full = window.location.pathname;
  const path = full.startsWith(BASE_PATH)
    ? full.slice(BASE_PATH.length) || "/"
    : full;
  return path === "/" ? "/start" : path;
}

const currentPath = ref(getAppPath());

window.addEventListener("popstate", () => {
  const path = getAppPath();
  const route = routes.find((r) => r.path === path);

  if (route?.requiresAuth && !isAuthenticated.value) {
    window.history.pushState({}, "", BASE_PATH + "/start");
    currentPath.value = "/start";
    return;
  }

  if (route?.guestOnly && isAuthenticated.value) {
    window.history.pushState({}, "", BASE_PATH + "/home");
    currentPath.value = "/home";
    return;
  }

  currentPath.value = path;
});

// Used to send user to a new page and updates the currentPath ref
export function navigate(path) {
  const route = routes.find((r) => r.path === path);
  if (route?.requiresAuth && !isAuthenticated.value) {
    window.history.pushState({}, "", BASE_PATH + "/start");
    currentPath.value = "/start";
    return;
  }

  if (route?.guestOnly && isAuthenticated.value) {
    window.history.pushState({}, "", BASE_PATH + "/home");
    currentPath.value = "/home";
    return;
  }

  window.history.pushState({}, "", BASE_PATH + path);
  currentPath.value = path;
}

export function applyAuthGuard() {
  const route = routes.find((r) => r.path === currentPath.value);
  if (route?.requiresAuth && !isAuthenticated.value) {
    window.history.replaceState({}, "", BASE_PATH + "/start");
    currentPath.value = "/start";
  } else if (route?.guestOnly && isAuthenticated.value) {
    window.history.replaceState({}, "", BASE_PATH + "/home");
    currentPath.value = "/home";
  }
}

export function useRouter() {
  const currentRoute = computed(
    () => routes.find((r) => r.path === currentPath.value) ?? null,
  );
  return { currentPath, currentRoute, navigate };
}
