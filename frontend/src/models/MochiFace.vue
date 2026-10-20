<script setup>
import { computed } from 'vue'

const props = defineProps({
  face: { type: [String, Number], default: 'default' },
  size: { type: Number, default: 160 },
})

// three faces, one per mood: default (Lazy), happy (Happy), sleepy (Tired)
const faces = [
  { id: 0, key: 'default', label: 'Default' },
  { id: 1, key: 'happy', label: 'Happy' },
  { id: 2, key: 'sleepy', label: 'Sleepy' },
]

const activeKey = computed(() => {
  const match = faces.find(f => f.key === props.face || f.id === props.face)
  return match ? match.key : 'default'
})
</script>

<template>
  <svg :width="size" :height="size" viewBox="0 0 100 100" class="mochi-face" shape-rendering="crispEdges" role="img" :aria-label="`Mochi face: ${activeKey}`">
    <g v-if="activeKey === 'default'"><rect x="32" y="44" width="8" height="8" fill="#2b2024" opacity="1"/><rect x="32" y="44" width="4" height="4" fill="#fff" opacity="1"/><rect x="60" y="44" width="8" height="8" fill="#2b2024" opacity="1"/><rect x="60" y="44" width="4" height="4" fill="#fff" opacity="1"/><rect x="40" y="64" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="44" y="68" width="12" height="4" fill="#2b2024" opacity="1"/><rect x="56" y="64" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="24" y="56" width="8" height="4" fill="#ff9bb6" opacity="0.7"/><rect x="64" y="56" width="8" height="4" fill="#ff9bb6" opacity="0.7"/></g>
    <g v-else-if="activeKey === 'happy'"><rect x="32" y="44" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="36" y="40" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="40" y="44" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="60" y="44" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="64" y="40" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="68" y="44" width="4" height="4" fill="#2b2024" opacity="1"/><rect x="40" y="60" width="20" height="4" fill="#2b2024" opacity="1"/><rect x="44" y="64" width="12" height="4" fill="#2b2024" opacity="1"/><rect x="44" y="68" width="12" height="4" fill="#ff7d9c" opacity="1"/><rect x="24" y="52" width="8" height="4" fill="#ff9bb6" opacity="0.75"/><rect x="68" y="52" width="8" height="4" fill="#ff9bb6" opacity="0.75"/></g>
    <g v-else-if="activeKey === 'sleepy'"><rect x="28" y="44" width="16" height="4" fill="#2b2024" opacity="1"/><rect x="32" y="48" width="8" height="4" fill="#2b2024" opacity="1"/><rect x="56" y="44" width="16" height="4" fill="#2b2024" opacity="1"/><rect x="60" y="48" width="8" height="4" fill="#2b2024" opacity="1"/><rect x="44" y="60" width="8" height="8" fill="#2b2024" opacity="1"/><rect x="72" y="32" width="8" height="4" fill="#6aa9ff" opacity="1"/><rect x="76" y="36" width="4" height="4" fill="#6aa9ff" opacity="1"/><rect x="72" y="40" width="8" height="4" fill="#6aa9ff" opacity="1"/><rect x="80" y="20" width="8" height="4" fill="#6aa9ff" opacity="1"/><rect x="84" y="24" width="4" height="4" fill="#6aa9ff" opacity="1"/><rect x="80" y="28" width="8" height="4" fill="#6aa9ff" opacity="1"/></g>
  </svg>
</template>

<style scoped>
.mochi-face { display: block; pointer-events: none; }
</style>