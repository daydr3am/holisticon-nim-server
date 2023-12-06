package dev.jadnb.nimgameserver.logic.computerstrategy

import org.springframework.stereotype.Component

/**
 * Straightforward implementation of the ComputerStrategyFactory interface. However,
 * this factory acts more as a Flyweight instancing the strategies and returning the
 * same objects upon request. This has the advantage that we can store information that is only
 * related to the strategy directly in the Strategy object across multiple invocations
 */
@Component
class ComputerStrategyFactoryImpl : ComputerStrategyFactory
{
    private val randomPlayer = RandomComputerStrategy()
    private val dpPlayer = DPStrategy()

    private val strategies = mapOf(
        "RANDOM" to randomPlayer,
        "DP" to dpPlayer,
    )

    /*
     Returns one of the strategy objects based on the descriptor. If no of the
     strategies matches the descriptors, the method throws a RuntimeException
     */
    override fun createStrategy(descriptor: String): ComputerPlayerStrategy {
        return strategies[descriptor] ?: throw RuntimeException("No Strategy named $descriptor")
    }

    /*
    Checks if the descriptor is a valid strategy for this factory. In this case, check
    if the descriptor is in the static list of supported strategies
     */
    override fun isValidStrategy(descriptor: String): Boolean {
        return descriptor in listOf("RANDOM", "DP")
    }

}