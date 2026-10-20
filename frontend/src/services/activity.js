import { http } from "@/services/http";

export async function getActivity(startDate, endDate) {
  return await http(`api/activity?startDate=${startDate}&endDate=${endDate}`, {
    method: "GET",
  });
}

export async function createActivity(activity) {
  return await http(`api/activity`, {
    method: "POST",
    body: JSON.stringify(activity),
  });
}

export async function editActivity(activity) {
  return await http(`api/activity`, {
    method: "PUT",
    body: JSON.stringify(activity),
  });
}

export async function completeActivity(id) {
  return await http(`api/activity/complete/${id}`, {
    method: "PATCH",
  });
}

export async function removeActivity(id) {
  return await http(`api/activity/${id}`, {
    method: "DELETE",
  });
}
