<script setup>
import { ref, computed, onMounted } from "vue";
import {
  Search,
  Plus,
  BookOpen,
  ChevronRight,
  ChevronDown,
  ChevronUp,
  Briefcase,
  GraduationCap,
  Dumbbell,
  Coffee,
  Pencil,
  X,
  Flame,
} from "lucide-vue-next";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import useChallenges from "@/composables/useChallenges";
import useFriends from "@/composables/useFriends";
import { user, loadUser } from "@/composables/useUser";

const { isLoading, retrieve, retrieveOne, create, update, remove } =
  useChallenges();
const { retrieveFriends } = useFriends();

const myUsername = computed(() => user.value?.username ?? null);

// friend list shown in the create/edit modal to pick challenge participants
const friends = ref([]);

// ---------- tabs ----------
const activeTab = ref("joined");
const tabs = [
  { id: "joined", label: "Joined" },
  { id: "discover", label: "Discover" },
  { id: "past", label: "Past" },
];

const templates = [
  {
    id: "work",
    label: "Work",
    icon: Briefcase,
    iconBg: "bg-primary/10",
    iconColor: "text-primary",
    color: "border-primary/40 bg-primary/5",
    goalTemplateId: 1,
    challenges: [
      {
        title: "Deep work streak",
        description: "2 hours of focused work daily for 30 days",
        quantity: 30,
      },
      {
        title: "No-meeting mornings",
        description: "Keep mornings free for 2 weeks",
        quantity: 14,
      },
      {
        title: "Inbox zero",
        description: "Clear your inbox every day for 21 days",
        quantity: 21,
      },
    ],
  },
  {
    id: "study",
    label: "Study",
    icon: GraduationCap,
    iconBg: "bg-purple-50",
    iconColor: "text-purple-500",
    color: "border-purple-200 bg-purple-50/50",
    goalTemplateId: 3,
    challenges: [
      {
        title: "Read daily",
        description: "Read for 30 minutes every day for 21 days",
        quantity: 21,
      },
      {
        title: "Language practice",
        description: "Practice a new language for 15 min daily",
        quantity: 30,
      },
      {
        title: "Course completion",
        description: "Complete one online lesson per day",
        quantity: 14,
      },
    ],
  },
  {
    id: "sport",
    label: "Sport",
    icon: Dumbbell,
    iconBg: "bg-green-50",
    iconColor: "text-green-500",
    color: "border-green-200 bg-green-50/50",
    goalTemplateId: 2,
    challenges: [
      {
        title: "30 day fitness streak",
        description: "Complete one workout every day",
        quantity: 30,
      },
      {
        title: "Morning run",
        description: "Run at least 2km every morning for 2 weeks",
        quantity: 14,
      },
      {
        title: "10k steps daily",
        description: "Hit 10,000 steps every day for 21 days",
        quantity: 21,
      },
    ],
  },
  {
    id: "relax",
    label: "Relax",
    icon: Coffee,
    iconBg: "bg-amber-50",
    iconColor: "text-amber-500",
    color: "border-amber-200 bg-amber-50/50",
    goalTemplateId: 5,
    challenges: [
      {
        title: "Meditation streak",
        description: "Meditate for 10 minutes every day",
        quantity: 21,
      },
      {
        title: "Digital sunset",
        description: "No screens after 9pm for 14 days",
        quantity: 14,
      },
      {
        title: "Journaling habit",
        description: "Write one journal entry per day for 30 days",
        quantity: 30,
      },
    ],
  },
];

function templateForGoalId(goalTemplateId) {
  return templates.find((t) => t.goalTemplateId === goalTemplateId) ?? null;
}

const challenges = ref([]);

function mapChallenge(c) {
  const tpl = templateForGoalId(c.goalTemplateId);
  return {
    id: c.id,
    title: c.title,
    description: c.description,
    quantity: c.quantity ?? 0,
    completed: c.completed ?? false,
    goalTemplateId: c.goalTemplateId ?? 0,
    creatorId: c.creatorId,
    participants: c.participants ?? [],
    icon: tpl?.icon ?? Flame,
    iconBg: tpl?.iconBg ?? "bg-orange-50",
    iconColor: tpl?.iconColor ?? "text-orange-500",
    category: tpl?.id ?? "work",
    leaderboard: null,
  };
}

async function loadChallenges() {
  const data = await retrieve();
  challenges.value = (data ?? []).map(mapChallenge);
}

onMounted(async () => {
  await loadUser();
  await loadChallenges();
  friends.value = await retrieveFriends();
});

const joined = computed(() => challenges.value.filter((c) => !c.completed));
const past = computed(() => challenges.value.filter((c) => c.completed));
const available = computed(() =>
  templates.reduce((n, t) => n + t.challenges.length, 0),
);

function normalizeLeaderboard(raw) {
  if (!raw) return [];

  let rows = [];
  if (Array.isArray(raw)) {
    rows = raw;
  } else if (Array.isArray(raw.entry)) {
    rows = raw.entry;
  } else if (raw.entry && typeof raw.entry === "object") {
    rows = [raw.entry];
  } else if (typeof raw === "object") {
    rows = Object.entries(raw).map(([key, value]) => ({ key, value }));
  }

  return rows
    .map((row) =>
      Array.isArray(row)
        ? { name: String(row[0]), progress: Number(row[1]) || 0 }
        : { name: String(row.key), progress: Number(row.value) || 0 },
    )
    .sort((a, b) => b.progress - a.progress);
}

const expanded = ref({});
async function toggleExpand(challenge) {
  const id = challenge.id;
  expanded.value[id] = !expanded.value[id];
  if (expanded.value[id] && !challenge.leaderboard) {
    const detail = await retrieveOne(id);
    challenge.leaderboard = normalizeLeaderboard(detail?.leaderboard);
  }
}

const showModal = ref(false);
const selectedTemplate = ref(null);
const editingChallenge = ref(null);

const form = ref({
  title: "",
  description: "",
  quantity: 30,
  goalTemplateId: 0,
  // usernames of selected friends to add to the challenge
  participants: [],
});

function resetForm() {
  form.value = {
    title: "",
    description: "",
    quantity: 30,
    goalTemplateId: 0,
    participants: [],
  };
}

function openCreate(preset = null, template = null) {
  editingChallenge.value = null;
  selectedTemplate.value = template?.id ?? null;
  resetForm();
  if (preset) {
    form.value.title = preset.title;
    form.value.description = preset.description;
    form.value.quantity = preset.quantity ?? 30;
    form.value.goalTemplateId = template?.goalTemplateId ?? 0;
  }
  showModal.value = true;
}

function openEdit(challenge) {
  editingChallenge.value = challenge;
  selectedTemplate.value = challenge.category;
  form.value = {
    title: challenge.title,
    description: challenge.description,
    quantity: challenge.quantity ?? 30,
    goalTemplateId: challenge.goalTemplateId ?? 0,
    // creator is always a participant on the backend; preselect only added friends
    participants: (challenge.participants ?? []).filter(
      (p) => p !== myUsername.value,
    ),
  };
  showModal.value = true;
}

function buildPayload() {
  const tpl = templates.find((t) => t.id === selectedTemplate.value);
  return {
    title: form.value.title,
    description: form.value.description,
    quantity: Number(form.value.quantity) || 0,
    goalTemplateId: tpl?.goalTemplateId ?? form.value.goalTemplateId ?? 0,
    // the backend adds the creator automatically and rejects self as a "friend"
    participants: form.value.participants.filter((p) => p !== myUsername.value),
  };
}

async function saveChallenge() {
  if (!form.value.title) return;
  const payload = buildPayload();

  if (editingChallenge.value) {
    await update({
      data: { ...payload, id: editingChallenge.value.id },
      // backend echoes only the submitted body; reload to keep participants/state accurate
      event: loadChallenges,
    });
  } else {
    await create({
      data: payload,
      event: (created) => {
        challenges.value.push(mapChallenge(created));
        activeTab.value = "joined";
      },
    });
  }

  showModal.value = false;
}

async function deleteChallenge(id) {
  await remove({
    id,
    event: () => {
      challenges.value = challenges.value.filter((c) => c.id !== id);
    },
  });
}
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">
    <!-- Header -->
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold text-foreground">Challenges</h1>
        <p class="text-sm text-muted-foreground mt-0.5">
          {{ joined.length }} joined · {{ available }} available
        </p>
      </div>
      <div class="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          class="gap-1.5"
          @click="activeTab = 'discover'"
        >
          <Search class="size-4" /> Browse
        </Button>
        <Button
          size="sm"
          class="bg-brand hover:bg-brand/90 text-white gap-1.5"
          @click="openCreate()"
        >
          <Plus class="size-4" /> Create
        </Button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 bg-card border border-border rounded-lg p-1 w-fit">
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
    <div
      v-if="isLoading && challenges.length === 0"
      class="flex flex-col gap-3"
    >
      <Card v-for="i in 2" :key="i">
        <CardContent class="p-5">
          <div class="h-24 bg-muted rounded-lg animate-pulse" />
        </CardContent>
      </Card>
    </div>

    <!-- Joined -->
    <div v-else-if="activeTab === 'joined'" class="flex flex-col gap-3">
      <Card v-for="challenge in joined" :key="challenge.id">
        <CardContent class="p-5">
          <div class="flex flex-col gap-3">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div
                  class="size-9 rounded-xl flex items-center justify-center"
                  :class="challenge.iconBg"
                >
                  <component
                    :is="challenge.icon"
                    class="size-5"
                    :class="challenge.iconColor"
                  />
                </div>
                <div>
                  <p class="text-sm font-semibold text-foreground">
                    {{ challenge.title }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ challenge.description }}
                  </p>
                </div>
              </div>
              <div class="flex items-center gap-1 shrink-0">
                <Badge variant="outline" class="text-xs font-semibold"
                  >Active</Badge
                >
                <button
                  class="text-muted-foreground hover:text-foreground transition-colors"
                  @click="toggleExpand(challenge)"
                >
                  <component
                    :is="expanded[challenge.id] ? ChevronUp : ChevronDown"
                    class="size-4"
                  />
                </button>
                <button
                  class="text-muted-foreground hover:text-primary transition-colors"
                  @click="openEdit(challenge)"
                >
                  <Pencil class="size-4" />
                </button>
                <button
                  class="text-muted-foreground hover:text-destructive transition-colors"
                  @click="deleteChallenge(challenge.id)"
                >
                  <X class="size-4" />
                </button>
              </div>
            </div>

            <div class="flex gap-6 text-xs pt-1">
              <div>
                <p class="text-muted-foreground">Goal</p>
                <p class="font-semibold text-foreground">
                  {{ challenge.quantity }}
                </p>
              </div>
              <div>
                <p class="text-muted-foreground">Participants</p>
                <p class="font-semibold text-foreground">
                  {{ challenge.participants.length }}
                </p>
              </div>
            </div>

            <!-- Expanded leaderboard -->
            <div
              v-if="expanded[challenge.id]"
              class="border-t border-border pt-3"
            >
              <p class="text-xs font-semibold text-foreground mb-2">
                Leaderboard
              </p>
              <div
                v-if="challenge.leaderboard && challenge.leaderboard.length"
                class="flex flex-col gap-1"
              >
                <div
                  v-for="(entry, i) in challenge.leaderboard"
                  :key="entry.name"
                  class="flex items-center gap-2 px-2 py-1.5 rounded-lg text-xs"
                >
                  <span class="w-3 text-muted-foreground">{{ i + 1 }}</span>
                  <div
                    class="size-5 rounded-full flex items-center justify-center text-[10px] font-medium bg-muted text-muted-foreground"
                  >
                    {{ entry.name.charAt(0).toUpperCase() }}
                  </div>
                  <span class="flex-1 text-foreground">{{ entry.name }}</span>
                  <span class="text-muted-foreground">{{
                    entry.progress
                  }}</span>
                </div>
              </div>
              <p v-else class="text-xs text-muted-foreground">
                No leaderboard data yet
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card v-if="joined.length === 0">
        <CardContent class="p-8 text-center text-muted-foreground text-sm">
          No active challenges. Browse or create one!
        </CardContent>
      </Card>
    </div>

    <!-- Discover -->
    <div v-else-if="activeTab === 'discover'" class="flex flex-col gap-5">
      <div
        v-for="template in templates"
        :key="template.id"
        class="flex flex-col gap-2"
      >
        <div class="flex items-center gap-2">
          <div
            class="size-6 rounded-lg flex items-center justify-center"
            :class="template.iconBg"
          >
            <component
              :is="template.icon"
              class="size-4"
              :class="template.iconColor"
            />
          </div>
          <p class="text-sm font-semibold text-foreground">
            {{ template.label }}
          </p>
        </div>
        <div class="grid grid-cols-3 gap-3">
          <Card
            v-for="c in template.challenges"
            :key="c.title"
            class="hover:shadow-md transition-shadow cursor-pointer border"
            :class="template.color"
            @click="openCreate(c, template)"
          >
            <CardContent class="p-4 flex flex-col gap-2">
              <div
                class="size-8 rounded-lg flex items-center justify-center"
                :class="template.iconBg"
              >
                <component
                  :is="template.icon"
                  class="size-4"
                  :class="template.iconColor"
                />
              </div>
              <p class="text-sm font-semibold text-foreground">{{ c.title }}</p>
              <p class="text-xs text-muted-foreground">{{ c.description }}</p>
              <div class="flex items-center justify-between mt-1">
                <span class="text-xs text-muted-foreground"
                  >{{ c.quantity }} days</span
                >
                <Badge variant="secondary" class="text-xs">Join</Badge>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>

    <!-- Past -->
    <div v-else-if="activeTab === 'past'" class="flex flex-col gap-3">
      <Card v-for="challenge in past" :key="challenge.id">
        <CardContent class="p-5">
          <div class="flex flex-col gap-3">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div
                  class="size-9 rounded-xl flex items-center justify-center"
                  :class="challenge.iconBg"
                >
                  <component
                    :is="challenge.icon"
                    class="size-5"
                    :class="challenge.iconColor"
                  />
                </div>
                <div>
                  <p class="text-sm font-semibold text-foreground line-through">
                    {{ challenge.title }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ challenge.description }}
                  </p>
                </div>
              </div>
              <div class="flex items-center gap-1 shrink-0">
                <Badge variant="secondary" class="text-xs">Completed</Badge>
                <button
                  class="text-muted-foreground hover:text-foreground transition-colors"
                  @click="toggleExpand(challenge)"
                >
                  <component
                    :is="expanded[challenge.id] ? ChevronUp : ChevronDown"
                    class="size-4"
                  />
                </button>
              </div>
            </div>

            <!-- Expanded leaderboard -->
            <div
              v-if="expanded[challenge.id]"
              class="border-t border-border pt-3"
            >
              <p class="text-xs font-semibold text-foreground mb-2">
                Leaderboard
              </p>
              <div
                v-if="challenge.leaderboard && challenge.leaderboard.length"
                class="flex flex-col gap-1"
              >
                <div
                  v-for="(entry, i) in challenge.leaderboard"
                  :key="entry.name"
                  class="flex items-center gap-2 px-2 py-1.5 rounded-lg text-xs"
                >
                  <span class="w-3 text-muted-foreground">{{ i + 1 }}</span>
                  <div
                    class="size-5 rounded-full flex items-center justify-center text-[10px] font-medium bg-muted text-muted-foreground"
                  >
                    {{ entry.name.charAt(0).toUpperCase() }}
                  </div>
                  <span class="flex-1 text-foreground">{{ entry.name }}</span>
                  <span class="text-muted-foreground">{{
                    entry.progress
                  }}</span>
                </div>
              </div>
              <p v-else class="text-xs text-muted-foreground">
                No leaderboard data yet
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card v-if="past.length === 0">
        <CardContent class="p-8 text-center text-muted-foreground text-sm"
          >No past challenges yet</CardContent
        >
      </Card>
    </div>

    <!-- Create / edit modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      @click.self="showModal = false"
    >
      <Card class="w-full max-w-md shadow-xl">
        <CardHeader>
          <div class="flex justify-between items-center">
            <CardTitle class="text-base">{{
              editingChallenge ? "Edit challenge" : "New challenge"
            }}</CardTitle>
            <button
              class="text-muted-foreground hover:text-foreground"
              @click="showModal = false"
            >
              <X class="size-4" />
            </button>
          </div>
          <CardDescription>Customize your challenge details</CardDescription>
        </CardHeader>
        <CardContent class="flex flex-col gap-4">
          <!-- Category picker -->
          <div class="flex flex-col gap-2">
            <p class="text-xs text-muted-foreground">Category</p>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="t in templates"
                :key="t.id"
                class="flex flex-col items-center gap-1 p-2 rounded-lg border-2 transition-colors text-xs"
                :class="
                  selectedTemplate === t.id
                    ? 'border-primary bg-primary/10'
                    : 'border-border hover:border-muted-foreground'
                "
                @click="selectedTemplate = t.id"
              >
                <div
                  class="size-7 rounded-lg flex items-center justify-center"
                  :class="t.iconBg"
                >
                  <component :is="t.icon" class="size-4" :class="t.iconColor" />
                </div>
                <span class="text-muted-foreground">{{ t.label }}</span>
              </button>
            </div>
          </div>

          <div class="flex flex-col gap-1">
            <p class="text-xs text-muted-foreground">Challenge name</p>
            <Input
              v-model="form.title"
              placeholder="e.g. Morning run streak"
              class="text-sm"
            />
          </div>

          <div class="flex flex-col gap-1">
            <p class="text-xs text-muted-foreground">Description</p>
            <Input
              v-model="form.description"
              placeholder="What does this challenge involve?"
              class="text-sm"
            />
          </div>

          <div class="flex flex-col gap-1">
            <p class="text-xs text-muted-foreground">Goal / hour</p>
            <Input
              v-model="form.quantity"
              type="number"
              placeholder="30"
              class="text-sm"
            />
          </div>

          <div class="flex flex-col gap-1">
            <p class="text-xs text-muted-foreground">
              Add friends
              <span v-if="form.participants.length" class="text-primary">
                · {{ form.participants.length }} selected
              </span>
            </p>
            <div
              class="max-h-40 overflow-y-auto rounded-lg border border-border divide-y divide-border"
            >
              <p
                v-if="!friends.length"
                class="text-xs text-muted-foreground px-3 py-3"
              >
                No friends to add yet.
              </p>
              <label
                v-for="f in friends"
                :key="f.id"
                class="flex items-center gap-2.5 px-3 py-2 cursor-pointer hover:bg-accent transition-colors"
              >
                <input
                  type="checkbox"
                  :value="f.username"
                  v-model="form.participants"
                  class="size-4 rounded border-border accent-primary"
                />
                <div
                  class="w-7 h-7 rounded-full bg-muted flex items-center justify-center text-xs font-semibold text-muted-foreground shrink-0"
                >
                  {{ f.username?.[0]?.toUpperCase() ?? "?" }}
                </div>
                <span class="text-sm text-foreground">{{ f.username }}</span>
              </label>
            </div>
          </div>

          <Button
            class="w-full bg-brand hover:bg-brand/90 text-white"
            :disabled="isLoading"
            @click="saveChallenge"
          >
            {{ editingChallenge ? "Save changes" : "Start challenge" }}
          </Button>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
