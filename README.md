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

At the time of writing, supported formats are:
- JSON
- XML
- CSV
- YAML
- Java Properties
- TOML
- Base 64
- Plain Text
- ROT13
- PyON (Python Object Notation)

![](https://cdn.mewna.xyz/2022/03/13/Je8X2G1iZq1D4.png)

Note: "universal" is a *big* claim, and I understand that utt isn't truly
universal. However, it is an active goal to work on supporting more and more
formats and transformations to make utt as close to truly universal as
possible.

## Getting it

A UNIX-compatible binary is automatically released here: https://github.com/queer/utt/releases.
This binary is just:
```bash
$ echo "#!/usr/bin/env -S java -jar" > utt && cat utt-*.jar >> utt && chmod +x utt
```

On Windows, run `mvn clean package` to create a JAR file in `target/`.

### Why is it so big???

Unfortunately, supporting everything under the sun requires dragging in a
pretty ridiculous set of libraries. For example, to support Python dict
notation, utt drags in an entire Python implementation via `jython-standalone`.

## What and why

utt (case-sensitive) is a tool for converting between textual data formats. utt
was originally written for a project that involved a lot of annoying
conversions, to the point where one-off scripts wouldn't be enough. 

utt makes some tradeoffs in the name of functionality. For example, utt does
*not* process data in a streaming manner, but rather loads the entire dataset
into memory before processing. In exchange, utt is more flexible in what it
accepts as input and output formats. For example, utt can only output to CSV by
[iterating over the input data to determine the output schema](https://github.com/queer/utt/blob/ffb886a64ecc24cf1320cf8adf5ec02cd9ad8221/src/main/java/gg/amy/utt/transform/impl/CsvTransformer.java#L34-L89).
Similarly, utt's mapping functionality relies on the GraalVM Polyglot API,
which has a high (~500ms) startup time.

While [pandoc](https://pandoc.org/) focuses more on converting between human
formats like markdown and HTML, utt is more focused on converting between data
formats understood by computers.

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

### Extract a JSON array from an XML object

```bash
$ echo "<a><b>c</b><b>c</b><b>c</b><b>c</b></a>" | utt -i xml -o json -M '$.b'
["c","c","c","c"]
$
```

### Apply a map function to the values of a list

```bash
$ echo "[1,2,3]" | utt -i json -o yaml -M "_ * 2"
---
- 2.0
- 4.0
- 6.0

$
```
