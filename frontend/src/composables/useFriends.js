import {
  getFriends,
  getFriendRequests,
  searchUsers,
  sendFriendRequest,
  acceptFriendRequest,
  removeFriend,
} from "@/services/friends";
import { ref } from "vue";

export default function useFriends() {
  const isLoading = ref(false);

  async function retrieveFriends() {
    console.log("Retrieving friends list...");
    isLoading.value = true;
    try {
      const response = await getFriends();
      if (!response.ok) return [];
      const data = await response.json();
      console.log(data);
      // Returns [{ id, username }]
      return data;
    } catch (e) {
      console.error(e);
      return [];
    } finally {
      isLoading.value = false;
    }
  }

  async function retrieveRequests(type = "received") {
    console.log(`Retrieving ${type} friend requests...`);
    isLoading.value = true;
    try {
      const response = await getFriendRequests(type);
      if (!response.ok) return [];
      const data = await response.json();
      console.log(`${type} friend requests:`, data);
      // Returns [{ id, username }]
      return data;
    } catch (e) {
      console.error(e);
      return [];
    } finally {
      isLoading.value = false;
    }
  }

  async function search({ query, event }) {
    console.log("Searching users: " + query);
    isLoading.value = true;
    try {
      const response = await searchUsers(query);
      if (!response.ok) return [];
      const data = await response.json();
      console.log(data);
      event?.(data);
      return data;
    } catch (e) {
      console.error(e);
      return [];
    } finally {
      isLoading.value = false;
    }
  }

  async function addFriend({ friendId, event }) {
    console.log("Sending friend request to id: " + friendId);
    isLoading.value = true;
    try {
      const response = await sendFriendRequest(friendId);
      if (!response.ok) return;
      const data = await response.json();
      console.log(data);
      event?.(data);
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function accept({ senderId, event }) {
    console.log("Accepting friend request from id: " + senderId);
    isLoading.value = true;
    try {
      const response = await acceptFriendRequest(senderId);
      if (!response.ok) return;
      const data = await response.json();
      console.log(data);
      event?.(data);
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  async function remove({ friendId, event }) {
    console.log("Removing friend with id: " + friendId);
    isLoading.value = true;
    try {
      const response = await removeFriend(friendId);
      if (!response.ok) return;
      event?.();
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  }

  return {
    isLoading,
    retrieveFriends,
    retrieveRequests,
    search,
    addFriend,
    accept,
    remove,
  };
}