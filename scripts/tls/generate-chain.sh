#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/tls/generate-chain.sh --student-id <ID> [--out-dir <DIR>] [--domain <DNS>] [--keystore-password <PASS>]

Examples:
  ./scripts/tls/generate-chain.sh --student-id 123456 --out-dir ./.secrets/tls
  ./scripts/tls/generate-chain.sh --student-id 123456 --domain localhost --keystore-password 'changeit'
EOF
}

STUDENT_ID=""
OUT_DIR="./.secrets/tls"
DOMAIN="localhost"
KEYSTORE_PASSWORD=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --student-id)
      STUDENT_ID="${2:-}"
      shift 2
      ;;
    --out-dir)
      OUT_DIR="${2:-}"
      shift 2
      ;;
    --domain)
      DOMAIN="${2:-}"
      shift 2
      ;;
    --keystore-password)
      KEYSTORE_PASSWORD="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "${STUDENT_ID}" ]]; then
  echo "Error: --student-id is required." >&2
  exit 1
fi

if [[ -z "${KEYSTORE_PASSWORD}" ]]; then
  if [[ -n "${TLS_KEYSTORE_PASSWORD:-}" ]]; then
    KEYSTORE_PASSWORD="${TLS_KEYSTORE_PASSWORD}"
  else
    echo "Error: provide --keystore-password or set TLS_KEYSTORE_PASSWORD." >&2
    exit 1
  fi
fi

mkdir -p "${OUT_DIR}"
umask 077

ROOT_KEY="${OUT_DIR}/atlas-root-${STUDENT_ID}.key"
ROOT_CSR="${OUT_DIR}/atlas-root-${STUDENT_ID}.csr"
ROOT_CRT="${OUT_DIR}/atlas-root-${STUDENT_ID}.crt"
INT_KEY="${OUT_DIR}/nebula-int-${STUDENT_ID}.key"
INT_CSR="${OUT_DIR}/nebula-int-${STUDENT_ID}.csr"
INT_CRT="${OUT_DIR}/nebula-int-${STUDENT_ID}.crt"
SRV_KEY="${OUT_DIR}/skyline-svc-${STUDENT_ID}.key"
SRV_CSR="${OUT_DIR}/skyline-svc-${STUDENT_ID}.csr"
SRV_CRT="${OUT_DIR}/skyline-svc-${STUDENT_ID}.crt"
CHAIN_CRT="${OUT_DIR}/skyline-svc-${STUDENT_ID}-chain.crt"
P12_FILE="${OUT_DIR}/server-keystore.p12"

ROOT_EXT="${OUT_DIR}/root-ext.cnf"
INT_EXT="${OUT_DIR}/int-ext.cnf"
SRV_EXT="${OUT_DIR}/srv-ext.cnf"

cat > "${ROOT_EXT}" <<EOF
basicConstraints=critical,CA:true,pathlen:1
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid:always,issuer
EOF

cat > "${INT_EXT}" <<EOF
basicConstraints=critical,CA:true,pathlen:0
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

cat > "${SRV_EXT}" <<EOF
basicConstraints=critical,CA:false
keyUsage=critical,digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:${DOMAIN},DNS:localhost,DNS:student-${STUDENT_ID}.demo4.local,IP:127.0.0.1
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl genrsa -out "${ROOT_KEY}" 4096
openssl req -new -sha256 \
  -key "${ROOT_KEY}" \
  -subj "/C=RU/O=Demo4/OU=Student-${STUDENT_ID}/CN=Atlas Root CA ${STUDENT_ID}" \
  -out "${ROOT_CSR}"

openssl x509 -req -sha256 -days 3650 \
  -in "${ROOT_CSR}" \
  -signkey "${ROOT_KEY}" \
  -out "${ROOT_CRT}" \
  -extfile "${ROOT_EXT}"

openssl genrsa -out "${INT_KEY}" 4096
openssl req -new -sha256 \
  -key "${INT_KEY}" \
  -subj "/C=RU/O=Demo4/OU=Student-${STUDENT_ID}/CN=Nebula Intermediate CA ${STUDENT_ID}" \
  -out "${INT_CSR}"

openssl x509 -req -sha256 -days 1825 \
  -in "${INT_CSR}" \
  -CA "${ROOT_CRT}" -CAkey "${ROOT_KEY}" -CAcreateserial \
  -out "${INT_CRT}" \
  -extfile "${INT_EXT}"

openssl genrsa -out "${SRV_KEY}" 2048
openssl req -new -sha256 \
  -key "${SRV_KEY}" \
  -subj "/C=RU/O=Demo4/OU=Student-${STUDENT_ID}/CN=${DOMAIN}" \
  -out "${SRV_CSR}"

openssl x509 -req -sha256 -days 825 \
  -in "${SRV_CSR}" \
  -CA "${INT_CRT}" -CAkey "${INT_KEY}" -CAcreateserial \
  -out "${SRV_CRT}" \
  -extfile "${SRV_EXT}"

cat "${SRV_CRT}" "${INT_CRT}" > "${CHAIN_CRT}"

openssl pkcs12 -export \
  -name service-tls \
  -inkey "${SRV_KEY}" \
  -in "${SRV_CRT}" \
  -certfile "${INT_CRT}" \
  -out "${P12_FILE}" \
  -password "pass:${KEYSTORE_PASSWORD}"

cat <<EOF
Generated certificate chain successfully:
  Root CA:          ${ROOT_CRT}
  Intermediate CA:  ${INT_CRT}
  Server cert:      ${SRV_CRT}
  Server chain:     ${CHAIN_CRT}
  PKCS12 keystore:  ${P12_FILE}

Contains student id in Subject/OU and SAN DNS: student-${STUDENT_ID}.demo4.local
EOF
