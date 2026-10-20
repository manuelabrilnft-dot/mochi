import {
  createActivity,
  getActivity,
  completeActivity,
  editActivity,
  removeActivity,
} from "@/services/activity";
import { ref } from "vue";

export default function useActivity() {
  const isLoading = ref(false);
  async function create({ data, event }) {
    console.log("Creating a new activity...");

    isLoading.value = true;
    try {
      console.log(data);
      const response = await createActivity(data);
      if (response?.ok) {
        event();
        return { ok: true };
      }
      // backend returns a plain message on 400, e.g. "Activity outside awake hours"
      let error = (await response?.text()) ?? "Couldn't create activity";
      try {
        error = JSON.parse(error);
      } catch {
        // not JSON-wrapped — use the raw text
      }
      return { ok: false, error: String(error).trim() };
    } catch (e) {
      console.error(e);
      return { ok: false, error: e.message };
    } finally {
      isLoading.value = false;
    }
  }

  async function retrieve({ startDate, endDate }) {
    console.log(
      "retrieving activities from " + startDate + " until " + endDate,
    );

    isLoading.value = true;
    try {
      const response = await getActivity(startDate, endDate);
      if (!response || !response.ok) return [];
      const data = await response.json();
      console.log(data);
      return data;
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function complete(id) {
    console.log("Completing activity " + id);
    try {
      const response = await completeActivity(id);
      return Boolean(response?.ok);
    } catch (e) {
      console.error(e);
      return false;
    }
  }

  async function edit(activity) {
    console.log("Editing activity " + activity.id);
    try {
      const response = await editActivity(activity);
      return Boolean(response?.ok);
    } catch (e) {
      console.error(e);
      return false;
    }
  }

  async function remove(id) {
    console.log("Removing activity " + id);
    try {
      const response = await removeActivity(id);
      return Boolean(response?.ok);
    } catch (e) {
      console.error(e);
      return false;
    }
  }

  return { create, retrieve, complete, edit, remove };
}
