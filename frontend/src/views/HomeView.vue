<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  Flame,
  Plus,
  Sparkles,
  ChevronLeft,
  ChevronRight,
} from 'lucide-vue-next'
import { navigate } from '@/router'
import useActivity from '@/composables/useActivity'
import useGoals from '@/composables/useGoals'
import useAchievements from '@/composables/useAchievements'
import useChallenges from '@/composables/useChallenges'
import { useMochi, gameProps } from '@/composables/useMochi'
import MochiCard from '@/components/MochiCard.vue'
import { Card } from '@/components/ui/card'

const { retrieve } = useActivity()
const { retrieve: retrieveGoals, create: createGoal } = useGoals()
const { retrieve: retrieveAchievements } = useAchievements()
const { retrieve: retrieveChallenges } = useChallenges()
const { load: loadMochi } = useMochi()

// ── Date / greeting ───────────────────────────────────────────────────────────

const now = new Date()

const today = now.toLocaleDateString('en-US', {
  weekday: 'long',
  month:   'long',
  day:     'numeric',
})

const hour = now.getHours()
const greeting =
  hour < 12 ? 'Good morning' :
  hour < 18 ? 'Good afternoon' :
              'Good evening'

// ── Stats ─────────────────────────────────────────────────────────────────────

// streak comes from the profile (via gameProps, loaded by loadMochi)
const streak = computed(() => gameProps.value.streak ?? 0)

const stats = computed(() => [
  { label: 'Level',        value: String(gameProps.value.level ?? 0) },
  { label: 'Total XP',     value: String(gameProps.value.xp ?? 0) },
  { label: 'Goals active', value: String(goals.value.filter(g => !g.done).length) },
  { label: 'Badges',       value: String(badgeCount.value) },
])

// ── Mini calendar ─────────────────────────────────────────────────────────────

const calMonth = ref(now.getMonth())
const calYear  = ref(now.getFullYear())

const monthName = computed(() =>
  new Date(calYear.value, calMonth.value, 1)
    .toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
)

function prevMonth() {
  if (calMonth.value === 0) { calMonth.value = 11; calYear.value-- }
  else calMonth.value--
}
function nextMonth() {
  if (calMonth.value === 11) { calMonth.value = 0; calYear.value++ }
  else calMonth.value++
}

const calDays = computed(() => {
  const firstDow = new Date(calYear.value, calMonth.value, 1).getDay()
  const daysInMonth = new Date(calYear.value, calMonth.value + 1, 0).getDate()
  const offset = firstDow === 0 ? 6 : firstDow - 1
  const cells = []
  for (let i = 0; i < offset; i++) cells.push(null)
  for (let d = 1; d <= daysInMonth; d++) cells.push(d)
  return cells
})

const completedDays = ref(new Set())
const partialDays   = ref(new Set())
const todayDate     = now.getDate()

async function loadMonthActivities() {
  const pad = (n) => String(n).padStart(2, "0")
  const start = `${calYear.value}-${pad(calMonth.value + 1)}-01`
  const lastDay = new Date(calYear.value, calMonth.value + 1, 0).getDate()
  const end = `${calYear.value}-${pad(calMonth.value + 1)}-${pad(lastDay)}`

  const activities = await retrieve({ startDate: start, endDate: end })
  if (!activities || activities.length === 0) {
    completedDays.value = new Set()
    partialDays.value = new Set()
    return
  }

  const done = {}
  const total = {}
  for (const a of activities) {
    const day = new Date(a.activityDate ?? a.startTime).getDate()
    total[day] = (total[day] ?? 0) + 1
    if (a.completed) done[day] = (done[day] ?? 0) + 1
  }
  const completed = new Set()
  const partial = new Set()
  for (const day of Object.keys(total)) {
    const d = Number(day)
    if (done[d] === total[d]) completed.add(d)
    else if (done[d] > 0) partial.add(d)
  }
  completedDays.value = completed
  partialDays.value = partial
}

onMounted(loadMonthActivities)
watch([calMonth, calYear], loadMonthActivities)

function dayClass(day) {
  if (!day) return ''
  if (day === todayDate && calMonth.value === now.getMonth() && calYear.value === now.getFullYear())
    return 'bg-brand text-white rounded-lg font-semibold'
  if (completedDays.value.has(day)) return 'bg-emerald-100 text-emerald-800 rounded-lg'
  if (partialDays.value.has(day))   return 'bg-amber-100 text-amber-800 rounded-lg'
  return 'text-muted-foreground hover:bg-accent rounded-lg'
}

// ── Today's goals ─────────────────────────────────────────────────────────────

const goals = ref([])

const goalPage = ref(1)
const totalGoalPages = computed(() => Math.max(1, goals.value.length))

async function loadGoals() {
  const pad = (n) => String(n).padStart(2, "0")
  const dateStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
  const data = await retrieveGoals(dateStr)
  if (!data || data.length === 0) {
    goals.value = []
    return
  }
  goals.value = data.map((g) => ({
    id: g.personGoalId ?? g.id,
    text: g.title ?? g.label,
    time: g.time ?? null,
    xp: g.xp ? `+${g.xp} xp` : '',
    done: g.done ?? g.completed ?? false,
    type: g.category ?? 'personal',
  }))
}

// quick add a personal goal straight from the dashboard
const showQuickAdd = ref(false)
const quickTitle = ref('')
const quickSaving = ref(false)

function toggleQuickAdd() {
  showQuickAdd.value = !showQuickAdd.value
  if (!showQuickAdd.value) quickTitle.value = ''
}

async function submitQuickAdd() {
  const title = quickTitle.value.trim()
  if (!title) return
  quickSaving.value = true

  const pad = (n) => String(n).padStart(2, "0")
  const dateStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`

  goals.value.unshift({ id: Date.now(), text: title, time: null, xp: '', done: false, type: 'personal' })

  try {
    await createGoal({
      data: {
        date: dateStr,
        title,
        description: title,
        quantity: 1,
        progress: 0.0,
        completed: false,
        goalTemplateId: 0,
      },
      subgoals: [],
      event: () => {},
    })
  } catch (e) {
    // keep the optimistic row even if the save fails offline
  } finally {
    quickSaving.value = false
    quickTitle.value = ''
    showQuickAdd.value = false
  }
}


// ── Active challenges ─────────────────────────────────────────────────────────

const challenges = ref([])

async function loadChallenges() {
  const data = await retrieveChallenges()
  challenges.value = (data ?? []).map((c) => {
    const total = c.quantity || 0
    const count = c.participants?.length ?? 0
    return {
      id: c.id,
      name: c.title,
      meta: `${count} ${count === 1 ? 'participant' : 'participants'}`,
      current: c.completed ? total : 0,
      total,
      color: c.completed ? 'bg-emerald-500' : 'bg-foreground',
    }
  })
}

// ── Recent achievements ───────────────────────────────────────────────────────

const achievements = ref([])

function isUnlocked(a) {
  if (a.progress != null && a.threshold != null) return a.progress >= a.threshold
  return !!a.completed
}

const badgeCount = computed(() => achievements.value.filter(isUnlocked).length)

const recentAchievements = computed(() =>
  achievements.value
    .filter(isUnlocked)
    .slice(0, 3)
    .map((a) => ({
      title: a.title,
      sub: a.description ?? '',
      xp: a.xp ? `+${a.xp} xp` : '',
    }))
)

async function loadAchievements() {
  achievements.value = (await retrieveAchievements()) ?? []
}

onMounted(async () => {
  await loadGoals()
  await loadAchievements()
  await loadChallenges()
  try { await loadMochi() } catch (e) { /* keep defaults */ }
})
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">

    <!-- ── Header ──────────────────────────────────────────────────────────── -->
    <header class="flex items-center justify-between">
      <div class="flex flex-col gap-0.5">
        <p class="text-xs text-muted-foreground tracking-wide">{{ today }}</p>
        <h1 class="text-xl font-bold text-foreground leading-tight">{{ greeting }}</h1>
      </div>
      <div class="flex items-center gap-3">
        <div class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-full border border-orange-200 bg-orange-50 text-orange-600">
          <Flame class="w-3.5 h-3.5" />
          <span>{{ streak }} day streak</span>
        </div>
      </div>
    </header>

    <!-- ── Stats strip ──────────────────────────────────────────────────────── -->
    <Card class="flex-row items-stretch divide-x divide-border p-0 gap-0">
      <div v-for="stat in stats" :key="stat.label" class="flex flex-col gap-0.5 px-6 py-3 flex-1">
        <span class="text-xs text-muted-foreground">{{ stat.label }}</span>
        <span class="text-lg font-semibold text-foreground tabular-nums">{{ stat.value }}</span>
      </div>
    </Card>

    <!-- ── Page body ────────────────────────────────────────────────────────── -->
    <main class="flex flex-col gap-4">

      <!-- Row 1: Calendar | Goals | Mochi — matches design proportions -->
      <div class="grid grid-cols-[2fr_2.5fr_1fr] gap-4">

        <!-- ── Mini calendar ──────────────────────────────────────────────── -->
        <Card class="p-6 gap-0">
          <!-- Month nav -->
          <div class="flex items-center justify-between mb-5">
            <span class="text-sm font-semibold text-foreground">{{ monthName }}</span>
            <div class="flex items-center gap-1">
              <button @click="prevMonth" class="w-7 h-7 flex items-center justify-center rounded hover:bg-accent transition-colors text-muted-foreground">
                <ChevronLeft class="w-4 h-4" />
              </button>
              <button @click="nextMonth" class="w-7 h-7 flex items-center justify-center rounded hover:bg-accent transition-colors text-muted-foreground">
                <ChevronRight class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Day-of-week headers -->
          <div class="grid grid-cols-7 mb-2">
            <span v-for="(d, i) in ['M','T','W','T','F','S','S']" :key="i"
              class="text-[11px] font-medium text-muted-foreground text-center py-1">
              {{ d }}
            </span>
          </div>

          <!-- Day cells — flex-1 so the grid fills the card height -->
          <div class="grid grid-cols-7 gap-y-1 flex-1 content-start">
            <div
              v-for="(day, i) in calDays"
              :key="i"
              :class="['text-xs text-center py-2 cursor-default select-none transition-colors', dayClass(day)]"
            >
              {{ day ?? '' }}
            </div>
          </div>

          <!-- Legend -->
          <div class="flex items-center gap-4 mt-5 pt-4 border-t border-border">
            <span class="flex items-center gap-1.5 text-[11px] text-muted-foreground">
              <span class="w-2.5 h-2.5 rounded-sm bg-emerald-200 inline-block"></span> Complete
            </span>
            <span class="flex items-center gap-1.5 text-[11px] text-muted-foreground">
              <span class="w-2.5 h-2.5 rounded-sm bg-amber-200 inline-block"></span> Partial
            </span>
            <button class="ml-auto text-[11px] font-medium text-primary hover:text-primary" @click="navigate('/calendar')">
              Open calendar →
            </button>
          </div>
        </Card>

        <!-- ── Today's goals ───────────────────────────────────────────────── -->
        <Card class="p-6 gap-0">
          <div class="flex items-center justify-between mb-4">
            <button class="text-[11px] font-medium uppercase tracking-wider text-muted-foreground hover:text-primary" @click="navigate('/goals')">Today's goals →</button>
            <span class="text-[11px] text-muted-foreground">{{ goalPage }} of {{ totalGoalPages }}</span>
          </div>

          <div class="flex flex-col gap-3 flex-1">
            <p v-if="!goals.length" class="text-xs text-muted-foreground py-4 text-center">
              No goals for today yet.
            </p>
            <div
              v-for="goal in goals"
              :key="goal.id"
              :class="[
                'flex items-center gap-4 px-4 py-4 rounded-lg border transition-colors',
                goal.done ? 'bg-muted border-border' : 'bg-card border-border'
              ]"
            >
              <!-- Checkbox -->
              <button
                @click="goal.done = !goal.done"
                :class="[
                  'w-5 h-5 rounded border-2 flex items-center justify-center shrink-0 transition-colors',
                  goal.done ? 'bg-emerald-500 border-emerald-500' : 'border-border bg-card'
                ]"
              >
                <svg v-if="goal.done" class="w-3 h-3 text-white" viewBox="0 0 12 12" fill="none">
                  <path d="M2 6l3 3 5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>

              <div class="flex-1 min-w-0">
                <p :class="['text-sm font-medium', goal.done ? 'line-through text-muted-foreground' : 'text-foreground']">
                  {{ goal.text }}
                </p>
                <p v-if="goal.time" class="text-xs text-muted-foreground mt-0.5">
                  {{ goal.time }} ·
                  <span :class="goal.type === 'challenge' ? 'text-primary font-medium' : 'text-muted-foreground'">
                    {{ goal.type === 'challenge' ? 'Challenge' : 'Personal' }}
                  </span>
                  · {{ goal.xp }}
                </p>
                <p v-else class="text-xs text-muted-foreground mt-0.5">{{ goal.xp }}</p>
              </div>
            </div>
          </div>

          <!-- Add subgoal -->
          <div v-if="showQuickAdd" class="mt-4 flex gap-2">
            <input
              v-model="quickTitle"
              type="text"
              placeholder="New goal title"
              class="flex-1 h-9 rounded-lg border border-border px-3 text-xs text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              @keyup.enter="submitQuickAdd"
            />
            <button
              :disabled="quickSaving"
              class="px-3 h-9 rounded-lg bg-brand text-white text-xs font-medium hover:bg-brand/90 transition-colors"
              @click="submitQuickAdd"
            >
              {{ quickSaving ? '...' : 'Add' }}
            </button>
          </div>
          <button v-else @click="toggleQuickAdd" class="mt-4 w-full py-3 rounded-lg bg-brand text-white text-xs font-medium hover:bg-brand/90 transition-colors flex items-center justify-center gap-1.5">
            <Plus class="w-3.5 h-3.5" />
            Add subgoal
          </button>
        </Card>

        <!-- ── Your Mochi ──────────────────────────────────────────────────── -->
        <MochiCard :avatar-size="80" />

      </div>

      <!-- Row 2: Active challenges | Recent achievements -->
      <div class="grid grid-cols-2 gap-4">

        <!-- Active challenges -->
        <Card class="p-6 gap-0">
          <button class="text-[11px] font-medium uppercase tracking-wider text-muted-foreground mb-3 hover:text-primary" @click="navigate('/challenges')">Active challenges →</button>
          <p v-if="!challenges.length" class="text-xs text-muted-foreground py-4">
            No active challenges.
          </p>
          <div v-for="(ch, i) in challenges" :key="ch.id">
            <div class="py-3">
              <div class="flex justify-between items-baseline mb-0.5">
                <span class="text-sm font-medium text-foreground">{{ ch.name }}</span>
                <span class="text-xs text-muted-foreground shrink-0 ml-2">{{ ch.current }} / {{ ch.total }}</span>
              </div>
              <p class="text-xs text-muted-foreground mb-2">{{ ch.meta }}</p>
              <div class="w-full h-1.5 rounded-full bg-muted overflow-hidden">
                <div :class="['h-full rounded-full', ch.color]" :style="{ width: (ch.total ? ch.current / ch.total * 100 : 0) + '%' }"></div>
              </div>
            </div>
            <hr v-if="i < challenges.length - 1" class="border-border" />
          </div>
        </Card>

        <!-- Recent achievements -->
        <Card class="p-6 gap-0">
          <button class="text-[11px] font-medium uppercase tracking-wider text-muted-foreground mb-3 hover:text-primary" @click="navigate('/achievements')">Recent achievements →</button>
          <p v-if="!recentAchievements.length" class="text-xs text-muted-foreground py-4">
            No achievements unlocked yet.
          </p>
          <div v-for="(ach, i) in recentAchievements" :key="ach.title">
            <div class="flex items-center gap-3 py-3">
              <div class="w-9 h-9 rounded-lg flex items-center justify-center shrink-0 bg-primary/10">
                <Sparkles class="w-4 h-4 text-primary" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-foreground">{{ ach.title }}</p>
                <p v-if="ach.sub" class="text-xs text-muted-foreground truncate">{{ ach.sub }}</p>
              </div>
              <span class="text-xs font-medium text-primary shrink-0">{{ ach.xp }}</span>
            </div>
            <hr v-if="i < recentAchievements.length - 1" class="border-border" />
          </div>
        </Card>

      </div>
    </main>
  </div>
</template>