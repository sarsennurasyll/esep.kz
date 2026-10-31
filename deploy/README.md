# Развёртывание Esep.kz

Staging и Production: одна ВМ в дата-центре РК, Docker Compose с приложением, PostgreSQL и nginx ([ADR-0005](../docs/adr/0005-path-to-production.md), [ADR-0011](../docs/adr/0011-production-profile.md)). Покупка ВМ и домена и открытие для пользователей — только с согласия владельца.

Перед Production: юрист проверил `/privacy` и `/terms`, в них указаны реквизиты оператора, Staging пройден. На Staging — только синтетические выписки.

## Первый запуск

1. На ВМ: Docker, Certbot, `age`; в файрволе открыты только 22, 80 и 443.
2. DNS домена указывает на ВМ.
3. Настройки:
   ```bash
   cp deploy/.env.example deploy/.env && chmod 600 deploy/.env
   ```
   Заполните `ESEP_DOMAIN`, `DB_PASSWORD` (`openssl rand -hex 32`), `ESEP_ADMIN_EMAILS`. Для Staging скопируйте `nginx/access-staging.conf.example` в `nginx/access-staging.conf`, укажите свой IP и задайте его в `ESEP_NGINX_ACCESS_FILE`.
4. Первый сертификат, пока порт 80 свободен:
   ```bash
   sudo certbot certonly --standalone -d "$ESEP_DOMAIN"
   ```
5. Запуск:
   ```bash
   docker compose -f deploy/compose.production.yml --env-file deploy/.env up -d --build --wait
   ```
6. Проверка: `https://домен/check` открывается, `http://` ведёт на `https://`, `/actuator/health` и `/api-docs` отвечают 404.

Приложение применяет миграции при запуске. Под профилем `prod` оно не стартует с локальными именами хоста или паролем базы для разработки.

## Продление сертификата

Задание cron на ВМ, два раза в день:

```bash
certbot renew --webroot -w /var/www/certbot --deploy-hook "docker compose -f /opt/esep/deploy/compose.production.yml --env-file /opt/esep/deploy/.env exec nginx nginx -s reload"
```

## Резервные копии

Создайте пару ключей `age` на компьютере владельца; на сервере хранится только публичный ключ, иначе взлом сервера раскроет и копии.

```bash
AGE_RECIPIENT=age1... deploy/backup.sh /var/backups/esep
```

Запускать ежедневно через cron и переносить файлы в хранилище **в РК** (провайдер ещё не выбран). Раз в месяц проверять восстановление на отдельной базе:

```bash
age -d -i key.txt копия.dump.age | docker compose -f deploy/compose.production.yml --env-file deploy/.env exec -T db sh -c 'createdb -U esep restored && pg_restore -U esep -d restored'
```

## Обновление и откат

Перед обновлением сделайте копию базы. Обновление: `git pull`, затем команда запуска из шага 5. Миграции необратимы, поэтому откат кода после новой миграции — только с восстановлением копии.
