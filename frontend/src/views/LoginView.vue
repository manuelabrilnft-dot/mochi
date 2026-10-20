<script setup lang="js">
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { ref } from "vue";
import { Target, Flame, Users } from "@lucide/vue";

import Button from "@/components/ui/button/Button.vue";
import Input from "@/components/ui/input/Input.vue";

import SignupForm from "@/components/auth/SignupForm.vue";
import LoginForm from "@/components/auth/LoginForm.vue";

import { useForm } from "vee-validate";
import { toTypedSchema } from "@vee-validate/zod";
import * as z from "zod";
import { useAuth } from "@/composables/useAuth.js";

const tabs = [
  { id: "signup", label: "Sign up" },
  { id: "login", label: "Log in" },
];
const activeTab = ref("signup");
</script>

<template>
  <div class="flex items-center justify-center h-screen">
    <div class="flex items-stretch gap-6 min-w-xl h-132">
      <Card class="w-md p-8 bg-primary/10 text-primary">
        <div class="flex flex-col text-center gap-2">
          <p class="text-lg font-semibold">Build habits with your Mochi</p>
          <p class="text-xs">
            Set goals, complete daily challenges, and watch your companion grow
            alongside you.
          </p>
        </div>
        <div>
          <div class="flex flex-col gap-2">
            <span class="flex flex-row gap-2">
              <Target size="22px" class="bg-card p-1 rounded-sm" />
              <p class="text-xs translate-y-1">
                Track personal goals and challenges
              </p>
            </span>
            <span class="flex flex-row gap-2">
              <Flame size="22px" class="bg-card p-1 rounded-sm" />
              <p class="text-xs translate-y-1">Earn XP and build streaks</p>
            </span>
            <span class="flex flex-row gap-2">
              <Users size="22px" class="bg-card p-1 rounded-sm" />
              <p class="text-xs translate-y-1">Compete with friends</p>
            </span>
          </div>
        </div>
      </Card>
      <Card class="w-md p-8 flex flex-col">
        <div class="flex flex-col flex-1">
          <div class="flex gap-1 bg-card border border-border rounded-lg p-1 w-full">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              type="button"
              class="flex-1 px-4 py-1.5 rounded-md text-sm transition-colors"
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
          <div class="mt-4 flex-1 flex flex-col">
            <!-- Sign up Page -->
            <div v-if="activeTab === 'signup'" class="flex-1 flex flex-col">
              <div class="flex flex-col gap-1">
                <CardTitle>Create your account</CardTitle>
                <CardDescription class="text-xs">
                  Get started. Your mochi is waiting.
                </CardDescription>
              </div>
              <div class="mt-5 flex-1">
                <SignupForm class="h-full" />
              </div>
            </div>

            <!-- Log in Page -->
            <div v-else class="flex-1 flex flex-col">
              <div class="flex flex-col gap-1">
                <CardTitle>Welcome back</CardTitle>
                <CardDescription class="text-xs">
                  Log in. Your mochi is missing you.
                </CardDescription>
              </div>
              <div class="mt-5 flex-1">
                <LoginForm class="h-full" />
              </div>
            </div>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>
