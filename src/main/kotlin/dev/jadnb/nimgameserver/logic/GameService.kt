package dev.jadnb.nimgameserver.logic

import dev.jadnb.nimgameserver.entities.Game
import java.util.UUID

/**
 * The GameService provides the logic of the game through a simple interface.
 * Furthermore, the GameService takes care of persisting the data of a game
 * and makes handles validation of inputs to ensure that the input adheres to the rules
 * of the game
 *
 */
interface GameService {
    /**
     * Creates a new game. The parameters can be adjusted to play the game with different
     * parameters, the default values for those parameters correspond to the default rules of the game
     * @param matches number of matches at the beginning of the game
     * @param legalMoves the possible number of matches that can be taken each turn
     * @param strategy the strategy that the computer player should use
     * @return A new game object
     */
    fun create(matches: Int = 13, legalMoves: List<Int> = listOf(1, 2, 3), strategy: String = "DP"): Game

    /**
     * Takes a UUID and then takes nMatches from the game which corresponds to the UUID. Checks if it
     * is allowed to take nMatches and fails if nMatches is not a valid input
     * @param id The uuid of the game as returned by the create function
     * @param nMatches The number of matches that shall be taken of the heap of the game
     */
    fun makeMove(id: UUID, nMatches: Int): Game

    /**
     * Loads a game given the id.
     * @param id The uuid of the game
     * @return a game instance or null if no instance with this id is found
     */
    fun getGame(id: UUID): Game?
}