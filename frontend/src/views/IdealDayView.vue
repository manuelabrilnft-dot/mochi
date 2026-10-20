<script setup>
import { ref, computed, onMounted, watch } from "vue";
import {
  Plus,
  X,
  Clock,
  Globe,
  Heart,
  Users,
  Moon,
  Briefcase,
  Bed,
  Sun,
  Coffee,
  Save,
} from "lucide-vue-next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import useIdealDay from "@/composables/useIdealDay";
import { user, loadUser } from "@/composables/useUser";
import { moodFromCoefficient } from "@/composables/useMochi";
import MochiCard from "@/components/MochiCard.vue";

const { isLoading, retrieve, update } = useIdealDay();

const moodCoefficient = computed(
  () => user.value?.mochi?.moodCoefficient ?? null,
);
const mood = computed(() => moodFromCoefficient(moodCoefficient.value));

const moodTheme = computed(() => {
  if (moodCoefficient.value == null)
    return {
      bg: "bg-muted",
      label: "text-muted-foreground",
      value: "text-muted-foreground",
    };
  if (mood.value === "Tired")
    return { bg: "bg-red-50", label: "text-red-400", value: "text-red-700" };
  if (mood.value === "Lazy")
    return {
      bg: "bg-amber-50",
      label: "text-amber-500",
      value: "text-amber-700",
    };
  return {
    bg: "bg-emerald-50",
    label: "text-emerald-500",
    value: "text-emerald-700",
  }; // Happy
});

const sleepTime = ref("23:00");
const wakeTime = ref("07:00");
const workHours = ref("8");

const isDirty = ref(false);
watch([sleepTime, wakeTime, workHours], () => {
  isDirty.value = true;
});

function toInputTime(timeStr) {
  if (!timeStr) return "";
  return timeStr.slice(0, 5);
}

function toBackendTime(timeStr) {
  if (!timeStr) return "";
  return timeStr.length === 5 ? `${timeStr}:00` : timeStr;
}

async function loadIdealDay() {
  const data = await retrieve();
  if (!data) return;
  wakeTime.value = toInputTime(data.wake_up_time);
  sleepTime.value = toInputTime(data.bedtime);
  workHours.value = String(data.max_working_hours ?? 8);
  isDirty.value = false;
}

async function savePreferences() {
  await update({
    data: {
      wake_up_time: toBackendTime(wakeTime.value),
      bedtime: toBackendTime(sleepTime.value),
      max_working_hours: parseInt(workHours.value),
    },
    event: () => {
      isDirty.value = false;
    },
  });
}

onMounted(async () => {
  await loadUser({ force: true });
  await loadIdealDay();
});

const sleepDuration = computed(() => {
  const [sh, sm] = sleepTime.value.split(":").map(Number);
  const [wh, wm] = wakeTime.value.split(":").map(Number);
  let diff = wh * 60 + wm - (sh * 60 + sm);
  if (diff < 0) diff += 24 * 60;
  return (diff / 60).toFixed(1);
});

const wakeLabel = computed(() => {
  const [h, m] = wakeTime.value.split(":").map(Number);
  const ampm = h >= 12 ? "PM" : "AM";
  const hh = h > 12 ? h - 12 : h === 0 ? 12 : h;
  return `${hh}:${String(m).padStart(2, "0")} ${ampm}`;
});

const activityName = ref("");
const activityMinutes = ref("30");
const selectedEnergy = ref("medium");
const selectedType = ref("focus");

const typeIcons = {
  focus: Globe,
  health: Heart,
  social: Users,
  reset: Moon,
  work: Briefcase,
};

const typeColors = {
  focus: "text-blue-500 bg-blue-50",
  health: "text-green-500 bg-green-50",
  social: "text-pink-400 bg-pink-50",
  reset: "text-indigo-400 bg-indigo-50",
  work: "text-blue-600 bg-blue-50",
};

const activities = ref([
  { id: 1, name: "Morning walk", minutes: 35, energy: "low", type: "health" },
  {
    id: 2,
    name: "Deep work block",
    minutes: 120,
    energy: "high",
    type: "work",
  },
  {
    id: 3,
    name: "Lunch with a friend",
    minutes: 60,
    energy: "medium",
    type: "social",
  },
  {
    id: 4,
    name: "Language practice",
    minutes: 45,
    energy: "medium",
    type: "focus",
  },
  { id: 5, name: "Workout", minutes: 50, energy: "high", type: "health" },
  { id: 6, name: "Evening reset", minutes: 30, energy: "low", type: "reset" },
]);

function addActivity() {
  if (!activityName.value || !activityMinutes.value) return;
  activities.value.push({
    id: Date.now(),
    name: activityName.value,
    startTime: snapTo30(activityStart.value),
    minutes: parseInt(activityMinutes.value),
    type: selectedType.value,
  });
  activityName.value = "";
  activityMinutes.value = "30";
}

function removeActivity(id) {
  activities.value = activities.value.filter((a) => a.id !== id);
}

// --- Schedule ---
const fmt = (m) => {
  if (m == null || Number.isNaN(Number(m))) return "";
  const totalMins = ((m % (24 * 60)) + 24 * 60) % (24 * 60);
  const h = Math.floor(totalMins / 60);
  const min = totalMins % 60;
  const ampm = h >= 12 ? "PM" : "AM";
  const hh = h > 12 ? h - 12 : h === 0 ? 12 : h;
  return `${hh}:${String(min).padStart(2, "0")} ${ampm}`;
};

const schedule = computed(() => {
  const [wh, wm] = wakeTime.value.split(":").map(Number);
  let current = wh * 60 + wm;

  return activities.value.map((a) => {
    const start = current;
    const end = current + a.minutes;
    current = end + 15;
    return { ...a, startLabel: fmt(start), endLabel: fmt(end) };
  });
});

const totalActivityHours = computed(() =>
  (activities.value.reduce((s, a) => s + a.minutes, 0) / 60).toFixed(1),
);

const totalWorkMins = computed(() =>
  activities.value
    .filter((a) => a.type === "work" || a.type === "focus")
    .reduce((s, a) => s + a.minutes, 0),
);

const workTarget = computed(() => parseInt(workHours.value) * 60);
const workPercent = computed(() =>
  Math.min(100, Math.round((totalWorkMins.value / workTarget.value) * 100)),
);
const dayEnd = computed(() => schedule.value.at(-1)?.endLabel ?? "—");
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">
    <!-- Header -->
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold text-foreground">Ideal Day</h1>
        <p class="text-sm text-muted-foreground mt-0.5">
          {{ activities.length }} activities organized
        </p>
      </div>
      <div
        class="flex items-center gap-1.5 px-3 py-1.5 bg-card border border-border rounded-lg text-sm"
      >
        <Clock class="size-4 text-primary" />
        <span class="text-foreground">{{ wakeLabel }} – {{ dayEnd }}</span>
      </div>
    </div>

    <!-- Daily preferences -->
    <Card>
      <CardHeader class="pb-3">
        <div class="flex justify-between items-center">
          <div>
            <CardTitle class="text-base">Daily preferences</CardTitle>
            <CardDescription>
              Set your sleep schedule and work goals to shape your ideal day
            </CardDescription>
          </div>
          <Button
            size="sm"
            class="gap-1.5"
            :class="
              isDirty
                ? 'bg-primary hover:bg-primary/90 text-primary-foreground'
                : 'bg-muted text-muted-foreground'
            "
            :disabled="!isDirty || isLoading"
            @click="savePreferences"
          >
            <Save class="size-4" />
            {{ isLoading ? "Saving..." : isDirty ? "Save changes" : "Saved" }}
          </Button>
        </div>
      </CardHeader>
      <div class="w-full h-px bg-border" />
      <CardContent class="pt-4">
        <!-- Loading skeleton -->
        <div
          v-if="isLoading && !wakeTime"
          class="h-24 bg-muted rounded-lg animate-pulse"
        />

        <div v-else>
          <div class="grid grid-cols-3 gap-4">
            <div class="flex flex-col gap-1.5">
              <div class="flex items-center gap-1.5">
                <Bed class="size-4 text-indigo-400" />
                <p class="text-xs font-medium text-foreground">Bedtime</p>
              </div>
              <Input v-model="sleepTime" type="time" class="text-sm" />
            </div>

            <div class="flex flex-col gap-1.5">
              <div class="flex items-center gap-1.5">
                <Sun class="size-4 text-amber-400" />
                <p class="text-xs font-medium text-foreground">Wake up</p>
              </div>
              <Input v-model="wakeTime" type="time" class="text-sm" />
            </div>

            <div class="flex flex-col gap-1.5">
              <div class="flex items-center gap-1.5">
                <Briefcase class="size-4 text-blue-500" />
                <p class="text-xs font-medium text-foreground">
                  Work target (hrs)
                </p>
              </div>
              <Input
                v-model="workHours"
                type="number"
                min="1"
                max="16"
                class="text-sm"
              />
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <Card class="p-8">
      <div class="flex flex-col gap-4">
        <CardTitle class="text-base">Mood Coefficient</CardTitle>
        <div class="flex flex-col sm:flex-row items-stretch gap-6">
          <div
            class="rounded-lg p-4 flex flex-col gap-1 sm:max-w-xs transition-colors"
            :class="moodTheme.bg"
          >
            <p class="text-xs" :class="moodTheme.label">Mood Coefficient</p>
            <p class="text-lg font-bold" :class="moodTheme.value">
              {{
                moodCoefficient == null
                  ? "—"
                  : Number(moodCoefficient.toFixed(2))
              }}
            </p>
            <p class="text-xs" :class="moodTheme.label">
              Your Mood Coefficient shows how balanced your lifestyle is. Too
              much work makes Mochi stressed, too much rest makes Mochi lazy.
              Aim for the sweet spot!
            </p>
          </div>
          <MochiCard class="flex-1" />
        </div>
      </div>
    </Card>
  </div>
</template>
