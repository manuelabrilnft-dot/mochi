<script setup>
import mochiSrc from '@/assets/pixel_blob_no_eyes.svg'

defineProps({
  color: {
    type: String,
    default: 'Blossom',
  },
  size: {
    type: Number,
    default: 160,
  },
})

const filters = {
  Pearl: 'saturate(0.15) brightness(1.18)',
  Blossom: 'none',
  Sky: 'hue-rotate(200deg) saturate(1.1)',
  Mint: 'hue-rotate(180deg) saturate(0.95)',
  Lavender: 'hue-rotate(270deg) saturate(1.6)',
  Honey: 'hue-rotate(70deg) saturate(1.4) brightness(1.25)',
}
</script>

<template>
  <img
    :src="mochiSrc"
    :width="size"
    :style="{ filter: filters[color] }"
    class="mochi"
    alt="Mochi"
  />
</template>

<style scoped>
.mochi {
  image-rendering: pixelated;
  height: auto;
}
</style>