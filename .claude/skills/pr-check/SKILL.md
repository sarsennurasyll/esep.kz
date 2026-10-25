---
name: pr-check
description: Pre-commit / pre-push / pre-merge check for Esep.kz — tests on JDK 21, personal-data and secret scan of the diff, AI-signature check, author check, PR and CI status. Use before every push to hybrid/* and before asking the owner to merge.
---

# Проверка перед commit, push и merge

Выполнять по порядку, выводить только итоги. Реальные выписки, отчёты surefire с прогона на реальном PDF и логи с данными в репозиторий не добавлять.

## 1. Тесты

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn -q -B test > "$TMPDIR/esep-test.log" 2>&1; echo EXIT=$?
python3 - <<'PY'
import glob,re
t=f=e=s=0
for p in glob.glob('target/surefire-reports/TEST-*.xml'):
    a=dict(re.findall(r'(tests|errors|skipped|failures)="(\d+)"',open(p).read(800)))
    t+=int(a['tests']);f+=int(a['failures']);e+=int(a['errors']);s+=int(a['skipped'])
print('tests',t,'failures',f,'errors',e,'skipped',s)
PY
```

- Не перезапускать, если с прошлого зелёного прогона код не менялся.
- Ожидаемо пропускаются 4 теста реального PDF. Testcontainers-тесты пропускаются без Docker — это нужно указать в отчёте.
- Тесты на реальной выписке — только локально: `-Dkaspi.pdf.path=<файл вне репозитория>`; после прогона удалить `target/surefire-reports`.

## 2. Diff: персональные данные, секреты, подписи

```bash
{ git diff; git diff --cached; git ls-files --others --exclude-standard | xargs cat; } \
  | grep -n -i -E 'KZ[0-9]{2}[0-9A-Z]{10,}|\*[1-9][0-9]{3}\b|\+7[0-9 ]{9,}|password=|secret|token=|BEGIN (RSA|OPENSSH)|co-authored-by|generated with|🤖'
```

- Имена в тестах — только явно вымышленные («Тестов Т.»). Имена, похожие на контрагентов из реальной выписки, заменить.
- Документы содержат только агрегаты с указанным знаменателем.

## 3. Коммит

- Файлы добавлять по пути после просмотра `git status --short`.
- Conventional Commits: тип на английском, описание на русском, без подписей AI-инструментов.
- Автор: `git log -1 --format='%an <%ae>'` должен быть `sarsennurasyll <160136040+sarsennurasyll@users.noreply.github.com>`. Глобальный Git не менять.

## 4. Push и PR

```bash
git push -q && git status -sb | head -1
gh pr view 1 --json mergeable,mergeStateStatus --jq .
gh run list --branch "$(git branch --show-current)" --limit 2 --json name,conclusion --jq '.[]|"\(.name) \(.conclusion)"'
```

- Если run упал без шагов, прочитать аннотации: `gh api repos/sarsennurasyll/esep.kz/check-runs/<job-id>/annotations`. Сообщение о billing-блокировке — проблема аккаунта владельца, не кода.
- Push только в рабочую ветку. Merge в `main`, force push и переписывание истории — только с разрешения владельца.
