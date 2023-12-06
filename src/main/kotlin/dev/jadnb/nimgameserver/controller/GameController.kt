package dev.jadnb.nimgameserver.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains
import dev.jadnb.nimgameserver.entities.Game
import dev.jadnb.nimgameserver.exceptions.*
import dev.jadnb.nimgameserver.logic.GameService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Spring Controller for the Nim Game Application, handles
 * http requests to the API and forwards them to the GameService.
 */
@RestController
@RequestMapping("/game")
class GameController @Autowired constructor(
    private val gameService: GameService
)
{
    /*
     * Endpoints
     */

    /**
     * Endpoint for creating a new game. The JSON request could be an
     * empty object or might specify the number of matches, allowedMoves, and
     * the strategy the computer player should use.
     * @param payload Additional rules for the game
     * @return A new game
     * @throws InvalidInputException if the payload specifies invalid values
     */
    @PostMapping("/new")
    fun createGame(@RequestBody payload: JsonNode): Game {
        // Will throw an exception if input is invalid
        validateNewGameInput(payload)
        return gameService.create(
            getNumberOfMatches(payload),
            getLegalMoves(payload),
            getStrategy(payload)
        )
    }

    /**
     * Endpoint for playing the game. Will validate the input and then
     *  hand over the information to the GameService.
     * @param payload Information about the move the player makes
     * @return The updated game
     * @throws InvalidInputException can be thrown if any input is missing
     * or not in the correct format.
     * @throws IllegalTurn is thrown when the action taken by the user is not allowed.
     * @throws NotFoundException is thrown if the game id does not exist
     */
    @PostMapping("/makeMove")
    fun makeMove(@RequestBody payload: JsonNode): Game {
        // Will throw an exception if input is incorrect
        validateMoveInput(payload)

        // UUID.fromString might fail, if so, throw InvalidInputException
        val id  = try { UUID.fromString(payload["id"].asText()) }
            catch (e: Exception) { throw InvalidInputException(e.message ?: "")}
        val nMatches = payload["nMatches"].toString().toInt()
        return gameService.makeMove(id, nMatches)
    }

    /**
     * Endpoint for getting a game without any state change
     * @param id given as query parameter
     */
    @GetMapping("/get")
    fun getGame(@RequestParam("id") id: String): Game {
        val uuid = try { UUID.fromString(id) }
            catch (e: Exception) { throw InvalidInputException(e.message ?: "")}

        return gameService.getGame(uuid) ?: throw NotFoundException("No game with $uuid was found")
    }

    /*
     * Helper Functions
     */

    /**
     * Validates the JSON request and checks if its eligible as request
     * for a move. If validation succeeds, the function returns.
     * @throws InvalidInputException is thrown if the validation does not succeed.
     */
    private fun validateMoveInput(payload: JsonNode) {
        if (!payload.isObject)
            throw InvalidInputException("Expected JSON object")

        if (!payload.contains("nMatches"))
            throw InvalidInputException("Expected field nMatches")

        if (!payload.contains("id"))
            throw InvalidInputException("Expected field id")
    }

    /**
     * Validates the JSON request for a new game. If validation succeeds, the function
     *  returns.
     * @throws InvalidInputException is thrown if the validation does not succeed.
     */
    private fun validateNewGameInput(payload: JsonNode) {
        if (!payload.isObject)
            throw InvalidInputException("Expected JSON object")
    }

    /**
     * Reads the "matches" field from a JSON request and parses it as int
     * @return the value of the "matches" field or a default value
     */
    private fun getNumberOfMatches(payload: JsonNode): Int {
        return if (payload.has("matches") && payload["matches"].isInt)
            payload["matches"].asInt()
        else 13
    }

    /**
     * Reads the "allowedMoves" field from a JSON request and parses the items
     * to ints.
     * @return the array in the "allowedMoves" field or a default value
     */
    private fun getLegalMoves(payload: JsonNode): List<Int> {
        return if (payload.has("allowedMoves")
            && payload["allowedMoves"].isArray
            && payload["allowedMoves"].all { it.isInt })
            payload["allowedMoves"].fold(listOf()) {acc, v ->
                acc + v.asInt()
            }
        else listOf(1,2,3)
    }

    /**
     * Reads the "computerStrategy" field which indicates which strategy the computer player should use.
     * @return the strategy specified in the "computerStrategy" field or a default value
     */
    private fun getStrategy(payload: JsonNode): String {
        return if (payload.has("computerStrategy") && payload["computerStrategy"].isTextual)
                payload["computerStrategy"].asText()
            else
                "DP"
    }
}