import { getGoals, createGoal, deleteGoal } from "@/services/goals";
import { ref } from "vue";

export default function useGoals() {
  const isLoading = ref(false);

  async function retrieve() {
    console.log("Retrieving goals...");
    isLoading.value = true;
    try {
      const response = await getGoals();
      if (!response.ok) return [];
      const data = await response.json();
      console.log(data);
      return data;
    } catch (e) {
      console.error(e);
      return [];
    } finally {
      isLoading.value = false;
    }
  }

  async function create({ data, event }) {
    console.log("Creating a new goal...");
    isLoading.value = true;
    try {
      const response = await createGoal(data);
      if (!response.ok) return;
      console.log(response);
      event?.();
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function remove({ id, event }) {
    console.log("Deleting goal with id: " + id);
    isLoading.value = true;
    try {
      const response = await deleteGoal(id);
      if (!response.ok) return;
      event?.();
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  return { isLoading, retrieve, create, remove };
}