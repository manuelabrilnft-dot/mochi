<script setup>
import { onMounted, watch } from "vue";
import AppSidebar from "./components/AppSidebar.vue";
import IdealDayReminder from "./components/IdealDayReminder.vue";
import MochiMoodAlert from "./components/MochiMoodAlert.vue";
import { isAuthenticated } from "./composables/useAuth";
import { useRouter } from "./router";
import {
  loadIdealDayActivities,
  startReminderWatch,
  stopReminderWatch,
} from "./composables/useIdealDay";

const { currentRoute } = useRouter();

async function begin() {
  await loadIdealDayActivities();
  startReminderWatch();
}

onMounted(() => {
  if (isAuthenticated.value) begin();
});

watch(isAuthenticated, (loggedIn) => {
  if (loggedIn) {
    begin();
  } else {
    stopReminderWatch();
  }
});
</script>

<template>
  <div class="flex h-screen bg-canvas p-3 gap-3">
    <AppSidebar v-if="isAuthenticated" />
    <main class="flex-1 min-h-0 overflow-auto">
      <component :is="currentRoute?.component" />
    </main>
    <IdealDayReminder v-if="isAuthenticated" />
    <MochiMoodAlert v-if="isAuthenticated" />
  </div>
</template>