package dev.jadnb.nimgameserver.logic

import dev.jadnb.nimgameserver.logic.computerstrategy.ComputerStrategyFactory
import dev.jadnb.nimgameserver.entities.Game
import dev.jadnb.nimgameserver.entities.GameRepository
import dev.jadnb.nimgameserver.entities.GameState
import dev.jadnb.nimgameserver.entities.GameStateRepository
import dev.jadnb.nimgameserver.exceptions.IllegalTurn
import dev.jadnb.nimgameserver.exceptions.InvalidInputException
import dev.jadnb.nimgameserver.exceptions.NotFoundException
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

/**
 * Implementation of the Game Service interface. Using the repositories to persist
 * created Games and GameStates
 */
@Service
class GameServiceImpl @Autowired constructor(
    private val gameRepository: GameRepository,
    private val gameStateRepository: GameStateRepository,
    private val computerStrategyFactory: ComputerStrategyFactory
) : GameService {

    /*
    Checks if the strategy is valid and then creates a game in an initial state
    according to the arguments passed to the function
     */
    override fun create(matches: Int, legalMoves: List<Int>, strategy: String): Game {
        // Check if passed strategy is valid
        if (!computerStrategyFactory.isValidStrategy(strategy)) {
            throw InvalidInputException("Strategy $strategy is not a valid strategy")
        }

        // Check if matches and moves are legal
        if (matches < 0) {
            throw InvalidInputException("Number of matches at the beginning of the game should be at least 0")
        }
        if (legalMoves.any { it < 0 } || legalMoves.isEmpty()) {
            throw InvalidInputException("allowedMoves is not valid")
        }

        // Create Game and initial GameState
        val game = createInitGame(legalMoves, strategy)
        val initialGameState = createInitGameState(matches, game)

        // Link initial GameState to Game
        game.currentState = initialGameState
        gameRepository.save(game)

        return game
    }

    /*
    This function handles the logic of actually playing the game. First checks if the
    move is actually legal. Afterward, it applies the move of the player and then
    performs the move of the computer. Returns the Game after the Computer made its move.
     */
    override fun makeMove(id: UUID, nMatches: Int): Game {
        // Look up game in repository
        val game = gameRepository.findGameById(id) ?: throw NotFoundException("No game with ID $id")
        val gameState = game.currentState ?: throw NotFoundException("Game State was null")

        // If illegal, will throw an exception to be handled by Spring
        isMoveLegal(gameState, nMatches)

        // Make players move and update the game
        val playerGameState = takeMatches(gameState, nMatches)
        setNewGameState(game, playerGameState)

        if (isGameOver(playerGameState)) {
            // The Game is finished, player looses
            setWinner(game, false)
            return game
        }

        // Computer Turn
        val strategy = computerStrategyFactory.createStrategy(game.computerPlayerStrategy)
        val computerMatches = strategy.calculateMove(game)

        // Perform the Computer Move
        val computerGameState = takeMatches(playerGameState, computerMatches)
        setNewGameState(game, computerGameState)

        if (isGameOver(computerGameState))
        {
            // The Game is finished, the computer looses
            setWinner(game, true)
        }

        gameRepository.save(game)
        return game
    }

    override fun getGame(id: UUID): Game? =
        gameRepository.findGameById(id)


    /*
    Private helper functions
     */

    /**
     * Creates the initial Game object and persist it
     * @param legalMoves Number of matches that can be taken each turn in this game
     * @param strategy The strategy the computer player is supposed to use
     * @return A new game object with the id field populated
     */
    private fun createInitGame(legalMoves: List<Int>, strategy: String): Game {
        val game = Game(null, null, mutableListOf(), legalMoves.toMutableList(), strategy)
        gameRepository.save(game)
        return game
    }

    /**
     * Creates the first game state and persists it
     * @param matches Number of matches to start the game with
     * @param game the Game object this state belongs to
     * @return A new GameState with the id field populated
     */
    private fun createInitGameState(matches: Int, game: Game): GameState {
        val initialGameState = GameState(null, matches, game, 0, true)
        gameStateRepository.save(initialGameState)
        return initialGameState
    }

    /**
     * Checks if taking nMatches in the current games state is allowed
     * @param gameState The current game state
     * @param nMatches the number of matches intended to be taken.
     * @throws IllegalTurn if a condition is violated
     */
    private fun isMoveLegal(gameState: GameState, nMatches: Int): Unit {
        if (gameState.numMatches == 0)
            throw IllegalTurn("Invalid Turn, this game is already finished")
        if (gameState.numMatches < nMatches)
            throw IllegalTurn("Invalid Turn, trying to take more matches than left on the heap")
        if (nMatches !in gameState.game.allowedMoves)
            throw IllegalTurn("Invalid Turn, Taking $nMatches is not allowed")
    }

    /**
     * Creates the new game state that results if we take nMatches
     * in the currentGameState
     * @param currentGameState the actual game state in which we want to take nMatches
     * @param nMatches the number of matches to be taken
     * @return the new GameState
     */
    private fun takeMatches(currentGameState: GameState, nMatches: Int): GameState {
        val game = currentGameState.game
        // Create a new GameState object by subtracting the matches, add one turn and change the player who is next
        val newGameState = GameState(null,
            currentGameState.numMatches - nMatches,
            game,
            currentGameState.turn + 1,
            !currentGameState.playersTurn)
        gameStateRepository.save(newGameState)
        return newGameState
    }

    /**
     * Update the Game to use the game state provided as argument
     * @param game The game that is supposed to be updated
     * @param gameState the new game state
     */
    private fun setNewGameState(game: Game, gameState: GameState): Unit {
        game.currentState = gameState
        game.allTurns.add(gameState)
    }

    /**
     * Checks if the GameState is a finished game.
     * @param gameState The game State
     * @return true if the game is over
     */
    private fun isGameOver(gameState: GameState): Boolean {
        return gameState.numMatches == 0
    }

    /**
     * Sets the winner field of Game
     * @param game The game where the winner field should be updated
     * @param playerHasWon Pass true if the player won, false otherwise
     */
    private fun setWinner(game: Game, playerHasWon: Boolean): Unit {
        game.winner = if (playerHasWon) "Player" else "Computer"
        gameRepository.save(game)
    }
}