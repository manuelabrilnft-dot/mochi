import { http } from "@/services/http";

// gets the current user's profile (username, level, streak, xp, ...)
export async function getProfile() {
  return await http("/api/profile", {
    method: "GET",
  });
}
