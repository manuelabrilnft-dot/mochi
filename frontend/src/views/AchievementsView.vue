<script setup>
import { ref, computed, onMounted } from 'vue'
import { Zap, Flame, Target, Users, Sparkles, Trophy, Calendar, Star, HelpCircle, Check, Footprints } from 'lucide-vue-next'
import { Card, CardContent } from '@/components/ui/card'
import useAchievements from '@/composables/useAchievements'

const ICON_MAP = {
  activity: Footprints,
  streak:   Flame,
  goal:     Target,
  social:   Users,
  mochi:    Sparkles,
  trophy:   Trophy,
  calendar: Calendar,
  star:     Star,
}
function resolveIcon(type) {
  return ICON_MAP[type] ?? HelpCircle
}

function typeLabel(type) {
  if (!type) return 'Other'
  return type
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

const { isLoading, retrieve } = useAchievements()

const achievements = ref([])

async function loadAchievements() {
  achievements.value = await retrieve()
}

onMounted(loadAchievements)

function isUnlocked(a) {
  if (a.progress != null && a.threshold != null) {
    return a.progress >= a.threshold
  }
  return !!a.completed
}

// Group achievements by type, each group split into unlocked / in progress / locked
const groupedByType = computed(() => {
  const groups = {}
  for (const a of achievements.value) {
    const key = a.type ?? 'other'
    if (!groups[key]) groups[key] = []
    groups[key].push(a)
  }
  return groups
})

const totalUnlocked = computed(() => achievements.value.filter(isUnlocked).length)
const totalXp = computed(() =>
  achievements.value.filter(isUnlocked).reduce((s, a) => s + (a.xp ?? 0), 0)
)
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">

    <!-- Header -->
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold text-foreground">Achievements</h1>
        <p class="text-sm text-muted-foreground mt-0.5">{{ totalUnlocked }} of {{ achievements.length }} unlocked</p>
      </div>
      <div class="flex items-center gap-1.5 px-3 py-1.5 bg-card border border-border rounded-full text-sm">
        <Zap class="size-4 text-primary" />
        <span class="text-foreground font-medium">{{ totalXp.toLocaleString() }} xp from badges</span>
      </div>
    </div>

    <!-- Overall progress -->
    <Card>
      <CardContent class="p-5 flex flex-col gap-2">
        <div class="flex justify-between text-xs text-muted-foreground">
          <span>Total unlocked</span>
          <span class="font-medium text-foreground">{{ totalUnlocked }} / {{ achievements.length }}</span>
        </div>
        <div class="w-full h-2 bg-muted rounded-full overflow-hidden">
          <div
            class="h-full bg-primary rounded-full transition-all"
            :style="`width: ${achievements.length ? Math.round((totalUnlocked / achievements.length) * 100) : 0}%`"
          />
        </div>
      </CardContent>
    </Card>

    <!-- Loading skeleton -->
    <div v-if="isLoading" class="flex flex-col gap-3">
      <Card v-for="i in 2" :key="i">
        <CardContent class="p-5">
          <div class="h-16 bg-muted rounded-lg animate-pulse" />
        </CardContent>
      </Card>
    </div>

    <!-- Grouped by type -->
    <template v-else>
      <div v-for="(items, type) in groupedByType" :key="type" class="flex flex-col gap-3">
        <p class="text-sm font-semibold text-foreground">{{ typeLabel(type) }}</p>

        <div class="grid grid-cols-3 gap-4">
          <Card
            v-for="a in items" :key="a.id"
            class="hover:shadow-md transition-shadow"
            :class="!isUnlocked(a) ? 'opacity-50' : ''"
          >
            <CardContent class="p-5 flex flex-col items-center gap-3 text-center">
              <div
                class="size-12 rounded-full flex items-center justify-center"
                :class="isUnlocked(a) ? 'bg-primary/10' : 'bg-muted'"
              >
                <component :is="resolveIcon(a.type)" class="size-5" :class="isUnlocked(a) ? 'text-primary' : 'text-muted-foreground'" />
              </div>

              <div>
                <p class="text-sm font-semibold text-foreground">{{ a.title }}</p>
                <p class="text-xs text-muted-foreground mt-0.5">{{ a.description }}</p>
              </div>

              <!-- Unlocked -->
              <div
                v-if="isUnlocked(a)"
                class="flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-muted text-muted-foreground"
              >
                <Zap class="size-3" />
                +{{ a.xp }} xp
              </div>

              <!-- In progress / locked -->
              <div v-else class="w-full flex flex-col gap-1">
                <div v-if="a.threshold" class="w-full h-1.5 bg-muted rounded-full overflow-hidden">
                  <div
                    class="h-full bg-primary rounded-full transition-all"
                    :style="`width: ${Math.round(((a.progress ?? 0) / a.threshold) * 100)}%`"
                  />
                </div>
                <div class="flex items-center justify-between text-xs text-muted-foreground">
                  <span v-if="a.threshold">{{ a.progress ?? 0 }} / {{ a.threshold }}</span>
                  <span v-else>Locked</span>
                  <span>+{{ a.xp }} xp</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <!-- Empty state -->
      <Card v-if="!achievements.length">
        <CardContent class="p-8 text-center text-muted-foreground text-sm">
          No achievements yet — start completing goals and challenges to unlock some!
        </CardContent>
      </Card>
    </template>

  </div>
</template>