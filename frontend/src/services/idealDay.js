import { http } from "./http";

export async function getIdealDay() {
  return await http("/api/ideal-day", { method: "GET" });
}

export async function updateIdealDay(data) {
  return await http("/api/ideal-day", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}