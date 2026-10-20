import { ref } from "vue";
import {
  getIdealDayActivitiesService,
  saveIdealDayActivitiesService,
} from "@/services/idealDayActivity";
import { getIdealDay, updateIdealDay } from "@/services/idealDay";

// module level shared refs so the page and the reminder see the same list
export const activities = ref([]);
export const loading = ref(false);

// reminder popup state
export const reminder = ref(null);

const REMINDER_MINUTES = 10;
let timer = null;
const firedToday = new Set();

export async function loadIdealDayActivities() {
  loading.value = true;
  try {
    const data = await getIdealDayActivitiesService();
    activities.value = Array.isArray(data) ? data : [];
  } catch (e) {
    activities.value = [];
  } finally {
    loading.value = false;
  }
}

export async function saveIdealDayActivities() {
  const saved = await saveIdealDayActivitiesService(activities.value);
  activities.value = Array.isArray(saved) ? saved : activities.value;
  firedToday.clear();
  return saved;
}

// turns "09:00" or "09:00:00" into minutes since midnight
function toMinutes(startTime) {
  if (!startTime) return null;
  const [h, m] = startTime.split(":").map(Number);
  return h * 60 + m;
}

function label(startTime) {
  const mins = toMinutes(startTime);
  if (mins === null) return "";
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  const ampm = h >= 12 ? "PM" : "AM";
  const hh = h > 12 ? h - 12 : h === 0 ? 12 : h;
  return `${hh}:${String(m).padStart(2, "0")} ${ampm}`;
}

function check() {
  const now = new Date();
  const nowMinutes = now.getHours() * 60 + now.getMinutes();
  for (const a of activities.value) {
    const start = toMinutes(a.startTime);
    if (start === null) continue;
    const diff = start - nowMinutes;
    const key = `${a.id}-${a.startTime}`;
    if (diff === REMINDER_MINUTES && !firedToday.has(key)) {
      firedToday.add(key);
      reminder.value = { name: a.name, startLabel: label(a.startTime) };
    }
  }
}

export function dismissReminder() {
  reminder.value = null;
}

// starts the once-a-minute check; safe to call more than once
export function startReminderWatch() {
  if (timer) return;
  check();
  timer = setInterval(check, 60 * 1000);
}

export function stopReminderWatch() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

export default function useIdealDay() {
  const isLoading = ref(false);

  async function retrieve() {
    console.log("Retrieving ideal day preferences...");
    isLoading.value = true;
    try {
      const response = await getIdealDay();
      if (!response || !response.ok) return null;
      const data = await response.json();
      console.log(data);
      return data;
    } catch (e) {
      console.error(e);
      return null;
    } finally {
      isLoading.value = false;
    }
  }

  async function update({ data, event }) {
    console.log("Updating ideal day preferences...");
    isLoading.value = true;
    try {
      const response = await updateIdealDay(data);
      if (!response || !response.ok) return false;
      event?.();
      return true;
    } catch (e) {
      console.error(e);
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  return { isLoading, retrieve, update };
}
