package dev.jadnb.nimgameserver

import dev.jadnb.nimgameserver.entities.Game
import dev.jadnb.nimgameserver.entities.GameState
import dev.jadnb.nimgameserver.logic.computerstrategy.ComputerPlayerStrategy
import dev.jadnb.nimgameserver.logic.computerstrategy.ComputerStrategyFactory
import dev.jadnb.nimgameserver.logic.computerstrategy.DPStrategy
import dev.jadnb.nimgameserver.logic.computerstrategy.RandomComputerStrategy
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import java.util.function.Predicate

@SpringBootTest
class StrategyTest @Autowired constructor(
    private val computerStrategyFactory: ComputerStrategyFactory
) {

    val randomStrat = computerStrategyFactory.createStrategy("RANDOM")
    val dpStrat = computerStrategyFactory.createStrategy("DP")

    private fun createTestGame(nMatches: Int, allowedMoves: MutableList<Int>): Game {
        val newGame = Game(UUID.randomUUID(), null, mutableListOf(), allowedMoves)
        val gameState = GameState(UUID.randomUUID(), nMatches, newGame, 0, true)
        newGame.currentState = gameState

        return newGame
    }

    @Test
    fun `factory should return correct instances`()
    {
        assertThat(computerStrategyFactory.createStrategy("RANDOM")).isInstanceOf(RandomComputerStrategy::class.java)
        assertThat(computerStrategyFactory.createStrategy("DP")).isInstanceOf(DPStrategy::class.java)
        assertThat(computerStrategyFactory.isValidStrategy("RANDOM")).isTrue()
        assertThat(computerStrategyFactory.isValidStrategy("DP")).isTrue()
    }

    @Test
    fun `factory for invalid name should fail`()
    {
        val exception = assertThrows<RuntimeException> { computerStrategyFactory.createStrategy("INVALID") }
        assertThat(exception.message).isEqualTo("No Strategy named INVALID")
        assertThat(computerStrategyFactory.isValidStrategy("INVALID")).isFalse()
    }

    fun testForMoves(strategy: ComputerPlayerStrategy, game: Game, predicate: Predicate<Int>) {
        for (i in 1..1000)
        {
            assertThat(strategy.calculateMove(game)).matches { predicate.test(it) }
        }
    }

    @Test
    fun `random strategy does not violate allowed moves`()
    {
        val allowedMoves = mutableListOf(1,2,3,4)
        val game = createTestGame(10, allowedMoves)
        testForMoves(randomStrat, game) { it in allowedMoves}

    }

    @Test
    fun `random strategy does not exceed current number of matches`()
    {
        val allowedMoves = mutableListOf(1,2,3,4)
        val game = createTestGame(2, allowedMoves)
        testForMoves(randomStrat, game) { it <= 2}
    }

    @Test
    fun `dp strategy does not violate allowed moves`()
    {
        val allowedMoves = mutableListOf(1,2,3,4)
        val game = createTestGame(10, allowedMoves)
        testForMoves(dpStrat, game) { it in allowedMoves}
    }

    @Test
    fun `dp strategy does not exceed current number of matches`()
    {
        val allowedMoves = mutableListOf(1,2,3,4)
        val game = createTestGame(2, allowedMoves)
        testForMoves(dpStrat, game) { it <= 2}
    }

}