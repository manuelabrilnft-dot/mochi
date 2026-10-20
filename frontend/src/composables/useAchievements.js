import { getAchievement } from "@/services/achievement";
import { ref } from "vue";

export default function useAchievements() {
  const isLoading = ref(false);

  async function retrieve() {
    console.log("retrieving achievements...");

    isLoading.value = true;
    try {
      const response = await getAchievement();
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

  return { isLoading, retrieve };
}