<script setup>
import { ref, computed, onMounted } from "vue";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card } from "@/components/ui/card";
import { UserPlus, UserMinus, Search } from "@lucide/vue";
import useFriends from "@/composables/useFriends";

const {
  isLoading,
  retrieveFriends,
  retrieveRequests,
  search,
  addFriend,
  accept,
  remove,
} = useFriends();

const friends = ref([]);
const requests = ref([]);

const tabs = computed(() => [
  { value: "all", label: "All friends" },
  { value: "requests", label: "Requests", badge: requests.value.length },
]);

const activeTab = ref("all");

// Map API response { id, username } to display shape
function mapFriend(f) {
  return {
    id: f.id,
    name: f.username,
    initial: f.username?.[0]?.toUpperCase() ?? "?",
    level: f.level ?? 1,
    online: f.online ?? false,
  };
}

async function loadFriends() {
  const data = await retrieveFriends();
  friends.value = data.map(mapFriend);
}

async function loadRequests() {
  const data = await retrieveRequests("received");
  requests.value = data.map(mapFriend);
}

async function loadSentRequests() {
  const data = await retrieveRequests("sent");
  pendingSent.value = data.map(mapFriend);
}

onMounted(() => {
  loadFriends();
  loadRequests();
  loadSentRequests();
});

function tintFor(level) {
  if (level >= 15) return "bg-violet-100";
  if (level >= 10) return "bg-emerald-100";
  if (level >= 5) return "bg-amber-100";
  return "bg-sky-100";
}

const expandedId = ref(null);
function toggleExpand(id) {
  expandedId.value = expandedId.value === id ? null : id;
}

// ---------- remove / decline ----------
async function handleRemove(id) {
  await remove({
    friendId: id,
    event: () => {
      friends.value = friends.value.filter((f) => f.id !== id);
      if (expandedId.value === id) expandedId.value = null;
    },
  });
}

async function handleDecline(req) {
  await remove({
    friendId: req.id,
    event: () => {
      requests.value = requests.value.filter((r) => r.id !== req.id);
    },
  });
}

// ---------- search + add ----------
const showAddFriend = ref(false);
const searchQuery = ref("");
const searchResults = ref([]);
// Pending requests we've sent (mapped display shape); used both for the
// "Pending (sent)" list and to disable re-sending in search results.
const pendingSent = ref([]);
const sentIds = computed(() => new Set(pendingSent.value.map((r) => r.id)));

async function handleSearch() {
  if (!searchQuery.value.trim()) return;
  await search({
    query: searchQuery.value,
    event: (results) => {
      searchResults.value = results;
    },
  });
}

async function handleSendRequest(user) {
  await addFriend({
    friendId: user.id,
    event: () => {
      if (!sentIds.value.has(user.id)) pendingSent.value.push(mapFriend(user));
    },
  });
}

// Cancel a pending request we sent (DELETE removes the row either direction)
async function handleCancelSent(req) {
  await remove({
    friendId: req.id,
    event: () => {
      pendingSent.value = pendingSent.value.filter((r) => r.id !== req.id);
    },
  });
}

// ---------- accept ----------
async function handleAccept(req) {
  await accept({
    senderId: req.id,
    event: () => {
      requests.value = requests.value.filter((r) => r.id !== req.id);
      friends.value.push(mapFriend({ id: req.id, username: req.name }));
    },
  });
}

function closeAddFriend() {
  showAddFriend.value = false;
  searchQuery.value = "";
  searchResults.value = [];
}
</script>

<template>
  <div class="min-h-screen bg-canvas p-8 flex flex-col gap-5">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold text-foreground">Friends</h1>
        <p class="text-sm text-muted-foreground">
          {{ friends.length }} friends · {{ requests.length }} requests
        </p>
      </div>
      <Button
        class="bg-brand text-brand-foreground hover:bg-brand/90"
        @click="showAddFriend = !showAddFriend"
      >
        <UserPlus class="h-4 w-4 mr-2" />
        Add friend
      </Button>
    </div>

    <!-- Search panel -->
    <Card v-if="showAddFriend" class="p-4 gap-3">
      <p class="text-sm font-medium text-foreground">
        Search for users by username
      </p>
      <div class="flex gap-2">
        <Input
          v-model="searchQuery"
          placeholder="e.g. Alex, Kai..."
          class="max-w-xs"
          @keyup.enter="handleSearch"
        />
        <Button size="sm" :disabled="isLoading" @click="handleSearch">
          <Search class="h-4 w-4 mr-1" />
          {{ isLoading ? "Searching..." : "Search" }}
        </Button>
        <Button size="sm" variant="outline" @click="closeAddFriend"
          >Cancel</Button
        >
      </div>

      <!-- Search results -->
      <div v-if="searchResults.length" class="flex flex-col gap-1">
        <div
          v-for="user in searchResults"
          :key="user.id"
          class="flex items-center justify-between px-3 py-2 rounded-lg hover:bg-accent"
        >
          <div class="flex items-center gap-2">
            <div
              class="w-8 h-8 rounded-full bg-muted flex items-center justify-center text-sm font-semibold text-muted-foreground"
            >
              {{ user.username?.[0]?.toUpperCase() }}
            </div>
            <span class="text-sm font-medium">{{ user.username }}</span>
          </div>
          <Button
            size="sm"
            :disabled="
              sentIds.has(user.id) || friends.some((f) => f.id === user.id)
            "
            @click="handleSendRequest(user)"
          >
            {{
              sentIds.has(user.id)
                ? "Sent ✓"
                : friends.some((f) => f.id === user.id)
                  ? "Already friends"
                  : "Add"
            }}
          </Button>
        </div>
      </div>
      <p
        v-else-if="searchQuery && !isLoading"
        class="text-sm text-muted-foreground"
      >
        No users found for "{{ searchQuery }}"
      </p>
    </Card>

    <!-- Loading skeleton -->
    <div v-if="isLoading && !friends.length" class="flex flex-col gap-2">
      <div
        v-for="i in 3"
        :key="i"
        class="h-16 bg-muted rounded-lg animate-pulse"
      />
    </div>

    <div v-else>
      <div class="flex gap-1 bg-card border border-border rounded-lg p-1 w-fit">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="px-4 py-1.5 rounded-md text-sm transition-colors flex items-center gap-1.5"
          :class="
            activeTab === tab.value
              ? 'bg-muted font-medium text-foreground'
              : 'text-muted-foreground hover:text-foreground'
          "
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
          <span
            v-if="tab.badge"
            class="bg-destructive text-white rounded-full px-1.5 text-xs font-medium"
            >{{ tab.badge }}</span
          >
        </button>
      </div>

      <div class="mt-4">
        <!-- All friends -->
        <Card v-if="activeTab === 'all'" class="p-0 gap-0">
          <div
            v-if="!friends.length"
            class="p-6 text-center text-sm text-muted-foreground"
          >
            No friends yet — search for someone to add!
          </div>
          <div
            v-for="friend in friends"
            :key="friend.id"
            class="border-b last:border-b-0"
          >
            <div
              class="flex items-center justify-between p-4 cursor-pointer hover:bg-accent/50"
              @click="toggleExpand(friend.id)"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold text-foreground"
                  :class="tintFor(friend.level)"
                >
                  {{ friend.initial }}
                </div>
                <div class="flex flex-col">
                  <div class="flex items-center">
                    <span class="font-medium">{{ friend.name }}</span>
                    <span
                      v-if="friend.online"
                      class="w-2 h-2 rounded-full bg-emerald-500 inline-block ml-2"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Expanded -->
            <div
              v-if="expandedId === friend.id"
              class="px-4 pb-4 pt-3 border-t bg-muted/30"
            >
              <div class="flex gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  :disabled="isLoading"
                  @click="handleRemove(friend.id)"
                >
                  <UserMinus class="h-4 w-4 mr-2" />
                  {{ isLoading ? "Removing..." : "Remove friend" }}
                </Button>
              </div>
            </div>
          </div>
        </Card>

        <!-- Requests -->
        <div v-if="activeTab === 'requests'" class="flex flex-col gap-4">
          <!-- Received -->
          <Card class="p-0 gap-0">
            <p class="px-4 pt-4 pb-2 text-sm font-semibold text-foreground">
              Received
            </p>
            <div
              v-if="!requests.length"
              class="px-4 pb-4 text-sm text-muted-foreground"
            >
              No incoming requests
            </div>
            <div
              v-for="req in requests"
              :key="req.id"
              class="border-t flex items-center justify-between p-4"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold text-foreground"
                  :class="tintFor(req.level)"
                >
                  {{ req.initial }}
                </div>
                <span class="font-medium">{{ req.name }}</span>
              </div>
              <div class="flex gap-2">
                <Button
                  size="sm"
                  :disabled="isLoading"
                  @click="handleAccept(req)"
                  >Accept</Button
                >
                <Button
                  size="sm"
                  variant="outline"
                  :disabled="isLoading"
                  @click="handleDecline(req)"
                  >Decline</Button
                >
              </div>
            </div>
          </Card>

          <!-- Pending (sent) -->
          <Card class="p-0 gap-0">
            <p class="px-4 pt-4 pb-2 text-sm font-semibold text-foreground">
              Pending (sent)
            </p>
            <div
              v-if="!pendingSent.length"
              class="px-4 pb-4 text-sm text-muted-foreground"
            >
              No pending sent requests
            </div>
            <div
              v-for="req in pendingSent"
              :key="req.id"
              class="border-t flex items-center justify-between p-4"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold text-foreground"
                  :class="tintFor(req.level)"
                >
                  {{ req.initial }}
                </div>
                <span class="font-medium">{{ req.name }}</span>
              </div>
              <Button
                size="sm"
                variant="outline"
                :disabled="isLoading"
                @click="handleCancelSent(req)"
                >Cancel</Button
              >
            </div>
          </Card>
        </div>
      </div>
    </div>

    <p class="text-sm text-muted-foreground mt-4">
      Showing {{ friends.length }} friends
    </p>
  </div>
</template>
