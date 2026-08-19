package dev.gaphunter.commitmessageconventioncompanion.inspection

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory

/**
 * Registers [ConventionalCommitCheckinHandler] for every VCS commit
 * dialog (svn/git/etc. -- CheckinHandlerFactory is VCS-agnostic by
 * design, same as the platform's own built-in handlers).
 *
 * Signature confirmed via `javap` against this exact IDE version's
 * cached app.jar (2025.2.6.2, 2026-08-19) -- see SDK_GOTCHAS.md for
 * the full bytecode dump. `createHandler` is abstract and Java (not
 * Kotlin), so there is no property-vs-function ambiguity here.
 */
class ConventionalCommitCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return ConventionalCommitCheckinHandler(panel)
    }
}

/**
 * Live-validates the commit message against Conventional Commits
 * (see [ConventionalCommitValidator] for the exact rules and honest
 * v0.1 scope) when the user presses Commit, and shows the exact
 * violation(s) before letting the commit through -- rather than
 * silently accepting or rejecting.
 *
 * `beforeCheckin()` (no-arg overload, confirmed via `javap`) runs
 * synchronously on the EDT right before the commit is performed --
 * validation here is pure string parsing (no PSI, no I/O), so no
 * background thread is needed, unlike heavier inspections elsewhere
 * in this catalog that must offload real work via
 * `executeOnPooledThread`.
 */
class ConventionalCommitCheckinHandler(
    private val panel: CheckinProjectPanel,
) : CheckinHandler() {

    override fun beforeCheckin(): ReturnResult {
        val commitMessage = panel.commitMessage
        val results = ConventionalCommitValidator.validate(commitMessage)

        val warnings = results.filterIsInstance<ConventionalCommitValidator.ValidationResult.Warning>()
        val suggestions = results.filterIsInstance<ConventionalCommitValidator.ValidationResult.Suggestion>()

        if (warnings.isEmpty() && suggestions.isEmpty()) {
            return ReturnResult.COMMIT
        }

        val lines = mutableListOf<String>()
        if (warnings.isNotEmpty()) {
            lines += "This commit message does not follow Conventional Commits:"
            warnings.forEach { lines += "  • ${it.message}" }
        }
        if (suggestions.isNotEmpty()) {
            if (lines.isNotEmpty()) lines += ""
            lines += "Suggestions:"
            suggestions.forEach { lines += "  • ${it.message}" }
        }
        lines += ""
        lines += "Commit anyway?"

        // Warning-level violations block by default (require an explicit
        // "commit anyway"); a message with only Suggestion-level results
        // (no Warning) commits straight through -- the soft heads-up is
        // informational only, never blocking.
        if (warnings.isEmpty()) {
            return ReturnResult.COMMIT
        }

        val choice = Messages.showYesNoDialog(
            panel.project,
            lines.joinToString("\n"),
            "Commit Message Convention Companion",
            "Commit Anyway",
            "Cancel",
            Messages.getWarningIcon(),
        )

        return if (choice == Messages.YES) ReturnResult.COMMIT else ReturnResult.CANCEL
    }
}
