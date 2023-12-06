package dev.jadnb.nimgameserver.logic.computerstrategy

/**
 * Interface for a strategy factory that allows to get a corresponding ComputerPlayerStrategy object
 * given a string identifier.
 */
interface ComputerStrategyFactory {
    /**
     * Returns ComputerPlayerStrategy object dependent on the descriptor
     * passed to this function
     * @param descriptor The descriptor of the strategy
     * @return An ComputerPlayerStrategy object
     * @throws RuntimeException if the descriptor is not understood
     */
    fun createStrategy(descriptor: String): ComputerPlayerStrategy

    /**
     * Allows checking if this factory understands the descriptor,
     * a more graceful way to check if a descriptor is valid than the exception in
     * the createStrategy function
     * @param descriptor The descriptor of the strategy
     * @return true if createStrategy does not fail when called with descriptor
     */
    fun isValidStrategy(descriptor: String): Boolean
}