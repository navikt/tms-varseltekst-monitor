package no.nav.tms.varseltekst.monitor.coalesce.rules

object NumberCensorRule: CoalescingRule {
    override val description = "Replaces all numbers with '***'"

    private val numberRegex = "[0-9]+".toRegex()

    override fun ruleApplies(text: String) = numberRegex.containsMatchIn(text)

    override fun applyRule(text: String): String {
        return text.replace(numberRegex, "***")
    }
}

object GreetingCensorRule: CoalescingRule {
    override val description = "Replaces names for specific formats with <name>"

    private val messagePattern = "^Hello, ([\\w\\s]+)!".toRegex()

    override fun ruleApplies(text: String) = messagePattern.containsMatchIn(text)

    override fun applyRule(text: String): String {
        val name = messagePattern.find(text)?.destructured?.component1()!!

        return text.replace(name, "<name>")
    }
}
