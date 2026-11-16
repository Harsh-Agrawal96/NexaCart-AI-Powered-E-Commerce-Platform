let keycloakRef = null;

export function setKeycloakInstance(kc) {
  keycloakRef = kc;
}

export function getKeycloakInstance() {
  return keycloakRef;
}
