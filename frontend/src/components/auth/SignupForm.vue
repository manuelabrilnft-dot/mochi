<script setup>
import Input from "@/components/ui/input/Input.vue";

import Button from "@/components/ui/button/Button.vue";

import { useAuth } from "@/composables/useAuth.js";
import validator from "@/models/validator";
import { useForm } from "@/composables/useForm";

const signupSchema = validator
  .schema({
    username: validator.string().required("Username is required"),
    password: validator
      .string()
      .min(8, "Password must be at least 8 characters")
      .regex(/[A-Z]/, "Password must contain at least one capital letter")
      .regex(/[0-9]/, "Password must contain at least one number"),
    repeatPassword: validator.string(),
  })
  .refine((data) => data.password === data.repeatPassword, {
    message: "Passwords do not match",
    field: "repeatPassword",
  });

const {
  fields,
  errors,
  handleSubmit: handleRegisterSubmit,
} = useForm({
  validationSchema: signupSchema,
});

const { isLoading, error, register, login } = useAuth();

const onSignup = handleRegisterSubmit(async (values) => {
  console.log("Sign up");
  await register(values.username, values.password);
});
</script>

<template>
  <form @submit="onSignup" class="flex flex-col gap-3 h-full">
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

    <!-- Repeat Password field -->
    <div class="flex gap-1 flex-col">
      <label class="text-xs text-muted-foreground">Repeat Password</label>
      <Input
        v-model="fields.repeatPassword"
        type="password"
        placeholder="Passwords must match"
        :class="
          errors.repeatPassword
            ? 'border-2 border-destructive focus-visible:border-destructive focus-visible:ring-destructive/30'
            : ''
        "
      />
      <p class="text-xs text-destructive">{{ errors.repeatPassword }}</p>
    </div>
    <Button type="submit" size="lg" class="text-md font-extrabold mt-auto">
      Create account
    </Button>
  </form>
</template>
