# Publication checks

The initial repository was assembled from an explicit source-file allowlist. It contains the addon API, host, example, build scripts, API tests, and documentation. Original client JARs, native payloads, runtime logs, configuration files, generated compiler arguments, local dependency copies, and credential stores are excluded.

Gitleaks 8.30.1 found no secrets in the prepared source tree. Its official GitHub release archive was checked against the SHA-256 digest published in the GitHub release asset metadata before use. A separate check rejected private machine paths and local account details. Git commits use a GitHub noreply email address.

The SDK and example compiled with Java 21, and API tests passed. The build scripts do not fetch packages from registries. These checks reduce exposure; they do not prove that all possible secrets or supply-chain attacks are impossible. Future commits need the same review.
