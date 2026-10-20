import { ref, computed } from "vue";
import { saveMochiService } from "@/services/mochi";
import { user, loadUser } from "@/composables/useUser";

// The backend stores color/costume as integer indices (and has no face/scene
// columns). These ordered lists map those indices to the string ids the sprite
// components use — keep them in sync with the option lists in MyMochi.vue.
export const COLOR_IDS = [
  "Blossom",
  "Pearl",
  "Sky",
  "Mint",
  "Lavender",
  "Honey",
];
export const COSTUME_IDS = ["none", "bowtie", "balloon", "buddy", "heart"];

const colorIdFromIndex = (i) => COLOR_IDS[i] ?? "Blossom";
const costumeIdFromIndex = (i) => COSTUME_IDS[i] ?? "none";
const indexFromColorId = (id) => Math.max(0, COLOR_IDS.indexOf(id));
const indexFromCostumeId = (id) => Math.max(0, COSTUME_IDS.indexOf(id));

// maps the mochi's mood_coefficient (from the profile) to a mood label.
// Single source of truth — used by the profile, home/profile mochi cards, etc.
export function moodFromCoefficient(c) {
  if (c == null) return "Happy";
  if (c > 1.5) return "Tired";
  if (c < 0.5) return "Lazy";
  return "Happy";
}

// each mood maps to one of the three mochi faces
export function faceFromMood(mood) {
  if (mood === "Tired") return "sleepy";
  if (mood === "Lazy") return "default";
  return "happy"; // Happy
}

// the displayed mochi face is driven by its mood (not user-customizable)
export const mochiFace = computed(() =>
  faceFromMood(moodFromCoefficient(user.value?.mochi?.moodCoefficient)),
);

// shared mochi state one source of truth across every page
export const mochi = ref({
  name: "Mochi",
  color: "Blossom",
  face: "default",
  costume: "none",
  scene: "sunset",
});

// game properties drive the unlocks (streak unlocks colors, xp unlocks faces)
export const gameProps = ref({
  level: 1,
  streak: 0,
  xp: 0,
});

// level-based evolution - each stage unlocks at a set level
export const evolutionStages = [
  { id: "egg", label: "Egg", level: 1 },
  { id: "sprout", label: "Sprout", level: 3 },
  { id: "bloom", label: "Bloom", level: 18 },
  { id: "radiant", label: "Radiant", level: 25 },
];

// highest stage whose level requirement we have reached
export const evolutionStageIndex = computed(() => {
  const level = gameProps.value.level ?? 1;
  let idx = 0;
  for (let i = 0; i < evolutionStages.length; i++) {
    if (level >= evolutionStages[i].level) idx = i;
  }
  return idx;
});

// the current stage object - drives stage labels everywhere
export const currentStage = computed(
  () => evolutionStages[evolutionStageIndex.value],
);

// the next stage we are working toward, or null if maxed out
export const nextStage = computed(() => {
  const i = evolutionStageIndex.value;
  return i < evolutionStages.length - 1 ? evolutionStages[i + 1] : null;
});

// progress from this stage's level toward the next stage's level
export const evolutionPercent = computed(() => {
  const level = gameProps.value.level ?? 1;
  const current = currentStage.value;
  const next = nextStage.value;
  if (!next) return 100;
  const span = next.level - current.level;
  if (span <= 0) return 100;
  const done = level - current.level;
  return Math.min(100, Math.max(0, Math.round((done / span) * 100)));
});

// how many levels until the next stage unlocks
export const levelsToNextStage = computed(() => {
  const level = gameProps.value.level ?? 1;
  const next = nextStage.value;
  if (!next) return 0;
  return Math.max(0, next.level - level);
});

// tracks whether the shared mochi/game state has been fetched at least once,
// so multiple components can call load() without triggering duplicate requests
let isLoaded = false;

export function useMochi() {
  const isLoading = ref(false);
  const error = ref(null);

  // The mochi look (color/costume/name) and game properties (level/streak/xp)
  // both come from the /profile endpoint — there is no GET /mochi or
  // /game-properties on the backend. face/scene aren't stored, so we leave the
  // current values in place.
  async function load({ force = false } = {}) {
    if (isLoaded && !force) return;
    isLoading.value = true;
    try {
      const profile = await loadUser({ force });
      if (profile) {
        gameProps.value = {
          level: profile.level ?? gameProps.value.level,
          streak: profile.streak ?? gameProps.value.streak,
          xp: profile.xp ?? gameProps.value.xp,
        };
        const m = profile.mochi;
        if (m) {
          mochi.value = {
            ...mochi.value,
            name: m.name || mochi.value.name,
            color: colorIdFromIndex(m.color),
            costume: costumeIdFromIndex(m.costume),
          };
        }
        console.log(m);
        isLoaded = true;
      }
    } catch (e) {
      error.value = e.message;
    } finally {
      isLoading.value = false;
    }
  }

  async function save() {
    isLoading.value = true;
    error.value = null;
    try {
      // map the sprite string ids back to the integer indices the backend stores
      await saveMochiService({
        name: mochi.value.name,
        color: indexFromColorId(mochi.value.color),
        costume: indexFromCostumeId(mochi.value.costume),
      });
      // keep the shared profile in sync so other pages reflect the change
      if (user.value?.mochi) {
        user.value.mochi.name = mochi.value.name;
        user.value.mochi.color = indexFromColorId(mochi.value.color);
        user.value.mochi.costume = indexFromCostumeId(mochi.value.costume);
      }
    } catch (e) {
      error.value = e.message;
    } finally {
      isLoading.value = false;
    }
  }

  return { mochi, gameProps, isLoading, error, load, save };
}
