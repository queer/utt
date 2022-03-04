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

![](https://cdn.mewna.xyz/2022/03/05/4rqR6k7iejFWo.png)