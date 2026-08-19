package dev.gaphunter.commitmessageconventioncompanion

import dev.gaphunter.commitmessageconventioncompanion.inspection.ConventionalCommitValidator
import dev.gaphunter.commitmessageconventioncompanion.inspection.ConventionalCommitValidator.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConventionalCommitValidatorTest {

    private fun warnings(message: String) =
        ConventionalCommitValidator.validate(message).filterIsInstance<ValidationResult.Warning>()

    private fun suggestions(message: String) =
        ConventionalCommitValidator.validate(message).filterIsInstance<ValidationResult.Suggestion>()

    @Test
    fun `a valid feat message with no scope produces no warnings`() {
        val results = ConventionalCommitValidator.validate("feat: add login form validation")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `a valid fix message with a scope produces no warnings`() {
        val results = ConventionalCommitValidator.validate("fix(auth): correct token refresh race condition")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `a message missing the type prefix triggers a missing type warning`() {
        val results = warnings("add login form validation")
        assertEquals(1, results.size)
        assertTrue(results[0].message.contains("missing type prefix"))
    }

    @Test
    fun `a message with an unrecognized type triggers an unknown type warning naming the type`() {
        val results = warnings("feature: add login form validation")
        assertEquals(1, results.size)
        assertTrue(results[0].message.contains("unknown type 'feature'"))
    }

    @Test
    fun `type checking is case-sensitive per the real Conventional Commits spec`() {
        val results = warnings("Feat: add login form validation")
        assertEquals(1, results.size)
        assertTrue(results[0].message.contains("unknown type 'Feat'"))
    }

    @Test
    fun `a subject line past the hard limit triggers a length warning with the real character count`() {
        val longDescription = "a".repeat(80)
        val message = "feat: $longDescription"
        val results = warnings(message)
        assertEquals(1, results.size)
        assertTrue(results[0].message.contains("exceeds 72 characters"))
        assertTrue(results[0].message.contains("currently ${message.length}"))
    }

    @Test
    fun `a subject line between 51 and 72 characters is a soft suggestion, not a warning`() {
        // "feat: " (6) + 60 a's = 66 chars total -- above 50, at/under 72.
        val message = "feat: " + "a".repeat(60)
        assertTrue(warnings(message).isEmpty())
        val suggestionResults = suggestions(message)
        assertEquals(1, suggestionResults.size)
        assertTrue(suggestionResults[0].message.contains("longer than the ideal 50"))
    }

    @Test
    fun `a description ending with a period triggers a trailing period warning`() {
        val results = warnings("fix: correct token refresh race condition.")
        assertEquals(1, results.size)
        assertTrue(results[0].message.contains("should not end with a period"))
    }

    @Test
    fun `a merge commit message is always skipped, never validated`() {
        val results = ConventionalCommitValidator.validate("Merge branch 'feature/login' into 'main'")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `a merge pull request message is also skipped`() {
        val results = ConventionalCommitValidator.validate(
            "Merge pull request #42 from acme-corp/fix-timeout",
        )
        assertTrue(results.isEmpty())
    }

    @Test
    fun `scope is optional -- present and absent are both valid when the rest of the format is correct`() {
        assertTrue(ConventionalCommitValidator.validate("chore: bump dependency versions").isEmpty())
        assertTrue(
            ConventionalCommitValidator.validate("chore(deps): bump dependency versions").isEmpty(),
        )
    }

    @Test
    fun `an empty commit message produces no results`() {
        assertTrue(ConventionalCommitValidator.validate("").isEmpty())
        assertTrue(ConventionalCommitValidator.validate("   ").isEmpty())
    }

    @Test
    fun `a message with multiple violations reports all of them at once`() {
        val results = warnings("Bad message that is quite long for no good reason at all today.")
        // Missing type prefix AND ends with a period -- both real violations.
        assertTrue(results.size >= 2)
        assertTrue(results.any { it.message.contains("missing type prefix") })
        assertTrue(results.any { it.message.contains("should not end with a period") })
    }

    @Test
    fun `all 11 standard Conventional Commits types are accepted`() {
        val types = listOf(
            "feat", "fix", "docs", "style", "refactor",
            "perf", "test", "build", "ci", "chore", "revert",
        )
        types.forEach { type ->
            val results = warnings("$type: a short valid description")
            assertTrue("type '$type' should be accepted but got: $results", results.isEmpty())
        }
    }
}
