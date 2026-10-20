import { http } from "./http";

export async function getMyChallenges(userId) {
  return await http(`api/challenge?userId=${userId}`, {
    method: "GET",
  });
}

export async function getChallenge(id) {
  return await http(`api/challenge/${id}`, {
    method: "GET",
  });
}

export async function createChallenge(challenge) {
  return await http(`api/challenge`, {
    method: "POST",
    body: JSON.stringify(challenge),
  });
}

export async function editChallenge(challenge) {
  return await http(`api/challenge`, {
    method: "PUT",
    body: JSON.stringify(challenge),
  });
}

export async function removeChallenge(id) {
  return await http(`api/challenge/${id}`, {
    method: "DELETE",
  });
}
