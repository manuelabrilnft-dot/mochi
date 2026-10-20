<script setup>
import {
  Home,
  Target,
  CalendarDays,
  Sun,
  Moon,
  Flame,
  Users,
  Trophy,
  ShoppingBag,
} from "@lucide/vue";
import { computed } from "vue";
import { useRouter } from "@/router";
import { user } from "@/composables/useUser";
import { useTheme } from "@/composables/useTheme";
import logoBlack from "@/assets/mochi_logo_transparent_black.svg";
import logoWhite from "@/assets/mochi_logo_transparent_white.svg";

const { currentPath, navigate } = useRouter();
const { isDark, toggleTheme } = useTheme();

const logoSrc = computed(() => (isDark.value ? logoWhite : logoBlack));

const displayName = computed(() => user.value?.username ?? "Username");
const level = computed(() => user.value?.level ?? 0);
const initial = computed(() => displayName.value.charAt(0).toUpperCase());

const navItems = [
  { label: "Home", icon: Home, path: "/home" },
  { label: "Goals", icon: Target, path: "/goals" },
  { label: "Calendar", icon: CalendarDays, path: "/calendar" },
  { label: "Ideal Day", icon: Sun, path: "/ideal-day" },
  { label: "Challenges", icon: Flame, path: "/challenges" },
  { label: "Friends", icon: Users, path: "/friends" },
  { label: "Achievements", icon: Trophy, path: "/achievements" },
  { label: "My Mochi", icon: ShoppingBag, path: "/profile" },
];
</script>

<template>
  <aside
    class="w-52 bg-card rounded-xl flex flex-col py-6 px-3 shrink-0 ring-1 ring-foreground shadow-xs"
  >
    <div class="px-3 mb-8">
      <img
        :src="logoSrc"
        alt="Mochi"
        class="h-9 w-auto mr-auto cursor-pointer"
        @click="navigate('/home')"
      />
    </div>

    <nav class="flex flex-col gap-0.5 flex-1">
      <button
        v-for="item in navItems"
        :key="item.path"
        @click="navigate(item.path)"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-foreground hover:bg-accent transition-colors w-full text-left cursor-pointer"
        :class="
          currentPath === item.path
            ? 'bg-accent font-medium'
            : ''
        "
      >
        <component
          :is="item.icon"
          class="size-4 shrink-0 text-muted-foreground"
        />
        {{ item.label }}
      </button>
    </nav>

    <div class="pt-4 mt-2 border-t border-border flex flex-col gap-0.5">
      <!-- Theme toggle -->
      <button
        @click="toggleTheme"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-foreground hover:bg-accent transition-colors w-full text-left cursor-pointer"
      >
        <component
          :is="isDark ? Sun : Moon"
          class="size-4 shrink-0 text-muted-foreground"
        />
        {{ isDark ? "Light mode" : "Dark mode" }}
      </button>

      <!-- User — opens the profile page -->
      <button
        @click="navigate('/profile-page')"
        class="flex items-center gap-3 px-3 py-2 rounded-lg w-full text-left hover:bg-accent transition-colors cursor-pointer"
        :class="currentPath === '/profile-page' ? 'bg-accent' : ''"
      >
        <div
          class="size-8 rounded-full bg-muted flex items-center justify-center text-xs font-semibold text-muted-foreground shrink-0"
        >
          {{ initial }}
        </div>
        <div class="flex flex-col min-w-0">
          <span class="text-sm font-medium text-foreground truncate">{{ displayName }}</span>
          <span class="text-xs text-muted-foreground">Lvl {{ level }}</span>
        </div>
      </button>
    </div>
  </aside>
</template>
