<script setup>
import Input from "@/components/ui/input/Input.vue";

import Button from "@/components/ui/button/Button.vue";

// Authentication
import { useAuth } from "@/composables/useAuth.js";

// Form validation
import validator from "@/models/validator";
import { useForm } from "@/composables/useForm";

const loginSchema = validator.schema({
  username: validator.string().required("Username missing"),
  password: validator.string().required("Password missing"),
});

const {
  fields,
  errors,
  handleSubmit: handleLoginSubmit,
} = useForm({
  validationSchema: loginSchema,
});

const { isLoading, error, register, login } = useAuth();

const onLogin = handleLoginSubmit(async (values) => {
  console.log("Log in");
  await login(values.username, values.password);
});
</script>

<template>
  <form @submit="onLogin" class="flex flex-col gap-3 h-full">
    <!-- Username Field -->
    <div class="flex gap-1 flex-col">
      <label class="text-xs text-muted-foreground">Username</label>
      <Input
        v-model="fields.username"
        class="sm"
        placeholder="Insert your username"
        :class="
          errors.username
            ? 'border-2 border-destructive focus-visible:border-destructive focus-visible:ring-destructive/30'
            : ''
        "
      />
      <p class="text-xs text-destructive">{{ errors.username }}</p>
    </div>

    <!-- Password field -->
    <div class="flex gap-1 flex-col">
      <label class="text-xs text-muted-foreground">Password</label>
      <Input
        v-model="fields.password"
        type="password"
        placeholder="Insert your password"
        :class="
          errors.password
            ? 'border-2 border-destructive focus-visible:border-destructive focus-visible:ring-destructive/30'
            : ''
        "
      />
      <p class="text-xs text-destructive">{{ errors.password }}</p>
    </div>
    <Button type="submit" size="lg" class="text-md font-extrabold mt-auto">
      Log in
    </Button>
  </form>
</template>
