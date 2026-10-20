import {
  getGoalsByDate,
  createPersonalGoal,
  createDailyGoal,
  updatePersonalGoal,
  updateDailyGoal,
  deletePersonalGoal,
  deleteDailyGoal,
} from "@/services/goals";
import { ref } from "vue";

export default function useGoals() {
  const isLoading = ref(false);

  async function retrieve(date) {
    const dateParam = date ?? new Date().toISOString().split("T")[0];
    console.log("Retrieving goals for date: " + dateParam);

    isLoading.value = true;
    try {
      const response = await getGoalsByDate(dateParam);
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

  function toHours(value) {
    return Math.max(1, Math.round(Number(value) || 1));
  }

  function buildDailyGoal({ date, personGoalId, sg }) {
    return {
      date,
      personGoalId,
      quantity: toHours(sg.hours),
      title: sg.label,
      description: sg.label,
      progress: 0.0,
      completed: false,
      goalTemplateId: sg.goalTemplateId ?? 1,
    };
  }

  async function create({ data, subgoals = [], event }) {
    console.log("Creating a new personal goal...");

    isLoading.value = true;
    try {
      const response = await createPersonalGoal(data);
      if (!response.ok) return;
      const created = await response.json();
      console.log(created);

      const personGoalId = created.personGoalId ?? created.id;
      for (const sg of subgoals) {
        try {
          await createDailyGoal(
            buildDailyGoal({ date: data.date, personGoalId, sg }),
          );
        } catch (e) {
          console.error("Failed to create subgoal:", e);
        }
      }

      event?.(created);
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function update({
    id,
    data,
    date,
    subgoals = [],
    originalSubgoals = [],
    event,
  }) {
    console.log("Updating personal goal with id: " + id);

    isLoading.value = true;
    try {
      const response = await updatePersonalGoal(id, data);
      if (!response.ok) return;
      const updated = await response.json();
      console.log(updated);

      const originalById = new Map(originalSubgoals.map((sg) => [sg.id, sg]));
      const keptIds = new Set();
      const goalDate = date ?? data.date;

      for (const sg of subgoals) {
        const orig = originalById.get(sg.id);
        if (!orig) {
          await createDailyGoal(
            buildDailyGoal({ date: goalDate, personGoalId: id, sg }),
          );
          continue;
        }

        keptIds.add(sg.id);
        const hoursChanged = toHours(sg.hours) !== toHours(orig.hours);
        const typeChanged =
          (sg.goalTemplateId ?? 1) !== (orig.goalTemplateId ?? 1);

        if (hoursChanged || typeChanged) {
          await deleteDailyGoal(sg.id);
          await createDailyGoal(
            buildDailyGoal({ date: goalDate, personGoalId: id, sg }),
          );
        } else if (sg.label !== orig.label) {
          await updateDailyGoal({
            id: sg.id,
            title: sg.label,
            description: sg.label,
          });
        }
      }

      for (const orig of originalSubgoals) {
        if (!keptIds.has(orig.id)) {
          await deleteDailyGoal(orig.id);
        }
      }

      event?.(updated);
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function remove({ id, event }) {
    console.log("Deleting personal goal with id: " + id);

    isLoading.value = true;
    try {
      const response = await deletePersonalGoal(id);
      if (!response.ok) return;
      event?.();
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  return { isLoading, retrieve, create, update, remove };
}
