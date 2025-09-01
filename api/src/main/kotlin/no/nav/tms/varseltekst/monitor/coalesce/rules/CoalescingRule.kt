package no.nav.tms.varseltekst.monitor.coalesce.rules

import java.time.LocalDateTime

interface CoalescingRule {
    val name: String get() = this::class.simpleName!!
    val description: String

    fun ruleApplies(text: String): Boolean

    fun applyRule(text: String): String
}

data class RuleDto(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: LocalDateTime
)

data class CoalescingRuleWrapper(
    val definition: CoalescingRule,
    val dto: RuleDto
) {
    val ruleId get() = dto.id
}
