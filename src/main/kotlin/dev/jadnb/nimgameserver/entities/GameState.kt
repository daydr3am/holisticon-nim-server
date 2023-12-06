package dev.jadnb.nimgameserver.entities

import jakarta.persistence.*
import java.util.*

/**
 * JPA Entity that represents the current game state. Note that this object designed as an immutable
 * object is replaced by a new object when a player makes a move.
 * @param id A unique identifier that is generated by the JPA backend
 * @param numMatches the number of matches that are left in the game.
 * @param game the parent game object, each GameState only belongs to one game
 * @param turn The number of turns already done
 * @param playersTurn Indicates if the next turn is the players turn (or not)
 */
@Entity
class GameState(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    val numMatches: Int,
    @ManyToOne
    val game: Game,
    val turn: Int,
    val playersTurn: Boolean,
)