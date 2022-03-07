# utt

utt is the **u**niversal **t**ext **t**ransformer. utt is intended for
converting between textual data representations. For example, utt can be used
to convert from JSON to YAML:

```bash
$ echo "[1, 2, 3]" | utt -i json -o yaml
---
- 1
- 2
- 3

$ 
```

Formats may be supported for input-only or output-only. You can see all
supported formats by running `utt` with no arguments.

![](https://cdn.mewna.xyz/2022/03/08/j54Tp0yOY5xGw.png)

## Getting it

A UNIX-compatible binary is automatically released here: https://github.com/queer/utt/releases.
This binary is just:
```bash
$ echo #!/usr/bin/env -S java -jar > utt && cat utt-*.jar >> utt && chmod +x utt
```

On Windows, run `mvn clean package` to create a JAR file in `target/`.

## Examples

### Convert from JSON to YAML

```bash
$ echo '{"key": [1, 2, 3]}' | utt -i json -o yaml
---
key:
- 1
- 2
- 3

$
```

### Extract keys from a JSON object

```bash
$ echo '[{"key": 1}, {"key": 2}, {"key": 3}]' | utt -i json -o json -M '_.key'
[1.0,2.0,3.0]
$
```

### Flatten a list

```bash
$ echo '[[1], [2], [3]]' | utt -i json -o json -F
[1.0,2.0,3.0]
$
```

### Encode text with base64

```bash
$ echo "this is a test" | utt -i plain -o base64
dGhpcyBpcyBhIHRlc3Q=
$
```

### Extract inner values and flatten

```bash
$ echo '{"key": [1, [2], [[3]]]}' | utt -i json -o json -M '_.key' -F
[1.0,2.0,3.0]
$ 
```