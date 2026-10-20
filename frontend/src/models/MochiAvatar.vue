<script setup>
// the whole mochi in one tag - scene + body + face + costume
import { computed } from 'vue'
import MochiScene from '@/models/MochiScene.vue'
import MochiColours from '@/models/MochiColours.vue'
import MochiFace from '@/models/MochiFace.vue'
import MochiCostum from '@/models/MochiCostum.vue'

const props = defineProps({
  color:   { type: String, default: 'Blossom' },
  face:    { type: [String, Number], default: 'default' },
  costume: { type: [String, Number], default: 'none' },
  scene:   { type: [String, Number], default: 'none' },
  stage:   { type: [String, Number], default: 'sprout' },
  size:    { type: Number, default: 180 },
})

// each evolution stage scales the mochi a bit - it grows as it evolves
const stageScale = {
  egg:     0.62,
  sprout:  0.82,
  bloom:   1,
  radiant: 1.12,
}

const scale = computed(() => {
  const byKey = stageScale[props.stage]
  if (byKey) return byKey
  const order = [0.62, 0.82, 1, 1.12]
  return order[props.stage] ?? 0.82
})

const isRadiant = computed(() => props.stage === 'radiant' || props.stage === 3)
</script>

<template>
  <div class="mochi-avatar" :style="{ width: size + 'px', height: size + 'px' }">
    <div
      v-if="scene !== 'none' && scene !== 0"
      class="scene-layer"
    >
      <MochiScene :scene="scene" />
    </div>

    <div v-if="isRadiant" class="glow" />

    <div class="stage-wrap" :style="{ transform: `scale(${scale})` }">
      <MochiColours :color="color" :size="size" class="sprite" />
      <MochiFace :face="face" :size="size" class="sprite" />
      <MochiCostum :costume="costume" :size="size" class="sprite" />
    </div>
  </div>
</template>

<style scoped>
.mochi-avatar {
  position: relative;
  display: block;
}
.scene-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border-radius: 16px;
  overflow: hidden;
  z-index: 0;
}
.scene-layer :deep(svg) {
  width: 100%;
  height: 100%;
  display: block;
}
.glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 80%;
  height: 72%;
  transform: translate(-50%, -50%);
  background: radial-gradient(ellipse, rgba(255, 220, 130, 0.55), transparent 70%);
  z-index: 0;
}
.stage-wrap {
  position: absolute;
  inset: 0;
  z-index: 1;
}
.sprite {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>