import { http } from "./http";

export async function getIdealDayActivitiesService() {
  const response = await http("api/ideal-day/activities", {
    method: "GET",
  });

  if (!response.ok) {
    throw new Error("Could not load ideal day activities");
  }
  return response.json();
}

export async function saveIdealDayActivitiesService(activities) {
  const response = await http("api/ideal-day/activities", {
    method: "PUT",
    body: JSON.stringify(activities),
  });

  if (!response.ok) {
    throw new Error("Could not save ideal day activities");
  }
  return response.json();
}