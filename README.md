# Лабораторная работа #2. Разрешения, компоненты и IPC

## Фиксированные идентификаторы

Их нельзя менять: по ним работают `scripts/check.sh` и клиент преподавателя.

| Что | Значение |
|---|---|
| applicationId «Заметок» | `edu.androidsec.notes` |
| applicationId клиента | `edu.androidsec.notes.client` |
| Deep link | `notes://note/<id>` |
| Authority FileProvider | `edu.androidsec.notes.files` |
| Authority провайдера заголовков | `edu.androidsec.notes.provider`, путь `/notes` |
| Signature-разрешение | `edu.androidsec.notes.permission.READ_NOTES` |
| Класс ресивера | `edu.androidsec.notes.reminder.ReminderActionReceiver` |
| Действие ресивера | `edu.androidsec.notes.action.DONE` |
| Id канала уведомлений | `reminders` |

## Что где доделывать

Каждый пункт задания помечен в коде комментарием `TODO(ipc-N)`. В комментарии написано, что
сделать, к какому блоку лекции это относится и каким шагом чек-листа проверяется.

| TODO | Файлы | Состояние в заготовке |
|---|---|---|
| ipc-1 | `notes/src/main/AndroidManifest.xml` | закомментированный блок разрешений: нужные и ненужные; `exported` только у лаунчера |
| ipc-2 | `reminder/ReminderPermission.kt`, `photo/PhotoPicker.kt` | функции с сигнатурами и заглушками, кнопки показывают Toast «TODO(ipc-2)» |
| ipc-3 | манифест, `deeplink/NoteDeepLink.kt`, `ui/note/NoteViewModel.kt` | нет intent-filter, `parse()` ничего не разбирает, несуществующий id не обрабатывается |
| ipc-4 | `reminder/ReminderScheduler.kt`, `reminder/ReminderActionReceiver.kt` | уведомление без `PendingIntent`, пустой `onReceive`, ресивера нет в манифесте |
| ipc-5 | `share/ShareNote.kt`, `res/xml/file_paths.xml` | функция пишет файл в `cacheDir/exports/` и возвращает его; `file_paths.xml` нет; провайдера нет в манифесте |
| ipc-6 | `provider/TitlesProvider.kt`, манифест | `query()` возвращает `null`; провайдера и `<permission>` нет в манифесте |

Что уже работает и трогать не нужно: список и экран заметки, Room (три стартовые заметки с
id 1, 2, 3), канал уведомлений, копирование выбранного фото во внутреннее хранилище
(`photo/PhotoStore.kt`), запись заметки в текстовый файл (`share/NoteExporter.kt`),
модуль `:client` целиком.

### Как устроен код

Один Gradle-проект, два application-модуля, только AndroidX и Kotlin. Каждый экран — это пара
файлов в `ui/<экран>/`:

- `…ViewModel.kt` держит состояние экрана в `StateFlow` и вызывает репозиторий
  (`data/NotesRepository.kt`, под ним Room). Одноразовые события — «показать сообщение»,
  «закрыть экран» — уходят через `Channel`.
- `…Screen.kt` (Compose) рисует состояние и вызывает методы ViewModel. Системные диалоги
  (запрос разрешения, выбор фото, «Поделиться») запускаются отсюда, потому что им нужна Activity.

Зависимости собираются вручную в `di/AppContainer.kt`.

## Окружение

- Android Studio актуальной версии. Gradle-демону нужна Java 25
  (`gradle/gradle-daemon-jvm.properties`); если её нет, Gradle скачает её сам при первой
  сборке — для этого один раз нужен интернет.
- **Эмулятор**: образ **Google Play**, API 34 или новее (заготовка проверена на API 36,
  Pixel 6). Обычный образ без Play тоже подойдёт, но в нём `adb root` доступен, и часть
  проверок теряет смысл.
- В SDK Manager включите **Android SDK Command-line Tools**: оттуда берётся `apkanalyzer`
  для `scripts/check.sh`.
- Сборка: `./gradlew :notes:assembleDebug :client:assembleDebug` (Windows: `gradlew.bat`).
  AGP 9.4, Kotlin 2.4, compileSdk 37, minSdk 26.
- Фото для проверки Photo Picker на эмуляторе: закинуть любую картинку и попросить систему
  её увидеть:

  ```bash
  adb push photo.jpg /sdcard/Pictures/photo.jpg
  adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/photo.jpg
  ```

## Скрипт проверки

`scripts/check.sh` запускает adb-часть чек-листа. Нужны запущенный эмулятор с установленными
«Заметками», переменная `ANDROID_HOME` (или SDK в стандартном месте) и **bash** (на Windows —
Git Bash; на macOS запускайте именно `bash scripts/check.sh`, не `zsh`).

```bash
bash scripts/check.sh all
```

Отдельно: `manifest` (шаг 1), `closed` (шаг 2), `deeplink` (шаг 4), `provider` (шаг 7).
Каждая проверка печатает `PASS`/`FAIL` и сырой вывод adb под ним; код возврата 0 — все
PASS. На заготовке скрипт обязан падать по всем шагам безопасности: заглушки настоящие.

Что делает скрипт:

- `manifest` — скачивает установленный APK, печатает merged manifest через `apkanalyzer`,
  проверяет `exported` у каждого компонента, список экспортированных (допустимы
  `MainActivity`, `TitlesProvider` и компоненты, закрытые разрешением уровня `signature`, —
  они отмечаются `NOTE`), объявление ресивера, `FileProvider`, провайдера с
  signature-разрешением и список `uses-permission` (допустимо только `POST_NOTIFICATIONS`).
- `closed` — два `am broadcast` с действием «Готово»: явный на ресивер и в пакет приложения.
  Ресивер не должен среагировать: в logcat не должно появиться ни одной строки с тегом
  `ReminderActionReceiver`. Затем `content read` на URI FileProvider (должен быть
  `Permission Denial`).
- `deeplink` — `am start -d notes://note/{1,999999,abc,}`; после каждого смотрит logcat на
  `FATAL EXCEPTION`. Если ваш intent-filter строже и ссылки с `abc` или пустым id вообще не
  открываются (`unable to resolve Intent`), это тоже PASS; для `1` и `999999` — FAIL.
- `provider` — `content query`, `content insert`, `content delete`; все должны быть отклонены.

Подсказка к шагу 1: merged manifest — это не ваш `AndroidManifest.xml`, а его слияние с
манифестами библиотек. Посмотрите его в Android Studio (вкладка *Merged Manifest*) или
выводом `check.sh manifest`: библиотеки добавляют свои компоненты, и среди них может быть
экспортированный. Для каждого такого компонента разберитесь, кто может его вызвать. Если он
закрыт разрешением уровня `signature`, это не дыра, и скрипт его пропустит с пометкой `NOTE`.
Если компонент приложению не нужен, его можно убрать из итогового манифеста через
`tools:node="remove"`; на защите будьте готовы объяснить, зачем.

## Клиент с другим ключом

По умолчанию оба модуля подписываются debug-ключом Android Studio
(`~/.android/debug.keystore`), поэтому у вас клиент видит заголовки. Преподаватель соберёт
тот же модуль своим ключом и должен увидеть `SecurityException`. Проверьте это сами:

```bash
keytool -genkeypair -keystore other.jks -storepass other123 -keypass other123 -alias other -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Other, O=Test, C=RU"
./gradlew :client:assembleDebug -Psigning.storeFile=/абсолютный/путь/other.jks -Psigning.storePassword=other123 -Psigning.keyAlias=other -Psigning.keyPassword=other123
adb uninstall edu.androidsec.notes.client
adb install client/build/outputs/apk/debug/client-debug.apk
```

Вместо `-P` можно положить те же четыре параметра в `client/signing.properties`
(файл в `.gitignore`). Удалять установленный клиент перед установкой обязательно: подпись
изменилась, а обновление поверх старой подписи система не разрешает. Чтобы вернуть
debug-подпись — собрать без параметров и снова `uninstall` + `install`.

## Флоу работы

- Создайте свой репозиторий из ветки `starter` (fork или `git clone` + `git push` в свой
  remote) и работайте в нём. **История ветки `starter` должна сохраниться**: не пересоздавайте
  репозиторий и не схлопывайте коммиты заготовки — по истории видно, что именно сделали вы.
- Ссылку на репозиторий загрузите в Moodle до дедлайна. В README своего репозитория добавьте
  раздел с командами adb и их выводом для пунктов 3–6.
- Не коммитьте `local.properties`, ключи (`*.jks`) и `client/signing.properties`.
