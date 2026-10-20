import { http } from "./http";

export async function getGoalsByDate(date) {
  return await http(`/api/goals/${date}`, { method: "GET" });
}

export async function createPersonalGoal(data) {
  return await http("/api/goals/personal", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updatePersonalGoal(id, data) {
  return await http("/api/goals/personal", {
    method: "PUT",
    body: JSON.stringify({ ...data, id }),
  });
}

export async function deletePersonalGoal(id) {
  return await http(`/api/goals/personal/${id}`, { method: "DELETE" });
}

// Daily goals (subgoals)

export async function createDailyGoal(data) {
  return await http("/api/goals/daily", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateDailyGoal(data) {
  return await http("/api/goals/daily", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteDailyGoal(id) {
  return await http(`/api/goals/daily/${id}`, { method: "DELETE" });
}
