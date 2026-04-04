#!/usr/bin/env bash
set -euo pipefail

OUT_DIR="${OUT_DIR:-./.secrets/signature}"
KEYSTORE_PATH="${KEYSTORE_PATH:-${OUT_DIR}/ticket-signing.p12}"
CERT_PATH="${CERT_PATH:-${OUT_DIR}/ticket-signing.crt}"
CERT_BASE64_PATH="${CERT_BASE64_PATH:-${OUT_DIR}/ticket-signing.crt.base64}"
ALIAS="${SIGNATURE_KEY_ALIAS:-ticket-signing}"
STOREPASS="${SIGNATURE_KEYSTORE_PASSWORD:-changeit}"
KEYPASS="${SIGNATURE_KEY_PASSWORD:-${STOREPASS}}"
DNAME="${DNAME:-CN=NewSEM Ticket Signing, OU=License, O=NewSEM, L=Moscow, C=RU}"
VALID_DAYS="${VALID_DAYS:-3650}"

mkdir -p "${OUT_DIR}"

keytool -genkeypair \
  -alias "${ALIAS}" \
  -keyalg RSA \
  -keysize 3072 \
  -sigalg SHA256withRSA \
  -keystore "${KEYSTORE_PATH}" \
  -storetype PKCS12 \
  -storepass "${STOREPASS}" \
  -keypass "${KEYPASS}" \
  -dname "${DNAME}" \
  -validity "${VALID_DAYS}" \
  -noprompt

keytool -exportcert \
  -alias "${ALIAS}" \
  -keystore "${KEYSTORE_PATH}" \
  -storetype PKCS12 \
  -storepass "${STOREPASS}" \
  -rfc \
  -file "${CERT_PATH}"

base64 < "${CERT_PATH}" | tr -d '\n' > "${CERT_BASE64_PATH}"

cat <<MSG
Generated:
- ${KEYSTORE_PATH}
- ${CERT_PATH}
- ${CERT_BASE64_PATH}

Use this value in CI/CD variable SIGNATURE_PUBLIC_CERT_BASE64:
$(cat "${CERT_BASE64_PATH}")
MSG
