<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Commit Message Convention Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- **Live validation of the commit message you are currently writing**,
  in the IntelliJ VCS Commit dialog, against the Conventional Commits
  convention -- via a real `CheckinHandlerFactory`/`CheckinHandler`
  (confirmed signature, first use of this platform mechanism in the
  Gap Hunter Labs catalog).
- **Rules checked**: `<type>(<scope>): <description>` /
  `<type>: <description>` header shape; the 11 standard, case-sensitive
  Conventional Commits types (`feat`, `fix`, `docs`, `style`,
  `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`); subject
  line length (hard warning past 72 characters, soft suggestion between
  51-72); description must not end with a period.
- **Merge commits are always skipped** -- a git-generated message
  (`Merge branch 'x' into 'y'`, `Merge pull request #N from ...`) is
  never written by hand and is never validated.
- **Exact violation shown before committing**, not a silent
  accept/reject -- e.g. "missing type prefix", "unknown type 'xyz',
  expected one of: build, chore, ci, ...", "subject line exceeds 72
  characters (currently 80)".
- **Honest v0.1 scope**: commit footers (`BREAKING CHANGE:`, `Refs:`),
  the `!` breaking-change marker after type/scope (`feat!: ...`), and
  configurable per-team rules (custom scope whitelist, custom regex,
  team-specific length limits shared via VCS) are NOT covered --
  documented as real, deliberate gaps for a possible future version,
  not silently half-implemented.
- 100% local text validation of the commit message string -- no
  network call, no external process spawned, no PSI/file parsing
  needed.

[Unreleased]: https://github.com/GapHunterLabs/commit-message-convention-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/commit-message-convention-companion/commits/0.1.0
