<script setup>
import { Flame, Trophy, Users, Zap, Star, LogOut } from "lucide-vue-next";
import { onMounted, ref, computed } from "vue";
import MochiAvatar from "@/models/MochiAvatar.vue";
import MochiScene from "@/models/MochiScene.vue";
import MochiCard from "@/components/MochiCard.vue";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { user, loadUser } from "@/composables/useUser";
import { logout } from "@/composables/useAuth";
import useFriends from "@/composables/useFriends";
import useAchievements from "@/composables/useAchievements";
import {
  useMochi,
  mochi,
  mochiFace,
  currentStage,
  moodFromCoefficient,
} from "@/composables/useMochi";

const { load: loadMochi } = useMochi();
const { retrieveFriends } = useFriends();
const { retrieve: retrieveAchievements } = useAchievements();

const friends = ref([]);
const achievements = ref([]);

onMounted(async () => {
  loadMochi();
  loadUser();
  friends.value = await retrieveFriends();
  achievements.value = await retrieveAchievements();
});

const today = new Date().toLocaleDateString("en-US", {
  weekday: "long",
  month: "long",
  day: "numeric",
});

// ── Derived profile values ────────────────────────────────────────────────
const username = computed(() => user.value?.username ?? "Mochi user");
const level = computed(() => user.value?.level ?? 1);
const streak = computed(() => user.value?.streak ?? 0);
const xp = computed(() => user.value?.xp ?? 0);
const friendCount = computed(() => friends.value.length);

// ── Achievements (count of unlocked badges shown in the header) ────────────
function isUnlocked(a) {
  if (a.progress != null && a.threshold != null)
    return a.progress >= a.threshold;
  return !!a.completed;
}
const badgeCount = computed(() => achievements.value.filter(isUnlocked).length);

const mood = computed(() =>
  moodFromCoefficient(user.value?.mochi?.moodCoefficient),
);
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">
    <header class="flex items-start justify-between">
      <div>
        <p class="text-xs text-muted-foreground tracking-wide">{{ today }}</p>
        <h1 class="text-xl font-bold text-foreground leading-tight">Profile</h1>
      </div>
      <Button variant="outline" size="sm" class="gap-1.5" @click="logout">
        <LogOut class="size-4" />
        Log out
      </Button>
    </header>

    <div class="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-6 items-stretch">
      <div class="flex flex-col gap-6">
        <!-- Profile hero card -->
        <Card class="p-6 gap-0">
          <div class="flex items-center gap-5">
            <div
              class="relative w-20 h-20 rounded-full border-2 border-primary bg-pink-50 flex items-center justify-center overflow-hidden shrink-0"
            >
              <MochiScene
                v-if="mochi?.scene && mochi.scene !== 'none'"
                :scene="mochi.scene"
                class="absolute inset-0 w-full h-full"
              />
              <MochiAvatar
                :color="mochi?.color"
                :face="mochiFace"
                :costume="mochi?.costume"
                scene="none"
                :size="72"
                class="relative z-10"
              />
            </div>

            <div class="flex flex-col gap-1.5 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-lg font-semibold text-foreground truncate">{{
                  username
                }}</span>
                <span
                  class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-primary text-white"
                  >Lvl {{ level }}</span
                >
              </div>

              <p class="text-xs text-muted-foreground">
                {{ currentStage?.label }} stage · {{ mood }}
              </p>

              <div class="flex items-center gap-2 mt-1 flex-wrap">
                <span
                  class="flex items-center gap-1 text-[11px] px-2.5 py-1 rounded-full border border-orange-200 bg-orange-50 text-orange-600"
                >
                  <Flame class="w-3 h-3" /> {{ streak }} day streak
                </span>
                <span
                  class="flex items-center gap-1 text-[11px] px-2.5 py-1 rounded-full border border-emerald-200 bg-emerald-50 text-emerald-700"
                >
                  <Trophy class="w-3 h-3" /> {{ badgeCount }} badges
                </span>
                <span
                  class="flex items-center gap-1 text-[11px] px-2.5 py-1 rounded-full border border-border bg-card text-muted-foreground"
                >
                  <Users class="w-3 h-3" /> {{ friendCount }} friends
                </span>
              </div>
            </div>
          </div>
        </Card>

        <!-- Stat tiles -->
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <Card class="p-4 gap-1 items-start">
            <Flame class="w-4 h-4 text-orange-500" />
            <span class="text-xl font-semibold text-foreground">{{
              streak
            }}</span>
            <span class="text-[11px] text-muted-foreground">Day streak</span>
          </Card>
          <Card class="p-4 gap-1 items-start">
            <Zap class="w-4 h-4 text-primary" />
            <span class="text-xl font-semibold text-foreground">{{
              level
            }}</span>
            <span class="text-[11px] text-muted-foreground">Level</span>
          </Card>
          <Card class="p-4 gap-1 items-start">
            <Star class="w-4 h-4 text-amber-500" />
            <span class="text-xl font-semibold text-foreground">{{
              xp.toLocaleString()
            }}</span>
            <span class="text-[11px] text-muted-foreground">Total XP</span>
          </Card>
          <Card class="p-4 gap-1 items-start">
            <Users class="w-4 h-4 text-emerald-600" />
            <span class="text-xl font-semibold text-foreground">{{
              friendCount
            }}</span>
            <span class="text-[11px] text-muted-foreground">Friends</span>
          </Card>
        </div>
      </div>

      <!-- Mochi card -->
      <MochiCard />
    </div>
  </div>
</template>
