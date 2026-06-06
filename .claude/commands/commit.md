Commit the current changes yourself, following this repo's take-home workflow rule. Do not wait for me to run anything.

The commit message MUST be my exact prompt for this iteration — verbatim, character for character, including newlines, quotes, and any special characters. Do not summarize, rephrase, shorten, fix typos, or add a prefix.

Steps:
1. `git add -A`
2. Write my exact prompt to a temp file (e.g. `.git/COMMIT_PROMPT.txt`) and commit with `git commit -F .git/COMMIT_PROMPT.txt`. Use `-F` (a file), never `-m`, so quotes and newlines survive intact.
3. Show me `git log --oneline -1` and the full message body so I can confirm it matches my prompt exactly.

Do not append a "Generated with Claude Code" footer, co-author trailers, or any other text. The message is the prompt, nothing more.

If this is the Step 2 (manual improvement) phase instead, tell me, and use a single commit whose message lists what was improved and why plus the major issues left unfixed.
