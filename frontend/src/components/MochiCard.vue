<script setup>
// Reusable "My Mochi" card — renders the shared mochi look (from useMochi) plus
// its real mood (from the profile's mood_coefficient) and level-based evolution
// progress. Used on the Home and Profile pages, and ready to drop in elsewhere.
import { computed, onMounted } from "vue";
import { navigate } from "@/router";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import MochiAvatar from "@/models/MochiAvatar.vue";
import MochiScene from "@/models/MochiScene.vue";
import {
  useMochi,
  mochi,
  mochiFace,
  currentStage,
  evolutionStageIndex,
  evolutionPercent,
  nextStage,
  levelsToNextStage,
  moodFromCoefficient,
} from "@/composables/useMochi";
import { user } from "@/composables/useUser";

defineProps({
  // diameter of the mochi sprite inside the avatar circle
  avatarSize: { type: Number, default: 84 },
});

const { load: loadMochi } = useMochi();
// force a fresh fetch each time the page mounts so the mood is always current
onMounted(() => loadMochi({ force: true }));

// mood derived from the real mood_coefficient on the profile (shared user state)
const mood = computed(() =>
  moodFromCoefficient(user.value?.mochi?.moodCoefficient),
);
</script>

<template>
  <Card class="p-5 items-center gap-3">
    <p
      class="text-[11px] font-medium uppercase tracking-wider text-muted-foreground self-start"
    >
      My Mochi
    </p>

    <span
      class="text-[10px] px-2.5 py-1 rounded-full border border-border text-muted-foreground bg-muted"
    >
      Stage {{ evolutionStageIndex + 1 }} · {{ currentStage?.label }}
    </span>

    <div
      class="relative w-24 h-24 rounded-full bg-pink-50 flex items-center justify-center overflow-hidden"
    >
      <template v-if="mochi">
        <MochiScene
          v-if="mochi.scene && mochi.scene !== 'none'"
          :scene="mochi.scene"
          class="absolute inset-0 w-full h-full"
        />
        <MochiAvatar
          :color="mochi.color"
          :face="mochiFace"
          :costume="mochi.costume"
          scene="none"
          :size="avatarSize"
          class="relative z-10"
        />
      </template>
    </div>

    <div class="text-center">
      <p class="text-sm font-semibold text-foreground">
        {{ mochi?.name || "Mochi" }}
      </p>
      <p class="text-xs text-muted-foreground">{{ mood }}</p>
    </div>

    <!-- Evolution progress (level-based, real) -->
    <div class="w-full">
      <div
        class="flex justify-between text-[11px] text-muted-foreground mb-1.5"
      >
        <span>Next evolution</span>
        <span v-if="nextStage"
          >{{ levelsToNextStage }} lvl to {{ nextStage.label }}</span
        >
        <span v-else>Maxed out</span>
      </div>
      <div class="w-full h-1.5 rounded-full bg-muted overflow-hidden">
        <div
          class="h-full rounded-full bg-primary transition-all"
          :style="{ width: evolutionPercent + '%' }"
        ></div>
      </div>
    </div>

    <Button size="sm" class="w-full mt-1" @click="navigate('/profile')">
      Customize
    </Button>
  </Card>
</template>
