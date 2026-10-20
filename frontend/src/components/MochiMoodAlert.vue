<script setup>
import { computed, ref, onMounted } from "vue";
import { Button } from "@/components/ui/button";
import { navigate } from "@/router";
import { user } from "@/composables/useUser";
import { useMochi, mochi, mochiFace, moodFromCoefficient } from "@/composables/useMochi";
import MochiAvatar from "@/models/MochiAvatar.vue";

const { load: loadMochi } = useMochi();
onMounted(loadMochi);

const dismissed = ref(false);

const mood = computed(() =>
  moodFromCoefficient(user.value?.mochi?.moodCoefficient),
);

const show = computed(
  () => !dismissed.value && (mood.value === "Tired" || mood.value === "Lazy"),
);

const message = computed(() =>
  mood.value === "Tired"
    ? "Your Mochi is tired. Plan some rest time"
    : "Your Mochi is feeling lazy. Plan some productive activities",
);

function goToIdealDay() {
  dismissed.value = true;
  navigate("/calendar");
}
</script>

<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
    @click.self="dismissed = true"
  >
    <div
      class="w-[340px] rounded-2xl bg-card p-6 shadow-xl flex flex-col items-center gap-3"
    >
      <div
        class="relative w-20 h-20 rounded-full bg-pink-50 flex items-center justify-center overflow-hidden"
      >
        <MochiAvatar
          :color="mochi.color"
          :face="mochiFace"
          :costume="mochi.costume"
          scene="none"
          :size="72"
        />
      </div>
      <p class="text-xs font-semibold tracking-wide text-pink-500 uppercase">
        Mochi mood · {{ mood }}
      </p>
      <p class="text-center text-base text-foreground">{{ message }}</p>
      <div class="flex gap-2 w-full mt-1">
        <Button variant="outline" class="flex-1" @click="dismissed = true">
          Dismiss
        </Button>
        <Button
          class="flex-1 bg-brand hover:bg-brand/90 text-brand-foreground"
          @click="goToIdealDay"
        >
          Go to Calendar
        </Button>
      </div>
    </div>
  </div>
</template>
