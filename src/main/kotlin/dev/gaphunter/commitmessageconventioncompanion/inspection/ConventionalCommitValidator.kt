package dev.gaphunter.commitmessageconventioncompanion.inspection

/**
 * Pure text validator for the Conventional Commits convention
 * (https://www.conventionalcommits.org/), v0.1 scope only.
 *
 * Covered: type prefix + optional scope + description shape, the
 * standard type vocabulary (case-sensitive), subject line length,
 * and "no trailing period" -- plus explicit skip for merge commits.
 *
 * NOT covered in v0.1, on purpose (documented honestly, not silently
 * half-handled): commit footers (BREAKING CHANGE:, Refs:, etc.), the
 * `!` breaking-change marker after type/scope (e.g. `feat!: ...`),
 * and configurable per-team rules (custom scope whitelist, custom
 * regex, team-specific length limits shared via VCS) -- all of that
 * is a distinct, larger feature intentionally deferred to a future
 * version.
 */
object ConventionalCommitValidator {

    /** Case-sensitive per the real Conventional Commits spec. */
    val STANDARD_TYPES: Set<String> = setOf(
        "feat", "fix", "docs", "style", "refactor",
        "perf", "test", "build", "ci", "chore", "revert",
    )

    /** Common convention: <=50 chars is "ideal" for the subject line. */
    const val SOFT_LENGTH_LIMIT = 50

    /**
     * Common convention: <=72 chars is the widely-used hard cap (keeps
     * `git log --oneline` and GitHub's UI readable without wrapping).
     * Chosen as the real WARNING threshold; 51-72 only gets a softer
     * suggestion (see [ValidationResult.Suggestion]), never a hard
     * warning -- documented choice, not an arbitrary number.
     */
    const val HARD_LENGTH_LIMIT = 72

    // Matches "type(scope): description" or "type: description".
    // Scope, if present, must be non-empty inside the parentheses.
    private val SUBJECT_PATTERN = Regex("""^([A-Za-z]+)(\(([^()]+)\))?: (.*)$""")

    private val MERGE_COMMIT_PATTERN = Regex(
        """^Merge (branch|pull request|remote-tracking branch) .+""",
    )

    sealed class ValidationResult {
        /** Message is either a merge commit (skipped) or fully valid. */
        object Ok : ValidationResult()

        /** A real convention violation -- must block/confirm the commit. */
        data class Warning(val message: String) : ValidationResult()

        /** A softer, non-blocking heads-up (e.g. 51-72 char subject). */
        data class Suggestion(val message: String) : ValidationResult()
    }

    /**
     * Returns every violation/suggestion found (empty list = fully
     * valid). A merge commit always returns an empty list -- it is
     * never written by hand, so it is never validated.
     */
    fun validate(commitMessage: String): List<ValidationResult> {
        val trimmed = commitMessage.trim()
        if (trimmed.isEmpty()) {
            return emptyList()
        }

        val subjectLine = trimmed.lineSequence().first()

        if (isMergeCommit(subjectLine)) {
            return emptyList()
        }

        val results = mutableListOf<ValidationResult>()

        val match = SUBJECT_PATTERN.matchEntire(subjectLine)
        if (match == null) {
            results += ValidationResult.Warning(
                if (!subjectLine.contains(':')) {
                    "missing type prefix -- expected \"type: description\" or " +
                        "\"type(scope): description\" (e.g. \"feat: add login form\")"
                } else {
                    "malformed header -- expected \"type: description\" or " +
                        "\"type(scope): description\", got \"$subjectLine\""
                },
            )
            // Header shape is broken -- still check length/period below
            // using the raw subject line, so a user gets every real
            // problem at once instead of one at a time across re-edits.
            results += checkLength(subjectLine)
            results += checkTrailingPeriod(subjectLine)
            return results
        }

        val type = match.groupValues[1]
        val description = match.groupValues[4]

        if (type !in STANDARD_TYPES) {
            results += ValidationResult.Warning(
                "unknown type '$type', expected one of: " +
                    STANDARD_TYPES.sorted().joinToString(", "),
            )
        }

        results += checkLength(subjectLine)
        results += checkTrailingPeriod(description.ifEmpty { subjectLine })

        return results
    }

    private fun checkLength(subjectLine: String): List<ValidationResult> {
        val length = subjectLine.length
        return when {
            length > HARD_LENGTH_LIMIT -> listOf(
                ValidationResult.Warning(
                    "subject line exceeds $HARD_LENGTH_LIMIT characters (currently $length)",
                ),
            )
            length > SOFT_LENGTH_LIMIT -> listOf(
                ValidationResult.Suggestion(
                    "subject line is longer than the ideal $SOFT_LENGTH_LIMIT characters " +
                        "(currently $length, still under the $HARD_LENGTH_LIMIT hard limit)",
                ),
            )
            else -> emptyList()
        }
    }

    private fun checkTrailingPeriod(description: String): List<ValidationResult> {
        return if (description.endsWith(".")) {
            listOf(ValidationResult.Warning("subject line should not end with a period"))
        } else {
            emptyList()
        }
    }

    private fun isMergeCommit(subjectLine: String): Boolean {
        return MERGE_COMMIT_PATTERN.containsMatchIn(subjectLine)
    }
}
