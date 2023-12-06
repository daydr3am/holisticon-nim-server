package dev.jadnb.nimgameserver.logic.computerstrategy

import dev.jadnb.nimgameserver.entities.Game

/**
 * Simple strategy that just selects a random number of matches. Adheres to the
 * rules of the game by filtering for illegal options
 */
class RandomComputerStrategy : ComputerPlayerStrategy {
    override fun calculateMove(game: Game): Int {
        val currentState = game.currentState ?:
            throw IllegalStateException("Computer Strategy should not be called when there is no current State")
        return game.allowedMoves.filter { it <= currentState.numMatches }.random()
    }
}