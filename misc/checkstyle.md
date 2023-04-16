# checkstyle

## install

`curl --create-dirs -Lo ~/.local/share/checkstyle/checkstyle-all.jar https://github.com/checkstyle/checkstyle/releases/download/checkstyle-10.3.4/checkstyle-10.3.4-all.jar`

```bash
tee ~/.local/bin/checkstyle <<'EOF' >/dev/null
#!/usr/bin/env bash

java -cp ~/.local/share/checkstyle/checkstyle-all.jar com.puppycrawl.tools.checkstyle.Main "$@"
EOF
```

## usage

`checkstyle -c misc/checkstyle.xml $(find src -iname '*.java')`
