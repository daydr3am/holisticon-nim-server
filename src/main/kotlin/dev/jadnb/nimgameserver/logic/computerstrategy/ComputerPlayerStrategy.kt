package dev.jadnb.nimgameserver.logic.computerstrategy

import dev.jadnb.nimgameserver.entities.Game

/**
 * An interface to be used in a strategy pattern to allow for multiple
 * strategies for the computer player.
 */
interface ComputerPlayerStrategy {
    /**
     * Given a game, this function returns how many matches should be taken
     * by the computer. It is the responsibility of this function to adhere to the
     * game rules such as allowed number of matches to take or to not take
     * more matches than left in the heap.
     */
    fun calculateMove(game: Game): Int
}