import { http } from "./http";

export async function getAchievement() {
  return await http("/api/achievement", { method: "GET" });
}
