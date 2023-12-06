package dev.jadnb.nimgameserver

import dev.jadnb.nimgameserver.entities.Game
import dev.jadnb.nimgameserver.entities.GameRepository
import dev.jadnb.nimgameserver.entities.GameState
import dev.jadnb.nimgameserver.entities.GameStateRepository
import dev.jadnb.nimgameserver.exceptions.IllegalTurn
import dev.jadnb.nimgameserver.exceptions.InvalidInputException
import dev.jadnb.nimgameserver.logic.GameService
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@Transactional
@SpringBootTest
class GameServiceTest @Autowired constructor(
    val gameRepository: GameRepository,
    val gameStateRepository: GameStateRepository,
    val gameService: GameService,
){

    /**
     * Will set up and persist a game and a gamestate according to the parameters to this function
     * @param nMatches Number of matches left
     * @param allowedMoves Moves allowed to make
     */
    private fun setupTestGame(nMatches: Int, allowedMoves: MutableList<Int>): Pair<Game, GameState> {
        val game = Game(null, null, mutableListOf(), allowedMoves)
        gameRepository.save(game)
        val gameState = GameState(null, nMatches, game, 0, true)
        gameStateRepository.save(gameState)
        game.currentState = gameState
        gameRepository.save(game)
        return Pair(game, gameState)
    }

    @Test
    fun `creating a new game should be persisted`()
    {
        val game = gameService.create()
        assertThat(game.id).isNotNull
        assertThat(gameRepository.findGameById(game.id!!)).isNotNull
    }

    @Test
    fun `Custom parameter should be persisted`()
    {
        val game = gameService.create(14, listOf(1,2,3,4))
        assertThat(game.id).isNotNull()
        val repoGame = gameRepository.findGameById(game.id!!)!!
        assertThat(repoGame.allowedMoves.size).isEqualTo(4)
        assertThat(repoGame.currentState).isNotNull
        assertThat(repoGame.currentState!!.numMatches).isEqualTo(14)
    }

    @Test
    fun `Invalid strategy should throw exception`()
    {
        assertThrows<InvalidInputException> { gameService.create(13, listOf(1,2,3), "Invalid") }
    }

    @Test
    fun `Valid strategy should be set in object`()
    {
        val game = gameService.create(13, listOf(1,2,3), "RANDOM")
        assertThat(game.id).isNotNull()
        assertThat(game.computerPlayerStrategy).isEqualTo("RANDOM")
    }

    @Test
    fun `A finished game should not accept a new move`()
    {
        lateinit var game: Game
        // Set up finished game
        setupTestGame(0, mutableListOf()).let {
            game = it.first
        }

        // Check that game actually got persisted
        assertThat(game.id).isNotNull()

        // Test
        assertThrows<IllegalTurn> {
            gameService.makeMove(game.id!!, 1)
        }
    }

    @Test
    fun `should not allow to take illegal number of matches`()
    {
        lateinit var game: Game
        // Setup game with 10 matches and [1,2,3,11] as allowed moves
        setupTestGame(10, mutableListOf(1,2,3,11)).let {
            game = it.first
        }
        // Check that game got persisted
        assertThat(game.id).isNotNull()

        // Test that we cannot take 4 matches
        val exception4Matches = assertThrows<IllegalTurn> {
            gameService.makeMove(game.id!!, 4)
        }
        assertThat(exception4Matches.message).isEqualTo("Invalid Turn, Taking 4 is not allowed")

        // Test that we cannot take more matches than available
        val exceptionNotEnoughMatches =  assertThrows<IllegalTurn> {
            gameService.makeMove(game.id!!, 11)
        }

        assertThat(exceptionNotEnoughMatches.message).isEqualTo("Invalid Turn, trying to take more matches than left on the heap")
    }

    @Test
    fun `should modify game and make computer turn`()
    {
        lateinit var game: Game
        // Set up game with 10 matches and allow only taking one match per turn
        setupTestGame(10, mutableListOf(1)).let {
            game = it.first
        }

        // Do a move
        gameService.makeMove(game.id!!, 1)

        game = gameRepository.findGameById(game.id!!)!!
        assertThat(game.currentState).isNotNull
        assertThat(game.currentState!!.numMatches).isEqualTo(8)
    }

    @Test
    fun `should set winner correctly when computer wins`()
    {
        lateinit var game: Game
        setupTestGame(1, mutableListOf(1)).let {
            game = it.first
        }

        // Before the move it should be none
        assertThat(game.winner).isEqualTo("none")

        gameService.makeMove(game.id!!, 1)

        // Check if field is updated
        game = gameRepository.findGameById(game.id!!)!!
        assertThat(game.winner).isEqualTo("Computer")
    }

    @Test
    fun `should set winner correctly when player wins`()
    {
        lateinit var game: Game
        setupTestGame(2, mutableListOf(1)).let {
            game = it.first
        }

        assertThat(game.winner).isEqualTo("none")

        gameService.makeMove(game.id!!, 1)

        // Check if field is updated correctly
        game = gameRepository.findGameById(game.id!!)!!
        assertThat(game.winner).isEqualTo("Player")

    }
}