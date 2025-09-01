package no.nav.tms.varseltekst.monitor.coalesce

import no.nav.tms.varseltekst.monitor.coalesce.rules.CoalescingRule
import no.nav.tms.varseltekst.monitor.coalesce.rules.CoalescingRuleWrapper

class CoalescingService private constructor() {

    private lateinit var coalescingRules: List<CoalescingRuleWrapper>
    private var initializer: Initializer? = null

    fun initialize(): CoalescingService {
        coalescingRules = initializer
            ?.initializeRules()
            ?: throw IllegalStateException("Cannot initialize CoalescingService without an initializer")

        return this
    }

    fun coalesce(tekst: String): CoalescingResult {

        val applicableRules = coalescingRules.filter { it.definition.ruleApplies(tekst) }
            .sortedWith(compareBy({ it.dto.createdAt }, { it.dto.id }))
            .map { it.definition }

        var coalescedTekst = tekst

        applicableRules.forEach { rule ->
            coalescedTekst = rule.applyRule(coalescedTekst)
        }

        return CoalescingResult(tekst, coalescedTekst, applicableRules)
    }

    fun coalesce(tekst: String, ruleId: Int): CoalescingResult {

        val ruleDefinition = coalescingRules.find { it.ruleId == ruleId }
            ?.definition
            ?: throw IllegalStateException("Did not find rule with id $ruleId")

        return if (ruleDefinition.ruleApplies(tekst)){
            val coalescedTekst = ruleDefinition.applyRule(tekst)

            CoalescingResult(tekst, coalescedTekst, listOf(ruleDefinition))
        } else {
            CoalescingResult.untouched(tekst)
        }
    }

    companion object {
        fun uninitialized(
            coalescingRepository: CoalescingRepository,
            backlogRepository: BacklogRepository,
            rules: List<CoalescingRule>
        ) = CoalescingService().also {
            it.initializer = Initializer(
                coalescingRepository, backlogRepository, rules
            )
        }

        fun initialized(rules: List<CoalescingRuleWrapper>) = CoalescingService().also {
            it.coalescingRules = rules
        }
    }

    private class Initializer(
        private val coalescingRepository: CoalescingRepository,
        private val backlogRepository: BacklogRepository,
        private val rules: List<CoalescingRule>
    ) {
        fun initializeRules(): List<CoalescingRuleWrapper> {
            val newRules = findNewRules(coalescingRepository, rules)

            if (newRules.isNotEmpty()) {
                backlogRepository.persistRulesAndBacklog(newRules)
            }

            return wrapDefinitionsWithDtos(coalescingRepository, rules)
        }

        private fun findNewRules(repository: CoalescingRepository, rules: List<CoalescingRule>): List<CoalescingRule> {
            val existingNames = repository.getCoalescingRules().map { it.name }

            return rules.filter { !existingNames.contains(it.name) }
        }

        private fun wrapDefinitionsWithDtos(repository: CoalescingRepository, rules: List<CoalescingRule>): List<CoalescingRuleWrapper> {
            val ruleDtos = repository.getCoalescingRules()

            return rules.map { rule ->
                val dto = ruleDtos.find { it.name == rule.name }

                if (dto != null) {
                    CoalescingRuleWrapper(rule, dto)
                } else {
                    throw IllegalStateException("Noe gikk galt ved initialisering. Fant ikke regel ${rule.name} i basen.")
                }
            }
        }
    }

}
