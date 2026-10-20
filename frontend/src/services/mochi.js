import { http } from "./http";

// The mochi look and game properties are read from the /profile endpoint
// (see useMochi). The backend only exposes PUT /api/mochi for saving the look —
// there is no GET /api/mochi or /api/game-properties.

// saves the mochi look back to the backend
export async function saveMochiService(mochi) {
  const response = await http("/api/mochi", {
    method: "PUT",
    body: JSON.stringify(mochi),
  });

  if (!response.ok) {
    throw new Error("Could not save Mochi");
  }
  return response;
}
