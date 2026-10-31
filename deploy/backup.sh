#!/bin/sh
# Шифрованная копия базы. Запуск с сервера: AGE_RECIPIENT=age1... deploy/backup.sh /backups
# Незашифрованная копия на диск не пишется. Перенос в хранилище в РК и проверка восстановления — в deploy/README.md.
set -eu

target_dir="${1:?Укажите каталог для копий}"
: "${AGE_RECIPIENT:?Укажите публичный ключ age}"
file="$target_dir/esep-$(date -u +%Y%m%dT%H%M%SZ).dump.age"

umask 077
docker compose -f "$(dirname "$0")/compose.production.yml" exec -T db pg_dump -U esep -d esep -Fc \
    | age -r "$AGE_RECIPIENT" > "$file.partial"
mv "$file.partial" "$file"
echo "$file"
