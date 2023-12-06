package dev.jadnb.nimgameserver.entities

import dev.jadnb.nimgameserver.entities.Game
import dev.jadnb.nimgameserver.entities.GameState
import org.springframework.data.repository.CrudRepository
import java.util.UUID

/**
 * Repository to queue and save Game objects
 */
interface GameRepository : CrudRepository<Game, UUID> {
    fun findGameById(id: UUID): Game?
}

/**
 * Repository to queue and save GameState objects
 */
interface GameStateRepository : CrudRepository<GameState, UUID>

