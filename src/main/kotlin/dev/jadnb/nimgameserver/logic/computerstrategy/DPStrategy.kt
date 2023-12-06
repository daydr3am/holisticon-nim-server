package dev.jadnb.nimgameserver.logic.computerstrategy

import dev.jadnb.nimgameserver.entities.Game
import dev.jadnb.nimgameserver.exceptions.NotFoundException
import java.util.*

/**
 * DPStrategy implements a more winning-oriented strategy by abusing the simplicity of the game.
 * The Strategy computes all possible moves in a Dynamic Programming (DP) way and keeps
 * track of the best move at all times. This might not always be a 100% win, but
 * the algorithm always chooses the situation with the most favorable outcome for the
 * computer. It can adapt to different game rules such as different number of matches
 * or different number of allowed moves.
 */
class DPStrategy : ComputerPlayerStrategy {

    /*
    We use that we only create one instance of this strategy and keep a cache that stores
    all already computed solutions such that we only have to run the computation on the first query
     */
    private val cache: MutableMap<UUID, Solution> = mutableMapOf()

    /*
    Returns the best possible number of matches to take
     */
    override fun calculateMove(game: Game): Int {
        // Check if game state is valid
        val gameId = game.id ?: throw NotFoundException("Id of game is missing")
        val gameState = game.currentState ?: throw IllegalStateException("Game State cannot be null")

        // Get the precomputed solution or if not present, recompute
        val sol: Solution = cache.getOrPut(gameId) {
            Solution(gameState.numMatches, game.allowedMoves)
        }

        return sol.getNextMove(gameState.numMatches)
    }

    /**
     * Helper class that stores all information related to the Solution and can be kept in our cache Map
     */
    class Solution (problemSize: Int, allowedMoves: List<Int>)
    {
        // The win probability for the computer from 0 to problemSize inclusive
        private val winProbability: MutableList<Double> = MutableList(problemSize+1) { -1.0 }
        // The win probability for the player
        private val playerWinProbability: MutableList<Double> = MutableList(problemSize+1) { -1.0 }
        // the best move at each point in the game for the computer
        private val bestMove: MutableList<Int> = MutableList(problemSize+1) { -1 }

        /**
         * Returns the best number of matches to take, according to this Solution
         */
        fun getNextMove(matchesLeft: Int): Int {
            return bestMove[matchesLeft]
        }

        /*
        On initialization, we have all information needed to compute the Solution for the whole game
         */
        init {
            // Initialize the base case, if zero matches left, the game is over
            winProbability[0] = 0.0
            playerWinProbability[0] = 0.0
            bestMove[0] = 0

            // Build DP tables from bottom up
            for (i in 1..problemSize) {
                // Calculate all the moves possible for the computer if i matches are left
                val allMoves: List<Pair<Double, Int>> = allowedMoves.fold(listOf()) { acc, j ->
                    if (j > i)
                        // Taking j matches is not allowed
                        acc
                    else if (j == i)
                        // Taking j matches will lose us the game
                        acc + Pair(0.0, j)
                    else
                        // Break down to game where i-j matches are left and player is in turn
                        acc + Pair(1.0-playerWinProbability[i-j], j)
                }

                // Its computers turn, so we want to select the best possible move
                val currentBestMove = allMoves.maxBy { it.first }
                winProbability[i] = currentBestMove.first
                bestMove[i] = currentBestMove.second

                /*
                The player might not always make the best decisions. To account for that,
                we have a look at all possible moves and average the possibility that the
                player wins
                 */
                playerWinProbability[i] = allowedMoves.fold(listOf<Double>()) { acc, j ->
                    if (j > i)
                        acc
                    else if (j == i)
                        acc + 0.0
                    else
                        acc + (1-winProbability[i-j])
                }.average()

            }

        }
    }
}