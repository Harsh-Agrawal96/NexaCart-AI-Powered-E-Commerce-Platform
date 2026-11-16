import { httpClient } from "./httpClient";

export async function getGatewayHealth() {
  const res = await httpClient.get("/actuator/health");
  return res.data;
}

export async function getGatewayInfo() {
  const res = await httpClient.get("/actuator/info");
  return res.data;
}

export async function getGatewayRoutes() {
  const res = await httpClient.get("/actuator/gateway/routes");
  return res.data;
}

export async function pingApiDocs(path) {
  const res = await httpClient.get(path);
  return res.data;
}

