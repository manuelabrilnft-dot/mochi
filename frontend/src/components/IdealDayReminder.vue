<script setup>
import { reminder, dismissReminder } from "@/composables/useIdealDay";
import { Button } from "@/components/ui/button";
import pixelBlob from "@/assets/pixel_blob_transparent.svg";
</script>

<template>
  <div
    v-if="reminder"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
    @click.self="dismissReminder"
  >
    <div class="w-[340px] rounded-2xl bg-card p-6 shadow-xl flex flex-col items-center gap-3">
      <img :src="pixelBlob" alt="Mochi" class="size-20" style="image-rendering: pixelated" />
      <p class="text-xs font-semibold tracking-wide text-pink-500 uppercase">From your ideal day</p>
      <p class="text-center text-base text-foreground">
        <span class="font-bold">{{ reminder.name }}</span> starts in 10 minutes,
        at {{ reminder.startLabel }}.
      </p>
      <div class="flex gap-2 w-full mt-1">
        <Button class="flex-1 bg-brand hover:bg-brand/90 text-brand-foreground" @click="dismissReminder">
          Got it
        </Button>
      </div>
    </div>
  </div>
</template>