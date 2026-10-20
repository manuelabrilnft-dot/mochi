import {
  getMyChallenges,
  getChallenge,
  createChallenge,
  editChallenge,
  removeChallenge,
} from "@/services/challenge";
import { meService } from "@/services/auth";
import { ref } from "vue";

export default function useChallenges() {
  const isLoading = ref(false);

  async function currentUserId() {
    const res = await meService();
    if (!res?.ok) return null;
    // /auth/me returns the raw user id
    return await res.json();
  }

  async function retrieve() {
    console.log("Retrieving challenges...");

    isLoading.value = true;
    try {
      const userId = await currentUserId();
      if (!userId) return [];
      const response = await getMyChallenges(userId);
      if (!response?.ok) return [];
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

  async function retrieveOne(id) {
    console.log("Retrieving challenge with id: " + id);

    isLoading.value = true;
    try {
      const response = await getChallenge(id);
      if (!response?.ok) return null;
      const data = await response.json();
      console.log(data);
      return data;
    } catch (e) {
      console.error(e);
      return null;
    } finally {
      isLoading.value = false;
    }
  }

  async function create({ data, event }) {
    console.log("Creating a new challenge...");

    isLoading.value = true;
    try {
      const response = await createChallenge(data);
      if (!response?.ok) return;
      const created = await response.json();
      console.log(created);
      event?.(created);
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function update({ data, event }) {
    console.log("Updating challenge with id: " + data?.id);

    isLoading.value = true;
    try {
      const response = await editChallenge(data);
      if (!response?.ok) return;
      const updated = await response.json();
      console.log(updated);
      event?.(updated);
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function remove({ id, event }) {
    console.log("Deleting challenge with id: " + id);

    isLoading.value = true;
    try {
      const response = await removeChallenge(id);
      if (!response?.ok) return;
      event?.();
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  return { isLoading, retrieve, retrieveOne, create, update, remove };
}
