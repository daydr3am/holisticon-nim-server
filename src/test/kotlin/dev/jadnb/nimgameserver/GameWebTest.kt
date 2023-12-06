package dev.jadnb.nimgameserver

import com.fasterxml.jackson.databind.ObjectMapper
import dev.jadnb.nimgameserver.controller.GameController
import dev.jadnb.nimgameserver.entities.Game
import dev.jadnb.nimgameserver.entities.GameState
import dev.jadnb.nimgameserver.exceptions.IllegalTurn
import dev.jadnb.nimgameserver.exceptions.NotFoundException
import dev.jadnb.nimgameserver.logic.GameService
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*


@WebMvcTest(GameController::class)
class GameWebTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
){

    @MockBean
    lateinit var gameService: GameService

    @Test
    fun `New Game endpoint should work with default parameters`()
    {
        val game = Game(null, null, mutableListOf(), mutableListOf())
        `when`(gameService.create()).thenReturn(game)
        mockMvc.post("/game/new") {
            content = mapOf<String, Any>()
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `New Game endpoint should accept additional game parameters`()
    {
        val game = Game(null, null, mutableListOf(), mutableListOf())
        `when`(gameService.create(anyInt(), anyList(), anyString())).thenReturn(game)

        mockMvc.post("/game/new") {
            content = objectMapper.writeValueAsString(mapOf<String, Any>("matches" to 14))
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }

        mockMvc.post("/game/new") {
            content = objectMapper.writeValueAsString(mapOf<String, Any>("allowedMoves" to listOf("1,2,3,4")))
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }

        mockMvc.post("/game/new") {
            content = objectMapper.writeValueAsString(mapOf("matches" to 14, "allowedMoves" to listOf("1,2,3,4")))
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `New Game with arguments should call GameService with parameters`()
    {
        val game = Game(null, null, mutableListOf(), mutableListOf())
        `when`(gameService.create(eq(14), anyList(), anyString())).thenReturn(game)

        mockMvc.post("/game/new") {
            content = objectMapper.writeValueAsString(mapOf("matches" to 14, "allowedMoves" to listOf("1,2,3,4")))
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
            }
        }
    }

    @Test
    fun `New Game should accept Strategy argument`()
    {
        mockMvc.post("/game/new") {
            content = objectMapper.writeValueAsString(mapOf("computerStrategy" to "RANDOM"))
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
        }
    }

    @Test
    fun `Check if fields are present`()
    {
        val game = Game(null, null, mutableListOf(), mutableListOf(1,2,3))
        val gameState = GameState(null, 13, game, 0, true)
        game.currentState = gameState
        `when`(gameService.create(anyInt(), anyList(), anyString())).thenReturn(game)

        mockMvc.post("/game/new") {
            content = objectMapper.writeValueAsString(mapOf<String, Any>())
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
            }
            jsonPath("\$.id").exists()
            jsonPath("\$.allowedMoves", `is`(listOf(1,2,3)))
            jsonPath("\$.winner", `is`("none"))
            jsonPath("\$.currentState").exists()
            jsonPath("\$.currentState.numMatches", `is`(13))
            jsonPath("\$.currentState.turn", `is`(0))
            jsonPath("\$.currentState.playersTurn", `is`(true))
        }
    }

    @Test
    fun `Empty request should fail`()
    {
        mockMvc.post("/game/new")
            .andExpectAll {
                status { isBadRequest() }
            }
    }

    @Test
    fun `Make move should work if supplied with correct arguments`()
    {
        val arguments = mapOf("nMatches" to 3, "id" to UUID.randomUUID())
        mockMvc.post("/game/makeMove") {
            content = objectMapper.writeValueAsString(arguments)
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status { isOk() }
        }
    }

    @Test
    fun `Make move with missing arguments should fail`()
    {
        val arg1 = mapOf("nMatches" to 3)
        val arg2 = mapOf("id" to "abc")
        mockMvc.post("/game/makeMove") {
            content = objectMapper.writeValueAsString(arg1)
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status {
                isUnprocessableEntity()
            }
        }

        mockMvc.post("/game/makeMove") {
            content = objectMapper.writeValueAsString(arg2)
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status {
                isUnprocessableEntity()
            }
        }
    }

    private fun <T> anyObject(): T {
        return Mockito.any<T>()
    }

    @Test
    fun `Illegal move should throw forbidden`()
    {
        val errorMessage = "Illegal turn"
        `when`(gameService.makeMove(anyObject(), anyInt())).thenThrow(IllegalTurn(errorMessage))
        val arguments = mapOf("nMatches" to 3, "id" to UUID.randomUUID())

        mockMvc.post("/game/makeMove") {
            content = objectMapper.writeValueAsString(arguments)
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status {
                isForbidden()
            }
        }
    }

    @Test
    fun `Invalid ID should return not found`()
    {
        val errorMessage = "Not Found"
        `when`(gameService.makeMove(anyObject(), anyInt())).thenThrow(NotFoundException(errorMessage))
        val arguments = mapOf("nMatches" to 3, "id" to UUID.randomUUID())
        mockMvc.post("/game/makeMove") {
            content = objectMapper.writeValueAsString(arguments)
            contentType = MediaType.APPLICATION_JSON
        }.andExpectAll {
            status {
                isNotFound()
            }
        }
    }

}