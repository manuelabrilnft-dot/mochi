<script setup>
import { ref, computed } from 'vue'
import { Lock, Pencil } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import MochiColours from '@/models/MochiColours.vue'
import MochiFace from '@/models/MochiFace.vue'
import MochiCostum from '@/models/MochiCostum.vue'
import MochiScene from '@/models/MochiScene.vue'
import { useMochi, mochiFace, evolutionStages as sharedStages, evolutionStageIndex, evolutionPercent, nextStage, levelsToNextStage, currentStage as sharedCurrentStage } from '@/composables/useMochi'
import { onMounted } from 'vue'
import MochiAvatar from '@/models/MochiAvatar.vue'

const { mochi, gameProps, load, save, isLoading, error } = useMochi()

onMounted(() => load({ force: true }))
const editingName = ref(false)

// persists the mochi look (name + color + costume) via PUT /api/mochi.
// face/scene/background aren't stored by the backend yet, so they stay local.
const justSaved = ref(false)
async function handleSave() {
  justSaved.value = false
  await save()
  if (!error.value) {
    justSaved.value = true
    setTimeout(() => (justSaved.value = false), 2000)
  }
}


const bodyColors = [
  { id: 'Blossom',  unlockLevel: 1,  swatch: '#f9a8d4', ring: '#f472b6' },
  { id: 'Pearl',    unlockLevel: 3,  swatch: '#f5f5f0', ring: '#d4d4d4' },
  { id: 'Sky',      unlockLevel: 5,  swatch: '#bfdbfe', ring: '#93c5fd' },
  { id: 'Mint',     unlockLevel: 8,  swatch: '#6ee7b7', ring: '#34d399' },
  { id: 'Lavender', unlockLevel: 12, swatch: '#c4b5fd', ring: '#a78bfa' },
  { id: 'Honey',    unlockLevel: 16, swatch: '#fde68a', ring: '#fbbf24' },
]

// colors unlock by level
const isUnlocked = (color) => gameProps.value.level >= color.unlockLevel

const unlockedCount = computed(
  () => bodyColors.filter(isUnlocked).length
)

const costumes = [
  { id: 'none',    label: 'None',          unlockLevel: 1 },
  { id: 'bowtie',  label: 'Bow Tie',       unlockLevel: 1 },
  { id: 'balloon', label: 'Balloon',       unlockLevel: 3 },
  { id: 'buddy',   label: 'Mini Friend',   unlockLevel: 5 },
  { id: 'heart',   label: 'Heart Balloon', unlockLevel: 7 },
]

// costumes unlock by level
const costumeUnlocked = (costume) => gameProps.value.level >= costume.unlockLevel
const costumesUnlocked = computed(() => costumes.filter(costumeUnlocked).length)

const scenes = [
  { id: 'none',   label: 'None',   unlockXp: 0   },
  { id: 'sunset', label: 'Sunset', unlockXp: 0   },
  { id: 'night',  label: 'Night',  unlockXp: 50  },
  { id: 'garden', label: 'Garden', unlockXp: 150 },
  { id: 'city',   label: 'City',   unlockXp: 300 },
  { id: 'galaxy', label: 'Galaxy', unlockXp: 450 },
  { id: 'beach',  label: 'Beach',  unlockXp: 600 },
  { id: 'candy',  label: 'Candy',  unlockXp: 750 },
  { id: 'forest', label: 'Forest', unlockXp: 900 },
]

// scenes unlock with xp, same idea as faces
const sceneUnlocked = (scene) => gameProps.value.xp >= scene.unlockXp
const scenesUnlocked = computed(() => scenes.filter(sceneUnlocked).length)

const faces = [
  { id: 'default',     label: 'Default',     unlockXp: 0   },
  { id: 'happy',       label: 'Happy',       unlockXp: 0   },
  { id: 'wink',        label: 'Wink',        unlockXp: 100 },
  { id: 'sleepy',      label: 'Sleepy',      unlockXp: 250 },
  { id: 'cool',        label: 'Cool',        unlockXp: 400 },
  { id: 'starry-eyed', label: 'Starry-eyed', unlockXp: 600 },
]

const faceUnlocked = (face) => gameProps.value.xp >= face.unlockXp

const facesUnlocked = computed(
  () => faces.filter(faceUnlocked).length
)

// build the evolution path display from the shared level-based stage index
// so this page, home, and profile always agree
const evolutionStages = computed(() =>
  sharedStages.map((stage, i) => {
    const idx = evolutionStageIndex.value
    const done = i < idx
    const current = i === idx
    const locked = i > idx
    let sublabel = ""
    if (done) sublabel = `Lvl ${stage.level}`
    else if (current) sublabel = "Current"
    else {
      const lvlLeft = stage.level - (gameProps.value.level ?? 1)
      sublabel = `Lvl ${stage.level} · ${lvlLeft} to go`
    }
    return { ...stage, done, current, locked, sublabel }
  })
)

// the current stage id - drives the sprite size
const currentStage = computed(() => sharedCurrentStage.value.id)

const stats = [
  { label: 'Bond level',     value: '7',      sub: null,            bar: true, progress: 58 },
  { label: 'Days together',  value: '47',     sub: 'Since Mar 27',  bar: false },
  { label: 'Goals together', value: '7',      sub: 'All time',      bar: false },
  { label: 'Costumes',       value: '4 / 12', sub: 'Unlock more →', bar: false, link: true },
]
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">

    <!-- Header -->
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold text-foreground">My Mochi</h1>
        <p class="text-sm text-muted-foreground mt-0.5">Customize and watch them grow</p>
      </div>
      <div class="flex items-center gap-2">
        <span v-if="justSaved" class="text-xs font-medium text-emerald-600">Saved ✓</span>
        <span v-else-if="error" class="text-xs font-medium text-destructive">Save failed</span>
        <Button size="sm" class="gap-1.5" :disabled="isLoading" @click="handleSave">
          {{ isLoading ? "Saving…" : "Save changes" }}
        </Button>
      </div>
    </div>

    <!-- Evolution path -->
    <Card>
      <CardHeader class="pb-2">
        <div class="flex justify-between items-center">
          <CardTitle class="text-sm font-semibold">Evolution path</CardTitle>
          <Badge variant="outline" class="text-xs">Currently stage {{ evolutionStageIndex + 1 }} of {{ evolutionStages.length }}</Badge>
        </div>
      </CardHeader>
      <div class="w-full h-px bg-muted" />
      <CardContent class="pt-2">
        <div class="flex items-start">
          <div
            v-for="(stage, i) in evolutionStages"
            :key="stage.id"
            class="flex flex-col items-center flex-1 relative gap-2"
          >
            <div
              v-if="i > 0"
              class="absolute top-5 right-1/2 w-full h-0.5 z-0"
              :class="evolutionStages[i - 1].done ? 'bg-primary' : 'bg-muted'"
            />
            <div
              class="w-10 h-10 rounded-full border-2 flex items-center justify-center z-10 text-xs"
              :class="[
                stage.done    ? 'bg-card border-primary text-primary text-sm' :
                stage.current ? 'bg-card border-primary text-primary' :
                'bg-muted border-border text-muted-foreground opacity-60'
              ]"
              :title="stage.sublabel"
            >
              <span v-if="stage.done">✓</span>
              <span v-else-if="stage.current">◡</span>
              <Lock v-else class="size-3" />
            </div>
            <p class="text-xs font-medium" :class="stage.current ? 'text-primary font-semibold' : 'text-foreground'">
              {{ stage.label }}
            </p>
            <p class="text-[11px] text-muted-foreground text-center">{{ stage.sublabel }}</p>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Main grid -->
    <div class="grid grid-cols-[1fr_1.6fr] gap-4">

      <!-- Mochi preview -->
      <Card class="bg-[#e8eef8] border-0 shadow-none">
        <CardContent class="p-6 flex flex-col items-center gap-2 relative">
          <!-- full-bleed scene background -->
          <div class="absolute inset-x-0 top-0 h-72 overflow-hidden">
            <MochiScene v-if="mochi.scene !== 'none'" :scene="mochi.scene" class="w-full h-full" />
          </div>

          <Badge variant="outline" class="absolute top-4 left-4 text-xs bg-white/70 z-10">
            Stage {{ evolutionStageIndex + 1 }} · {{ sharedCurrentStage.label }}
          </Badge>

         <!-- mochi sits on the scene -->
          <div class="relative z-10" style="margin-top: 120px;">
            <MochiAvatar
              :color="mochi.color"
              :face="mochiFace"
              :costume="mochi.costume"
              scene="none"
              :stage="currentStage"
              :size="150"
            />
          </div>

          <!-- Editable name -->
          <div class="flex items-center gap-1.5">
            <template v-if="editingName">
              <Input
                v-model="mochi.name"
                class="h-7 text-sm text-center w-28 text-primary font-semibold"
                @blur="editingName = false"
                @keyup.enter="editingName = false"
                autofocus
              />
            </template>
            <template v-else>
              <p class="text-base font-semibold text-primary">{{ mochi.name }}</p>
              <Button variant="ghost" size="icon" class="size-5" @click="editingName = true">
                <Pencil class="size-3 text-muted-foreground" />
              </Button>
            </template>
          </div>

          <p class="text-xs text-muted-foreground">Happy · Well rested</p>

          <div class="w-full h-px bg-primary/10 my-2" />

          <div class="w-full">
            <div class="flex justify-between text-xs text-muted-foreground mb-1.5">
              <span>Next evolution</span>
              <span v-if="nextStage">{{ levelsToNextStage }} lvl to {{ nextStage.label }}</span>
              <span v-else>Maxed out</span>
            </div>
            <div class="w-full h-1.5 bg-muted rounded-full overflow-hidden">
              <div class="h-full bg-primary rounded-full" :style="{ width: evolutionPercent + '%' }" />
            </div>
          </div>

          <div class="w-full h-px bg-primary/10 my-2" />

          <!-- Face selection is disabled for now: the backend doesn't store
               the mochi face, so changes here wouldn't be persisted.
          <div class="w-full flex flex-col gap-2">
            <div class="flex justify-between items-center">
              <p class="text-xs text-muted-foreground">Face</p>
              <Badge variant="secondary" class="text-xs">
                {{ facesUnlocked }} of {{ faces.length }} unlocked
              </Badge>
            </div>
            <div class="grid grid-cols-3 gap-2">
              <button
                v-for="face in faces"
                :key="face.id"
                class="aspect-square rounded-xl border-2 flex items-center justify-center transition-colors overflow-hidden bg-white/40"
                :class="[
                  !faceUnlocked(face)
                    ? 'opacity-60 cursor-not-allowed border-border'
                    : mochi.face === face.id
                      ? 'border-primary bg-primary/10'
                      : 'border-border hover:border-muted-foreground cursor-pointer'
                ]"
                :disabled="!faceUnlocked(face)"
                :title="faceUnlocked(face) ? face.label : `${face.label} · Unlock at level ${face.unlockXp}`"
                @click="faceUnlocked(face) && (mochi.face = face.id)"
              >
                <MochiFace :face="face.id" :size="72" :class="faceUnlocked(face) ? '' : 'opacity-40'" />
                <Lock
                  v-if="!faceUnlocked(face)"
                  class="size-4 text-muted-foreground absolute top-1 right-1"
                />
              </button>
            </div>
          </div>
          -->
        </CardContent>
      </Card>

      <!-- Customize panel -->
      <Card>
        <CardHeader class="pb-2">
          <CardTitle class="text-base">Customize</CardTitle>
        </CardHeader>
        <CardContent class="flex flex-col gap-5">

          <!-- Body color -->
          <div class="flex flex-col gap-2">
            <div class="flex justify-between items-center">
              <p class="text-xs text-muted-foreground">Body color</p>
              <Badge variant="secondary" class="text-xs">
                {{ unlockedCount }} of {{ bodyColors.length }} unlocked
              </Badge>
            </div>
            <div class="flex gap-2">
              <button
                v-for="color in bodyColors"
                :key="color.id"
                class="relative w-9 h-9 rounded-full border-2 transition-transform"
                :class="isUnlocked(color)
                  ? 'hover:scale-110 cursor-pointer'
                  : 'opacity-50 cursor-not-allowed'"
                :style="{
                  background: color.swatch,
                  borderColor: mochi.color === color.id ? color.ring : '#e5e7eb',
                  transform: mochi.color === color.id ? 'scale(1.15)' : undefined
                }"
                :title="isUnlocked(color) ? color.id : `${color.id} · Unlock at level ${color.unlockLevel}`"
                :disabled="!isUnlocked(color)"
                @click="isUnlocked(color) && (mochi.color = color.id)"
              >
                <Lock
                  v-if="!isUnlocked(color)"
                  class="size-3.5 text-muted-foreground absolute inset-0 m-auto"
                />
              </button>
            </div>
          </div>

          <!-- Costume -->
          <div class="flex flex-col gap-2">
            <div class="flex justify-between items-center">
              <p class="text-xs text-muted-foreground">Costume</p>
              <Badge variant="secondary" class="text-xs">{{ costumesUnlocked - 1 }} of {{ costumes.length - 1 }} unlocked</Badge>
            </div>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="costume in costumes"
                :key="costume.id"
                class="relative aspect-square rounded-xl border-2 flex items-center justify-center text-xl transition-colors"
                :class="[
                  !costumeUnlocked(costume)
                    ? 'bg-muted cursor-not-allowed border-border'
                    : mochi.costume === costume.id
                      ? 'border-primary bg-primary/10'
                      : 'border-border bg-muted hover:border-muted-foreground cursor-pointer'
                ]"
                :disabled="!costumeUnlocked(costume)"
                :title="costumeUnlocked(costume) ? costume.label : `${costume.label} · Unlock at level ${costume.unlockLevel}`"
                @click="costumeUnlocked(costume) && (mochi.costume = costume.id)"
              >
                <div v-if="costume.id !== 'none'" class="relative" style="width: 56px; height: 56px;" :class="costumeUnlocked(costume) ? '' : 'opacity-40'">
                  <MochiColours :color="mochi.color" :size="56" class="absolute inset-0 m-auto" />
                  <MochiCostum :costume="costume.id" :size="56" class="absolute inset-0 m-auto" />
                </div>
                <span v-else class="text-sm text-muted-foreground">✕</span>
                <Lock
                  v-if="!costumeUnlocked(costume)"
                  class="size-4 text-foreground absolute top-1 right-1"
                />
              </button>
            </div>
          </div>

          <!-- Background selection is disabled for now: the backend doesn't
               store the scene/background, so changes here wouldn't be persisted.
          <div class="flex flex-col gap-2">
            <div class="flex justify-between items-center">
              <p class="text-xs text-muted-foreground">Background</p>
              <Badge variant="secondary" class="text-xs">{{ scenesUnlocked - 1 }} of {{ scenes.length - 1 }} unlocked</Badge>
            </div>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="scene in scenes"
                :key="scene.id"
                class="relative h-14 rounded-xl border-2 overflow-hidden flex items-center justify-center transition-colors"
                :class="[
                  mochi.scene === scene.id ? 'border-primary' : 'border-border hover:border-muted-foreground',
                  sceneUnlocked(scene) ? 'cursor-pointer' : 'cursor-not-allowed'
                ]"
                :title="sceneUnlocked(scene) ? scene.label : `${scene.label} · Unlock at level ${scene.unlockXp}`"
                :disabled="!sceneUnlocked(scene)"
                @click="sceneUnlocked(scene) && (mochi.scene = scene.id)"
              >
                <MochiScene
                  v-if="scene.id !== 'none'"
                  :scene="scene.id"
                  class="w-full h-full"
                  :class="sceneUnlocked(scene) ? '' : 'opacity-40'"
                />
                <span v-else class="text-sm text-muted-foreground">✕</span>
                <Lock
                  v-if="!sceneUnlocked(scene)"
                  class="size-4 text-foreground absolute top-1 right-1"
                />
              </button>
            </div>
          </div>
          -->

        </CardContent>
      </Card>
    </div>

  </div>
</template>