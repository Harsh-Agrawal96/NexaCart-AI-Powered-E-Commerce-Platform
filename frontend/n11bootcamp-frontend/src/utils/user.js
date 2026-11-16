export function getUserIdFromKeycloak(kc) {
  const sub =
    kc?.tokenParsed?.sub ??
    kc?.idTokenParsed?.sub ??
    kc?.subject ??
    null;
  return typeof sub === "string" && sub.length ? sub : null;
}

