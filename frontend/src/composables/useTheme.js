import { ref } from "vue";

const STORAGE_KEY = "theme";
const isDark = ref(false);

function apply(dark) {
  isDark.value = dark;
  document.documentElement.classList.toggle("dark", dark);
}

export function initTheme() {
  const stored = localStorage.getItem(STORAGE_KEY);
  const prefersDark =
    window.matchMedia?.("(prefers-color-scheme: dark)").matches ?? false;
  apply(stored ? stored === "dark" : prefersDark);
}

export function toggleTheme() {
  apply(!isDark.value);
  localStorage.setItem(STORAGE_KEY, isDark.value ? "dark" : "light");
}

export function useTheme() {
  return { isDark, toggleTheme };
}
