import { httpClient } from "./httpClient";

export async function getActiveCategories() {
  const res = await httpClient.get("/api/v1/categories");
  return res.data;
}

