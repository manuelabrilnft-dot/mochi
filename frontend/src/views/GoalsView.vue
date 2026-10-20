<script setup>
import { ref, computed, onMounted } from "vue";
import {
  Plus,
  X,
  Filter,
  Check,
  Target,
  Flame,
  BookOpen,
  Dumbbell,
  Globe,
  Droplets,
  ChevronDown,
  ChevronUp,
  Pencil,
} from "lucide-vue-next";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import useGoals from "@/composables/useGoals";

const { isLoading, retrieve, create, update, remove } = useGoals();

const activeTab = ref("all");
const tabs = [
  { id: "all", label: "All" },
  { id: "personal", label: "Personal" },
  { id: "completed", label: "Completed" },
];

// ---------- icon map ----------
const ICON_MAP = {
  dumbbell: Dumbbell,
  flame: Flame,
  book: BookOpen,
  globe: Globe,
  droplets: Droplets,
  target: Target,
};
function resolveIcon(key) {
  return ICON_MAP[key] ?? Target;
}

const GOAL_TYPES = [
  {
    id: 1,
    value: "work",
    label: "Work",
    icon: Target,
    iconColor: "text-purple-500",
  },
  {
    id: 2,
    value: "study",
    label: "Study",
    icon: BookOpen,
    iconColor: "text-primary",
  },
  {
    id: 3,
    value: "gym",
    label: "Gym",
    icon: Dumbbell,
    iconColor: "text-green-500",
  },
  {
    id: 4,
    value: "rest",
    label: "Rest",
    icon: Droplets,
    iconColor: "text-cyan-500",
  },
];

function sanitizeHours(value) {
  return Math.max(1, Math.round(Number(value) || 1));
}
function goalType(id) {
  return GOAL_TYPES.find((t) => t.id === id) ?? null;
}
function typeLabel(id) {
  return goalType(id)?.label ?? "—";
}

const goals = ref([]);
const today = new Date().toISOString().split("T")[0];

async function loadGoals() {
  const data = await retrieve(today);
  goals.value = (data ?? []).map((g) => ({
    id: g.personGoalId ?? g.id,
    title: g.title,
    description: g.description,
    category: g.category ?? "personal",
    ends: g.date,
    completed: g.completed ?? false,
    progress: g.progress ?? 0,
    quantity: g.quantity ?? 1,
    goalTemplateId: g.goalTemplateId ?? null,
    xpEarned: g.xpEarned ?? 0,
    participants: g.participants ?? [],
    rank: g.rank ?? null,
    icon: g.icon ?? "target",
    iconBg: g.iconBg ?? "bg-muted",
    iconColor: g.iconColor ?? "text-muted-foreground",
    subgoals: (g.dailyGoals ?? []).map((sg) => ({
      id: sg.id,
      label: sg.title ?? sg.label,
      hours: sg.quantity ?? sg.hours ?? 0,
      progress: sg.progress ?? 0,
      completed: sg.completed ?? false,
      goalTemplateId: sg.goalTemplateId ?? null,
    })),
  }));
}

onMounted(loadGoals);

const filtered = computed(() => {
  if (activeTab.value === "all") return goals.value;
  if (activeTab.value === "completed")
    return goals.value.filter((g) => g.completed);
  return goals.value.filter(
    (g) => g.category === activeTab.value && !g.completed,
  );
});

const activeCount = computed(
  () => goals.value.filter((g) => !g.completed).length,
);
const completedCount = computed(
  () => goals.value.filter((g) => g.completed).length,
);

function subgoalDone(sg) {
  return Math.min(sg.progress ?? 0, sg.hours ?? 0);
}
function totalHours(goal) {
  if (goal.subgoals?.length) {
    return goal.subgoals.reduce((s, sg) => s + (sg.hours ?? 0), 0);
  }
  return goal.quantity ?? 1;
}
function doneHours(goal) {
  if (goal.subgoals?.length) {
    return goal.subgoals.reduce((s, sg) => s + subgoalDone(sg), 0);
  }
  return Math.round((goal.progress ?? 0) * totalHours(goal));
}
function progressPercent(goal) {
  if (goal.subgoals?.length) {
    const t = totalHours(goal);
    return t === 0 ? 0 : Math.round((doneHours(goal) / t) * 100);
  }
  return Math.round((goal.progress ?? 0) * 100);
}
function doneCount(goal) {
  return goal.subgoals?.filter((sg) => sg.completed).length ?? 0;
}

const expanded = ref({});
function toggleExpand(id) {
  expanded.value[id] = !expanded.value[id];
}

const templates = [
  {
    id: "fitness",
    label: "Run a half marathon",
    description: "Build endurance over three months.",
    icon: Dumbbell,
    iconBg: "bg-green-50",
    iconColor: "text-green-500",
    category: "personal",
    subgoals: [
      { label: "Strength training", hours: 1, goalTemplateId: 3 },
      { label: "Endurance run", hours: 2, goalTemplateId: 3 },
    ],
  },
  {
    id: "reading",
    label: "Read 12 books this year",
    description: "One book per month, mixed genres.",
    icon: BookOpen,
    iconBg: "bg-primary/10",
    iconColor: "text-primary",
    category: "personal",
    subgoals: [{ label: "Focused reading", hours: 1, goalTemplateId: 2 }],
  },
  {
    id: "language",
    label: "Learn a new language",
    description: "Daily practice toward fluency.",
    icon: Globe,
    iconBg: "bg-purple-50",
    iconColor: "text-purple-500",
    category: "personal",
    subgoals: [
      { label: "Vocab & grammar", hours: 1, goalTemplateId: 2 },
      { label: "Listening & speaking", hours: 1, goalTemplateId: 2 },
    ],
  },
  {
    id: "balance",
    label: "Work-life balance",
    description: "Stay productive without burning out.",
    icon: Droplets,
    iconBg: "bg-cyan-50",
    iconColor: "text-cyan-500",
    category: "personal",
    subgoals: [
      { label: "Deep work", hours: 2, goalTemplateId: 1 },
      { label: "Recovery & rest", hours: 1, goalTemplateId: 4 },
    ],
  },
  {
    id: "fitness-challenge",
    label: "30 day fitness streak",
    description: "One workout per day for a month.",
    icon: Flame,
    iconBg: "bg-orange-50",
    iconColor: "text-orange-500",
    category: "challenges",
    subgoals: [{ label: "Daily workout", hours: 1, goalTemplateId: 3 }],
  },
];

const showModal = ref(false);
const modalStep = ref("choose");
const selectedTpl = ref(null);
const editingGoal = ref(null);

function newSubgoal() {
  return {
    id: Date.now() + Math.random(),
    label: "",
    hours: 1,
    goalTemplateId: 1,
  };
}

const form = ref({
  title: "",
  description: "",
  date: today,
  subgoals: [newSubgoal()],
});

function openBlank() {
  selectedTpl.value = null;
  editingGoal.value = null;
  form.value = {
    title: "",
    description: "",
    date: today,
    subgoals: [newSubgoal()],
  };
  modalStep.value = "form";
}

function openFromTemplate(tpl) {
  selectedTpl.value = tpl;
  editingGoal.value = null;
  form.value = {
    title: tpl.label,
    description: tpl.description,
    date: today,
    subgoals: tpl.subgoals.map((sg) => ({
      ...sg,
      id: Date.now() + Math.random(),
    })),
  };
  modalStep.value = "form";
}

function openEdit(goal) {
  editingGoal.value = goal;
  selectedTpl.value = null;
  form.value = {
    title: goal.title,
    description: goal.description,
    date: goal.ends ?? today,
    subgoals: (goal.subgoals ?? []).map((sg) => ({
      id: sg.id,
      label: sg.label,
      hours: sg.hours ?? 1,
      goalTemplateId: sg.goalTemplateId ?? 1,
    })),
  };
  modalStep.value = "form";
  showModal.value = true;
}

function addSubgoal() {
  form.value.subgoals.push(newSubgoal());
}
function removeSubgoal(id) {
  form.value.subgoals = form.value.subgoals.filter((sg) => sg.id !== id);
}

const formTotal = computed(() =>
  form.value.subgoals.reduce((s, sg) => s + Number(sg.hours || 0), 0),
);

async function saveGoal() {
  if (!form.value.title) return;

  const payload = {
    date: form.value.date,
    title: form.value.title,
    description: form.value.description,
    quantity: formTotal.value || 1,
    progress: 0.0,
    completed: false,
  };

  if (editingGoal.value) {
    await update({
      id: editingGoal.value.id,
      data: payload,
      date: form.value.date,
      subgoals: form.value.subgoals,
      originalSubgoals: editingGoal.value.subgoals ?? [],
      event: () => loadGoals(),
    });
  } else {
    await create({
      data: payload,
      subgoals: form.value.subgoals,
      event: () => loadGoals(),
    });
  }

  showModal.value = false;
}

async function deleteGoal(id) {
  await remove({
    id,
    event: () => {
      goals.value = goals.value.filter((g) => g.id !== id);
    },
  });
}
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">
    <!-- Header -->
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold text-foreground">Goals</h1>
        <p class="text-sm text-muted-foreground mt-0.5">
          {{ activeCount }} active · {{ completedCount }} completed
        </p>
      </div>
      <div class="flex gap-2">
        <Button variant="outline" size="sm" class="gap-1.5">
          <Filter class="size-4" /> Filter
        </Button>
        <Button
          size="sm"
          class="bg-brand hover:bg-brand/90 text-white gap-1.5"
          @click="
            modalStep = 'choose';
            showModal = true;
          "
        >
          <Plus class="size-4" /> New goal
        </Button>
      </div>
    </div>

    <!-- Tabs -->
    <div
      class="flex gap-1 bg-card border border-border rounded-lg p-1 w-fit"
    >
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="px-4 py-1.5 rounded-md text-sm transition-colors"
        :class="
          activeTab === tab.id
            ? 'bg-muted font-medium text-foreground'
            : 'text-muted-foreground hover:text-foreground'
        "
        @click="activeTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Loading skeleton -->
    <div v-if="isLoading" class="grid grid-cols-2 gap-4">
      <Card v-for="i in 2" :key="i">
        <CardContent class="p-5">
          <div class="h-32 bg-muted rounded-lg animate-pulse" />
        </CardContent>
      </Card>
    </div>

    <!-- Grid -->
    <div v-else class="grid grid-cols-2 gap-4">
      <Card
        v-for="goal in filtered"
        :key="goal.id"
        class="hover:shadow-md transition-shadow"
        :class="goal.completed ? 'opacity-75' : ''"
      >
        <CardContent class="p-5 flex flex-col gap-3">
          <!-- Top row -->
          <div class="flex items-start justify-between gap-2">
            <div class="flex items-center gap-2.5">
              <div
                class="size-8 rounded-lg flex items-center justify-center shrink-0"
                :class="goal.iconBg"
              >
                <component
                  :is="resolveIcon(goal.icon)"
                  class="size-4"
                  :class="goal.iconColor"
                />
              </div>
              <div>
                <p
                  class="text-sm font-semibold"
                  :class="
                    goal.completed
                      ? 'line-through text-muted-foreground'
                      : 'text-foreground'
                  "
                >
                  {{ goal.title }}
                </p>
                <p class="text-xs text-muted-foreground">
                  {{
                    goal.category === "challenges" ? "Challenge" : "Personal"
                  }}
                  <span v-if="goal.participants?.length">
                    · {{ goal.participants.length }} friends</span
                  >
                  <span v-else-if="goal.ends"> · ends {{ goal.ends }}</span>
                </p>
              </div>
            </div>
            <div class="flex items-center gap-1 shrink-0">
              <button
                class="text-muted-foreground hover:text-foreground transition-colors"
                @click="toggleExpand(goal.id)"
              >
                <component
                  :is="expanded[goal.id] ? ChevronUp : ChevronDown"
                  class="size-4"
                />
              </button>
              <button
                class="text-muted-foreground hover:text-primary transition-colors"
                @click="openEdit(goal)"
              >
                <Pencil class="size-4" />
              </button>
              <button
                class="text-muted-foreground hover:text-destructive transition-colors"
                @click="deleteGoal(goal.id)"
              >
                <X class="size-4" />
              </button>
            </div>
          </div>

          <!-- Completed -->
          <div v-if="goal.completed" class="flex flex-col gap-2">
            <p class="text-xs text-green-500 flex items-center gap-1">
              <Check class="size-3" /> Completed {{ goal.ends }}
            </p>
            <div class="w-full h-1.5 bg-green-100 rounded-full overflow-hidden">
              <div class="h-full bg-green-400 rounded-full w-full" />
            </div>
            <p class="text-xs text-muted-foreground">
              +{{ goal.xpEarned ?? 0 }} xp earned
            </p>
          </div>

          <!-- Active -->
          <div v-else class="flex flex-col gap-3">
            <p class="text-xs text-muted-foreground">{{ goal.description }}</p>

            <div>
              <div class="flex justify-between text-xs text-muted-foreground mb-1.5">
                <span>Progress</span>
                <span class="font-medium">{{ progressPercent(goal) }}%</span>
              </div>
              <div
                class="w-full h-1.5 bg-muted rounded-full overflow-hidden"
              >
                <div
                  class="h-full rounded-full transition-all"
                  :class="
                    goal.category === 'challenges'
                      ? 'bg-orange-400'
                      : 'bg-primary'
                  "
                  :style="`width: ${progressPercent(goal)}%`"
                />
              </div>
            </div>

            <!-- Collapsed summary -->
            <div
              v-if="!expanded[goal.id]"
              class="flex items-center justify-between text-xs text-muted-foreground"
            >
              <span v-if="goal.subgoals?.length">
                Today · {{ doneCount(goal) }} of
                {{ goal.subgoals.length }} subgoals
              </span>
              <span v-else>{{ progressPercent(goal) }}% complete</span>
              <span>{{ doneHours(goal) }} / {{ totalHours(goal) }} hr</span>
            </div>

            <!-- Expanded subgoals -->
            <div v-else class="flex flex-col gap-2 pt-1">
              <p class="text-xs font-medium text-muted-foreground mb-0.5">
                Subgoals · {{ totalHours(goal) }} hr total
              </p>
              <div
                v-for="sg in goal.subgoals"
                :key="sg.id"
                class="flex flex-col gap-1"
              >
                <div class="flex items-center gap-2.5">
                  <div
                    class="size-4 rounded-full flex items-center justify-center shrink-0"
                    :class="sg.completed ? 'bg-green-500' : 'bg-muted'"
                  >
                    <Check v-if="sg.completed" class="size-2.5 text-white" />
                  </div>
                  <span
                    class="text-xs flex-1"
                    :class="
                      sg.completed
                        ? 'line-through text-muted-foreground'
                        : 'text-foreground'
                    "
                  >
                    {{ sg.label }}
                    <span class="text-muted-foreground">
                      · {{ typeLabel(sg.goalTemplateId) }}</span
                    >
                  </span>
                  <span class="text-xs text-muted-foreground"
                    >{{ subgoalDone(sg) }} / {{ sg.hours }} hr</span
                  >
                </div>
                <div
                  class="w-full h-1 bg-muted rounded-full overflow-hidden ml-[26px]"
                >
                  <div
                    class="h-full rounded-full transition-all"
                    :class="sg.completed ? 'bg-green-400' : 'bg-primary'"
                    :style="`width: ${sg.hours ? Math.min(100, (subgoalDone(sg) / sg.hours) * 100) : 0}%`"
                  />
                </div>
              </div>
              <p class="text-[11px] text-muted-foreground mt-0.5">
                Complete matching activities in your calendar to make progress.
              </p>
            </div>

            <!-- Participants -->
            <div
              v-if="goal.participants?.length"
              class="flex items-center justify-between"
            >
              <div class="flex -space-x-1">
                <div
                  v-for="(p, i) in goal.participants.slice(0, 4)"
                  :key="i"
                  class="size-6 rounded-full bg-muted border-2 border-white flex items-center justify-center text-[10px] font-medium text-muted-foreground"
                >
                  {{ p }}
                </div>
              </div>
              <p v-if="goal.rank" class="text-xs text-muted-foreground">
                You're #{{ goal.rank }}
              </p>
            </div>

            <div
              v-if="progressPercent(goal) >= 30 && !goal.participants?.length"
              class="flex justify-end"
            >
              <span class="text-xs text-green-500">→ On track</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Create card -->
      <button
        class="border-2 border-dashed border-border rounded-xl flex flex-col items-center justify-center gap-2 p-8 text-muted-foreground hover:border-primary hover:text-primary transition-colors min-h-[160px]"
        @click="
          modalStep = 'choose';
          showModal = true;
        "
      >
        <Plus class="size-6" />
        <span class="text-sm">Create a new goal</span>
      </button>
    </div>

    <!-- Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4"
      @click.self="showModal = false"
    >
      <div
        class="bg-card rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto"
      >
        <!-- Step 1: Choose template -->
        <div v-if="modalStep === 'choose'" class="p-6 flex flex-col gap-4">
          <div class="flex justify-between items-center">
            <div>
              <h2 class="text-base font-bold text-foreground">New goal</h2>
              <p class="text-xs text-muted-foreground mt-0.5">
                Start from a template or create your own
              </p>
            </div>
            <button
              class="text-muted-foreground hover:text-foreground"
              @click="showModal = false"
            >
              <X class="size-4" />
            </button>
          </div>

          <p
            class="text-xs font-semibold text-muted-foreground uppercase tracking-wide"
          >
            Templates
          </p>
          <div class="flex flex-col gap-2">
            <button
              v-for="tpl in templates"
              :key="tpl.id"
              class="flex items-center gap-3 p-3 rounded-xl border border-border hover:border-primary hover:bg-primary/5 transition-colors text-left"
              @click="openFromTemplate(tpl)"
            >
              <div
                class="size-8 rounded-lg flex items-center justify-center shrink-0"
                :class="tpl.iconBg"
              >
                <component
                  :is="tpl.icon"
                  class="size-4"
                  :class="tpl.iconColor"
                />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-foreground">{{ tpl.label }}</p>
                <p class="text-xs text-muted-foreground truncate">
                  {{ tpl.description }}
                </p>
              </div>
              <span class="text-xs text-muted-foreground shrink-0">
                {{ tpl.subgoals.reduce((s, sg) => s + sg.hours, 0) }} hr/day
              </span>
            </button>
          </div>

          <div class="flex items-center gap-2">
            <div class="flex-1 h-px bg-muted" />
            <span class="text-xs text-muted-foreground">or</span>
            <div class="flex-1 h-px bg-muted" />
          </div>

          <Button
            class="w-full bg-brand hover:bg-brand/90 text-white gap-1.5"
            @click="openBlank"
          >
            <Plus class="size-4" /> Create from scratch
          </Button>
        </div>

        <!-- Step 2: Form (create or edit) -->
        <div v-else class="p-6 flex flex-col gap-4">
          <div class="flex justify-between items-center">
            <div>
              <h2 class="text-base font-bold text-foreground">
                {{
                  editingGoal
                    ? "Edit goal"
                    : selectedTpl
                      ? "Customize goal"
                      : "New goal"
                }}
              </h2>
              <p class="text-xs text-muted-foreground mt-0.5">
                Fill in the details and set your subgoals
              </p>
            </div>
            <button
              class="text-muted-foreground hover:text-foreground"
              @click="showModal = false"
            >
              <X class="size-4" />
            </button>
          </div>

          <div class="flex flex-col gap-1">
            <p class="text-xs text-muted-foreground">Goal title</p>
            <Input
              v-model="form.title"
              placeholder="e.g. Run a half marathon"
              class="text-sm"
            />
          </div>

          <div class="flex flex-col gap-1">
            <p class="text-xs text-muted-foreground">Description</p>
            <Input
              v-model="form.description"
              placeholder="What does this goal involve?"
              class="text-sm"
            />
          </div>

          <div class="flex flex-col gap-1">
            <p class="text-xs text-muted-foreground">Date</p>
            <Input v-model="form.date" type="date" class="text-sm" />
          </div>

          <!-- Subgoals -->
          <div class="flex flex-col gap-2">
            <div class="flex items-center justify-between">
              <p class="text-xs text-muted-foreground">Subgoals</p>
              <span class="text-xs text-primary font-medium"
                >{{ formTotal }} hr total</span
              >
            </div>
            <p class="text-[11px] text-muted-foreground -mt-1">
              Each subgoal tracks a type. Completed calendar activities of that
              type add to its progress.
            </p>
            <div class="flex flex-col gap-2">
              <div
                v-for="(sg, i) in form.subgoals"
                :key="sg.id"
                class="flex items-center gap-2"
              >
                <div
                  class="w-5 h-5 rounded border border-border flex items-center justify-center text-[10px] text-muted-foreground shrink-0"
                >
                  {{ i + 1 }}
                </div>
                <Input
                  v-model="sg.label"
                  placeholder="Subgoal name"
                  class="text-sm flex-1"
                />
                <select
                  v-model.number="sg.goalTemplateId"
                  class="h-9 rounded-md border border-border bg-card px-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                >
                  <option v-for="t in GOAL_TYPES" :key="t.id" :value="t.id">
                    {{ t.label }}
                  </option>
                </select>
                <Input
                  v-model.number="sg.hours"
                  type="number"
                  min="1"
                  step="1"
                  class="text-sm w-16 text-center"
                  @blur="sg.hours = sanitizeHours(sg.hours)"
                />
                <span class="text-xs text-muted-foreground">hr</span>
                <button
                  class="text-muted-foreground hover:text-destructive transition-colors shrink-0"
                  @click="removeSubgoal(sg.id)"
                >
                  <X class="size-3.5" />
                </button>
              </div>
            </div>
            <button
              class="flex items-center gap-1.5 text-xs text-primary hover:text-primary mt-1"
              @click="addSubgoal"
            >
              <Plus class="size-3.5" /> Add subgoal
            </button>
          </div>

          <!-- Summary -->
          <div
            class="bg-muted rounded-xl p-3 flex items-center justify-between"
          >
            <p class="text-xs text-muted-foreground">Daily time commitment</p>
            <p class="text-sm font-bold text-foreground">
              {{ formTotal }} hr / day
            </p>
          </div>

          <div class="flex gap-2 pt-1">
            <Button
              variant="outline"
              class="flex-1"
              @click="
                editingGoal ? (showModal = false) : (modalStep = 'choose')
              "
            >
              {{ editingGoal ? "Cancel" : "Back" }}
            </Button>
            <Button
              class="flex-1 bg-brand hover:bg-brand/90 text-white"
              @click="saveGoal"
            >
              {{ editingGoal ? "Save changes" : "Save goal" }}
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
