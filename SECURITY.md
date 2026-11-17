# Security and dependency handling

Never commit license keys, Minecraft access tokens, session data, personal logs, native client payloads, local dependency folders, or generated compiler argument files. `.gitignore` excludes common sensitive and generated files; ignore rules do not sanitize files already committed.

Builds use local inputs and do not fetch third-party packages. No API key is required to build or use this addon API. The Python host build uses only Python's standard library. Java annotation processing is disabled in all build commands.

Addon JARs execute arbitrary Java code with Minecraft's permissions. The host isolates registration failures and disables modules whose tick callbacks throw; it is not a security sandbox. Review addon sources and dependency origins before running them.

For future changes, run a trusted secret scanner against both the source tree and Git history before pushing. Do not paste secrets into an issue or pull request. If a real credential is ever exposed, revoke or rotate it; deleting the text alone is insufficient.

Use the exact owner/repository named in README. There is no official npm or PyPI package for this project. New dependency names require verification against the upstream project's own documentation; do not assume a package exists because generated code names it.
