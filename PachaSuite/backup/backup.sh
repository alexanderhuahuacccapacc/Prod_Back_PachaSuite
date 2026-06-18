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

  # Alerta de ÉXITO por correo (PA-43)
  if [ -n "$RESEND_API_KEY" ]; then
    curl -s -X POST "https://api.resend.com/emails" \
      -H "Authorization: Bearer $RESEND_API_KEY" \
      -H "Content-Type: application/json" \
      -d "{
        \"from\": \"onboarding@resend.dev\",
        \"to\": \"$ALERT_EMAIL\",
        \"subject\": \"✅ Backup pachadb OK - $FECHA\",
        \"text\": \"El backup automático de pachadb se completó exitosamente el $FECHA. Tamaño: $TAMANIO\"
      }"
  fi

else
  echo "❌ Backup FALLÓ"

  # Alerta de FALLO por correo (PA-43)
  if [ -n "$RESEND_API_KEY" ]; then
    curl -s -X POST "https://api.resend.com/emails" \
      -H "Authorization: Bearer $RESEND_API_KEY" \
      -H "Content-Type: application/json" \
      -d "{
        \"from\": \"onboarding@resend.dev\",
        \"to\": \"$ALERT_EMAIL\",
        \"subject\": \"❌ Backup pachadb FALLÓ - $FECHA\",
        \"text\": \"El backup automático de pachadb falló el $FECHA. Revisar logs en Render.\"
      }"
  fi

  exit 1
fi