# google-java-format

## install

Arch Linux: install `google-java-format` from AUR

Others:

1. Download `google-java-format-x.x.x-all-deps.jar` from [github](https://github.com/google/google-java-format/releases).

```
curl --create-dirs -Lo ~/.local/share/java/google-java-format/google-java-format.jar <url>
```

2. Create (executable) startup script in your PATH (i.e. `~/.local/bin/google-java-format`)

Java 17 or Later:
```java
#!/usr/bin/env bash

exec java \
  -jar "/home/$USER/.local/share/java/google-java-format/google-java-format.jar" "$@"
```

Pre Java 17
```java
#!/usr/bin/env bash

exec java \
  --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
  --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
  --add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
  --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
  --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
  -jar "/home/$USER/.local/share/java/google-java-format/google-java-format.jar" "$@"
```

3. (optionally) Create Vim command (or keymap) in vimrc:

```
command! Format %!google-java-format -
" OR or and
nnoremap <space>bf <cmd>%!google-java-format -<cr>
```

## usage

- Enter `:Format` in vim to format current buffer (if command was setup)
- Run `./format-java.sh`
