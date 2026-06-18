#!/bin/bash
set -e

echo "🔄 Iniciando backup - $(date)"

FECHA=$(date +%Y%m%d_%H%M)
ARCHIVO="/tmp/backup_${FECHA}.sql"

# Hacer el dump
pg_dump "$DATABASE_URL" > "$ARCHIVO"

if [ $? -eq 0 ]; then
  TAMANIO=$(du -sh "$ARCHIVO" | cut -f1)
  echo "✅ Backup completado: $ARCHIVO ($TAMANIO)"

  # Subir a Cloudflare R2
  AWS_ACCESS_KEY_ID=$R2_ACCESS_KEY \
  AWS_SECRET_ACCESS_KEY=$R2_SECRET_KEY \
  aws s3 cp "$ARCHIVO" "s3://$R2_BUCKET/backup_${FECHA}.sql" \
    --endpoint-url "$R2_ENDPOINT"

  if [ $? -eq 0 ]; then
    echo "☁️ Subido a R2: backup_${FECHA}.sql"

    # Email éxito
    curl -s -X POST "https://api.resend.com/emails" \
      -H "Authorization: Bearer $RESEND_API_KEY" \
      -H "Content-Type: application/json" \
      -d "{
        \"from\": \"onboarding@resend.dev\",
        \"to\": \"$ALERT_EMAIL\",
        \"subject\": \"✅ Backup pachadb OK - $FECHA\",
        \"text\": \"Backup completado y guardado en Cloudflare R2. Tamaño: $TAMANIO\"
      }"
  fi

else
  echo "❌ Backup FALLÓ"

  # Email fallo
  curl -s -X POST "https://api.resend.com/emails" \
    -H "Authorization: Bearer $RESEND_API_KEY" \
    -H "Content-Type: application/json" \
    -d "{
      \"from\": \"onboarding@resend.dev\",
      \"to\": \"$ALERT_EMAIL\",
      \"subject\": \"❌ Backup pachadb FALLÓ - $FECHA\",
      \"text\": \"El backup automático falló el $FECHA. Revisar logs en Render.\"
    }"

  exit 1
fi