import { http } from "./http";

export async function getFriends() {
  return await http("/api/friendship/list", { method: "GET" });
}

export async function searchUsers(query) {
  return await http(
    `/api/friendship/search?query=${encodeURIComponent(query)}`,
    { method: "GET" },
  );
}

export async function getFriendRequests(type) {
  return await http(
    `/api/friendship/requests?type=${encodeURIComponent(type)}`,
    {
      method: "GET",
    },
  );
}

export async function sendFriendRequest(friendId) {
  return await http(`/api/friendship/request?friendId=${friendId}`, {
    method: "POST",
  });
}

export async function acceptFriendRequest(senderId) {
  return await http(`/api/friendship/accept?senderId=${senderId}`, {
    method: "PATCH",
  });
}

export async function removeFriend(friendId) {
  return await http(`/api/friendship/remove?friendId=${friendId}`, {
    method: "DELETE",
  });
}
