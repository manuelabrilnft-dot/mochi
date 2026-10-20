<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ChevronLeft, ChevronRight, Check } from "@lucide/vue";
import { today, getLocalTimeZone, CalendarDate } from "@internationalized/date";

import useActivity from "@/composables/useActivity";
import validator from "@/models/validator";
import { useForm } from "@/composables/useForm";
import { AlertCircle } from "lucide-vue-next";

const { create, retrieve, complete, edit, remove } = useActivity();

const todayDate = today(getLocalTimeZone());
const weekAnchor = ref(todayDate);

const startHour = 0;
const slotHeight = 40;
const events = ref([]);
const weekScroll = ref(null);

// Header row (h-14 = 56px) plus the hour slots above the target time, minus a
// small offset so the current hour isn't flush against the sticky header.
function scrollToHour(hour) {
  const el = weekScroll.value;
  if (!el) return;
  el.scrollTop = Math.max(0, (hour - startHour) * slotHeight - slotHeight);
}

const eventColors = {
  green: "bg-event-green border-event-green-border text-event-foreground",
  orange: "bg-event-orange border-event-orange-border text-event-foreground",
  purple: "bg-event-purple border-event-purple-border text-event-foreground",
};
const categories = [
  { id: "gym", label: "Gym", color: "orange" },
  { id: "work", label: "Work", color: "purple" },
  { id: "study", label: "Study", color: "purple" },
  { id: "rest", label: "Rest", color: "green" },
];

const showAddModal = ref(false);

const activitySchema = validator
  .schema({
    title: validator.string().required("Title is required"),
    description: validator.string(),
    day: validator.string().required("Date is required"),
    startTime: validator.string().required("Start time is required"),
    endTime: validator.string().required("End time is required"),
    type: validator.string().required("Category is required"),
  })
  .refine((data) => data.endTime > data.startTime, {
    field: "endTime",
    message: "End time must be after start time",
  });

const { fields, errors, handleSubmit } = useForm({
  validationSchema: activitySchema,
});
fields.startTime = "09:00";
fields.endTime = "10:00";

// The backend only accepts activities that match a 30-minute Timeslot, so the
// time pickers snap to a 30-minute grid and a longer selection is created (and
// completed) as several back-to-back 30-minute activities.
const SLOT_MINUTES = 30;
const DAY_MINUTES = 24 * 60;

function labelToMinutes(label) {
  const [h, m] = label.split(":").map(Number);
  return h * 60 + m;
}
function minutesToLabel(total) {
  const h = Math.floor(total / 60);
  const m = total % 60;
  return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
}

function minutesToApiTime(total) {
  if (total >= DAY_MINUTES) return "23:59:59";
  return minutesToLabel(total) + ":00";
}

const startTimeOptions = [];
for (let m = 0; m <= DAY_MINUTES - SLOT_MINUTES; m += SLOT_MINUTES) {
  startTimeOptions.push(minutesToLabel(m));
}
const endTimeOptions = [];
for (let m = SLOT_MINUTES; m <= DAY_MINUTES; m += SLOT_MINUTES) {
  endTimeOptions.push(minutesToLabel(m));
}

// error returned by the backend when an activity falls outside the user's
// awake/working hours (shown in the add modal)
const addError = ref("");

async function openAddModal() {
  addError.value = "";
  Object.assign(fields, {
    title: "",
    description: "",
    // defaults to today; the date picker lets the user pick any day
    day: toApiDate(todayDate),
    startTime: "09:00",
    endTime: "10:00",
    type: "",
  });
  showAddModal.value = true;
  await nextTick();
  Object.assign(errors, {
    title: null,
    description: null,
    day: null,
    startTime: null,
    endTime: null,
    type: null,
  });
}

const onAdd = handleSubmit(async (values) => {
  addError.value = "";
  const activityDate = values.day;
  const startMinutes = labelToMinutes(values.startTime);
  const endMinutes = labelToMinutes(values.endTime);

  for (let m = startMinutes; m < endMinutes; m += SLOT_MINUTES) {
    const payload = {
      title: values.title,
      description: values.description,
      activityDate,
      startTime: minutesToApiTime(m),
      endTime: minutesToApiTime(m + SLOT_MINUTES),
      type: values.type,
      completed: false,
    };
    const result = await create({ data: payload, event: () => {} });
    // stop and surface the backend message (e.g. outside awake/working hours)
    if (result && !result.ok) {
      addError.value = result.error || "Couldn't create activity";
      await fetchActivities();
      return;
    }
  }

  showAddModal.value = false;
  await fetchActivities();
});

async function markComplete(event) {
  if (event.completed || !event.ids?.length) return;
  let allOk = true;
  for (const id of event.ids) {
    const ok = await complete(id);
    allOk = allOk && ok;
  }
  if (allOk) event.completed = true;
  await fetchActivities();
}

const showEditModal = ref(false);
const editingEvent = ref(null);
const editForm = ref({ title: "", description: "" });

function openEditModal(event) {
  editingEvent.value = event;
  editForm.value = { title: event.title, description: event.description ?? "" };
  showEditModal.value = true;
}

async function onEdit() {
  const event = editingEvent.value;
  if (!event || !editForm.value.title) return;
  for (const id of event.ids) {
    await edit({
      id,
      title: editForm.value.title,
      description: editForm.value.description,
    });
  }
  showEditModal.value = false;
  editingEvent.value = null;
  await fetchActivities();
}

// an event groups the back-to-back 30-minute slots it was created as, so
// deleting it removes every underlying activity id
async function onDelete() {
  const event = editingEvent.value;
  if (!event?.ids?.length) return;
  for (const id of event.ids) {
    await remove(id);
  }
  showEditModal.value = false;
  editingEvent.value = null;
  await fetchActivities();
}

const monthNames = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];
const dayNames = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
const hours = Array.from(
  { length: 24 },
  (_, i) => `${String(i).padStart(2, "0")}:00`,
);

function prevWeek() {
  weekAnchor.value = weekAnchor.value.subtract({ days: 7 });
}
function nextWeek() {
  weekAnchor.value = weekAnchor.value.add({ days: 7 });
}
function goToday() {
  weekAnchor.value = todayDate;
}

function isToday(date) {
  return (
    date.year === todayDate.year &&
    date.month === todayDate.month &&
    date.day === todayDate.day
  );
}

const currentWeek = computed(() => {
  let dow = new Date(
    weekAnchor.value.year,
    weekAnchor.value.month - 1,
    weekAnchor.value.day,
  ).getDay();
  dow = dow === 0 ? 6 : dow - 1;
  const monday = weekAnchor.value.subtract({ days: dow });
  return Array.from({ length: 7 }, (_, i) => monday.add({ days: i }));
});

const weekRangeLabel = computed(() => {
  const first = currentWeek.value[0];
  const last = currentWeek.value[6];
  const firstMonth = monthNames[first.month - 1].slice(0, 3);
  const lastMonth = monthNames[last.month - 1].slice(0, 3);
  if (first.month === last.month) {
    return `${firstMonth} ${first.day} - ${last.day}`;
  }
  return `${firstMonth} ${first.day} - ${lastMonth} ${last.day}`;
});

const weekNumberLabel = computed(() => {
  const first = currentWeek.value[0];
  const date = new Date(first.year, first.month - 1, first.day);
  const startOfYear = new Date(first.year, 0, 1);
  const dayOfYear = Math.floor((date - startOfYear) / 86400000) + 1;
  const weekNumber = Math.ceil(dayOfYear / 7);
  return `Week ${weekNumber}, ${first.year}`;
});

function eventsForDay(dayIndex) {
  return events.value.filter((e) => e.day === dayIndex);
}
function eventStyle(event) {
  return {
    top: (event.start - startHour) * slotHeight + "px",
    height: (event.end - event.start) * slotHeight + "px",
  };
}
const nowLine = computed(() => {
  const now = new Date();
  const hour = now.getHours() + now.getMinutes() / 60;
  if (hour < startHour || hour > startHour + hours.length) return null;
  return (hour - startHour) * slotHeight;
});

function toApiDate(date) {
  return `${date.year}-${String(date.month).padStart(2, "0")}-${String(date.day).padStart(2, "0")}`;
}

function parseTimeToDecimal(timeStr) {
  const [h, m] = timeStr.split(":").map(Number);
  return h + m / 60;
}

function activityToEvent(activity) {
  const [y, mo, d] = activity.activityDate.split("-").map(Number);
  const actDate = new CalendarDate(y, mo, d);
  const dayIndex = currentWeek.value.findIndex(
    (date) =>
      date.year === actDate.year &&
      date.month === actDate.month &&
      date.day === actDate.day,
  );
  if (dayIndex === -1) return null;
  const cat = categories.find((c) => c.id === activity.type);
  return {
    ids: activity.ids ?? [activity.id],
    day: dayIndex,
    start: parseTimeToDecimal(activity.startTime),
    end: parseTimeToDecimal(activity.endTime),
    title: activity.title,
    description: activity.description ?? "",
    type: activity.type,
    completed: activity.completed ?? false,
    color: cat?.color ?? "green",
  };
}

const LINE_HEIGHT = 12;
const TITLE_ROW = 18;
function descLines(event) {
  const heightPx = (event.end - event.start) * slotHeight;
  const remaining = heightPx - TITLE_ROW - 4;
  if (remaining < LINE_HEIGHT) return 0;
  return Math.floor(remaining / LINE_HEIGHT);
}
function eventTimeLabel(event) {
  if (!event) return "";
  return `${minutesToLabel(Math.round(event.start * 60))} – ${minutesToLabel(Math.round(event.end * 60))}`;
}

function mergeActivities(activities) {
  const groups = new Map();
  for (const act of activities) {
    const key = `${act.activityDate}|${act.title}|${act.description}|${act.completed}|${act.type}`;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(act);
  }

  const blocks = [];
  for (const group of groups.values()) {
    group.sort((a, b) => a.startTime.localeCompare(b.startTime));
    let block = null;
    for (const act of group) {
      if (block && act.startTime === block.endTime) {
        block.ids.push(act.id);
        block.endTime = act.endTime;
      } else {
        block = { ...act, ids: [act.id] };
        blocks.push(block);
      }
    }
  }
  return blocks;
}

const fetchActivities = async () => {
  const start = currentWeek.value[0];
  const end = currentWeek.value[6];
  const result = await retrieve({
    startDate: toApiDate(start),
    endDate: toApiDate(end),
  });
  if (result) {
    events.value = mergeActivities(result).map(activityToEvent).filter(Boolean);
  }
};

watch(weekAnchor, fetchActivities);

onMounted(async () => {
  await fetchActivities();
  await nextTick();
  scrollToHour(new Date().getHours());
});

</script>

<template>
  <div class="flex h-screen flex-col overflow-hidden bg-canvas p-8">
    <!-- Page Header -->
    <div class="flex items-center justify-between mb-4 shrink-0">
      <div>
        <h1 class="text-xl font-bold text-foreground">
          {{ weekRangeLabel }}
        </h1>
        <p class="text-sm text-muted-foreground mt-0.5">
          {{ weekNumberLabel }}
        </p>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" size="sm" @click="goToday">Today</Button>
        <Button variant="outline" size="icon" @click="prevWeek">
          <ChevronLeft class="h-4 w-4" />
        </Button>
        <Button variant="outline" size="icon" @click="nextWeek">
          <ChevronRight class="h-4 w-4" />
        </Button>
        <Button
          size="sm"
          class="bg-brand text-brand-foreground hover:bg-brand/90"
          @click="openAddModal"
        >
          + Add activity
        </Button>
      </div>
    </div>

    <!-- Calendar Box -->
    <Card class="p-0 gap-0 flex-1 min-h-0">
      <!-- WEEK VIEW -->
      <div ref="weekScroll" class="flex flex-1 min-h-0 overflow-y-auto">
        <!-- Hours -->
        <div class="w-14 shrink-0 border-r">
          <div class="h-14 border-b sticky top-0 z-20 bg-card" />
          <div
            v-for="hour in hours"
            :key="hour"
            class="h-10 border-b text-xs text-muted-foreground text-right pr-2"
          >
            {{ hour }}
          </div>
        </div>

        <!-- Days -->
        <div class="flex-1 grid grid-cols-7">
          <div
            v-for="(date, i) in currentWeek"
            :key="date.toString()"
            class="border-r last:border-r-0"
            :class="isToday(date) ? 'bg-accent/40' : ''"
          >
            <div
              class="h-14 border-b flex flex-col items-center justify-center sticky top-0 z-10"
              :class="isToday(date) ? 'bg-accent' : 'bg-card'"
            >
              <span
                class="text-[10px] tracking-wider uppercase text-muted-foreground"
                >{{ dayNames[i] }}</span
              >
              <span
                class="text-base font-semibold"
                :class="isToday(date) ? 'text-primary' : ''"
                >{{ date.day }}</span
              >
            </div>

            <div
              class="relative"
              :style="{ height: hours.length * slotHeight + 'px' }"
            >
              <div v-for="h in hours" :key="h" class="h-10 border-b" />

              <div
                v-for="event in eventsForDay(i)"
                :key="event.title + event.start"
                class="group absolute left-1 right-1 cursor-pointer rounded-md border-l-4 px-2 py-1 text-xs font-medium overflow-hidden"
                :class="[
                  eventColors[event.color],
                  event.completed ? 'opacity-60' : '',
                ]"
                :style="eventStyle(event)"
                title="Click to edit"
                @click="openEditModal(event)"
              >
                <div class="flex items-start justify-between gap-1">
                  <span :class="event.completed ? 'line-through' : ''">{{
                    event.title
                  }}</span>
                  <button
                    v-if="!event.completed"
                    type="button"
                    title="Mark as complete"
                    class="shrink-0 rounded p-0.5 opacity-0 transition-opacity hover:bg-accent hover:text-green-600 group-hover:opacity-100"
                    @click.stop="markComplete(event)"
                  >
                    <Check class="h-3.5 w-3.5" />
                  </button>
                  <Check v-else class="h-3.5 w-3.5 shrink-0 text-green-600" />
                </div>
                <p
                  v-if="
                    event.end - event.start >= 1 &&
                    event.description &&
                    descLines(event) > 0
                  "
                  class="font-normal opacity-80 text-[10px] leading-3 mt-0.5"
                  :style="{
                    display: '-webkit-box',
                    WebkitBoxOrient: 'vertical',
                    WebkitLineClamp: descLines(event),
                    overflow: 'hidden',
                  }"
                >
                  {{ event.description }}
                </p>
              </div>

              <div
                v-if="isToday(date) && nowLine !== null"
                class="absolute left-0 right-0 flex items-center"
                :style="{ top: nowLine + 'px' }"
              >
                <span class="w-2 h-2 rounded-full bg-red-500 -ml-1" />
                <span class="flex-1 h-px bg-red-500" />
              </div>
            </div>
          </div>
        </div>
      </div>

    </Card>

    <!-- Add activity modal -->
    <div
      v-if="showAddModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      @click.self="showAddModal = false"
    >
      <form
        class="bg-card ring-1 ring-foreground rounded-xl shadow-xs p-6 w-full max-w-md"
        @submit="onAdd"
      >
        <h2 class="text-lg font-semibold mb-4">Add activity</h2>

        <div
          v-if="addError"
          class="mt-4 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive flex flex-row gap-2"
        >
          <AlertCircle class="size-5" />
          {{ addError }}
        </div>

        <div class="space-y-3">
          <div>
            <label class="text-sm text-muted-foreground">Title</label>
            <input
              v-model="fields.title"
              type="text"
              placeholder="e.g. Study session"
              class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
              :class="errors.title ? 'border-2 border-destructive' : ''"
            />
            <p class="text-xs text-destructive mt-1">{{ errors.title }}</p>
          </div>

          <div>
            <label class="text-sm text-muted-foreground">Description</label>
            <input
              v-model="fields.description"
              type="text"
              placeholder="Optional"
              class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
            />
          </div>

          <div>
            <label class="text-sm text-muted-foreground">Date</label>
            <input
              v-model="fields.day"
              type="date"
              class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
              :class="errors.day ? 'border-2 border-destructive' : ''"
            />
            <p class="text-xs text-destructive mt-1">{{ errors.day }}</p>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm text-muted-foreground">Start</label>
              <select
                v-model="fields.startTime"
                class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
                :class="errors.startTime ? 'border-2 border-destructive' : ''"
              >
                <option v-for="t in startTimeOptions" :key="t" :value="t">
                  {{ t }}
                </option>
              </select>
              <p class="text-xs text-destructive mt-1">
                {{ errors.startTime }}
              </p>
            </div>
            <div>
              <label class="text-sm text-muted-foreground">End</label>
              <select
                v-model="fields.endTime"
                class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
                :class="errors.endTime ? 'border-2 border-destructive' : ''"
              >
                <option v-for="t in endTimeOptions" :key="t" :value="t">
                  {{ t }}
                </option>
              </select>
              <p class="text-xs text-destructive mt-1">{{ errors.endTime }}</p>
            </div>
          </div>

          <div>
            <label class="text-sm text-muted-foreground">Category</label>
            <select
              v-model="fields.type"
              class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
              :class="errors.type ? 'border-2 border-destructive' : ''"
            >
              <option value="" disabled>Select a category</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">
                {{ c.label }}
              </option>
            </select>
            <p class="text-xs text-destructive mt-1">{{ errors.type }}</p>
          </div>
        </div>

        <div class="flex justify-end gap-2 mt-6">
          <Button
            variant="outline"
            size="sm"
            type="button"
            @click="showAddModal = false"
            >Cancel</Button
          >
          <Button
            type="submit"
            size="sm"
            class="bg-brand text-brand-foreground hover:bg-brand/90"
            >Add</Button
          >
        </div>
      </form>
    </div>

    <div
      v-if="showEditModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      @click.self="showEditModal = false"
    >
      <form
        class="bg-card border rounded-lg p-6 w-full max-w-md"
        @submit.prevent="onEdit"
      >
        <h2 class="text-lg font-semibold mb-1">Edit activity</h2>
        <p class="text-xs text-muted-foreground mb-4">
          {{ eventTimeLabel(editingEvent) }} · only the title and description
          can be changed.
        </p>

        <div class="space-y-3">
          <div>
            <label class="text-sm text-muted-foreground">Title</label>
            <input
              v-model="editForm.title"
              type="text"
              placeholder="e.g. Study session"
              class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
            />
          </div>

          <div>
            <label class="text-sm text-muted-foreground">Description</label>
            <input
              v-model="editForm.description"
              type="text"
              placeholder="Optional"
              class="w-full border rounded-md px-3 py-2 text-sm bg-background mt-1"
            />
          </div>
        </div>

        <div class="flex justify-between items-center mt-6">
          <Button
            variant="ghost"
            size="sm"
            type="button"
            class="text-destructive hover:text-destructive"
            @click="onDelete"
            >Delete</Button
          >
          <div class="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              type="button"
              @click="showEditModal = false"
              >Cancel</Button
            >
            <Button
              type="submit"
              size="sm"
              class="bg-brand text-brand-foreground hover:bg-brand/90"
              >Save</Button
            >
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
