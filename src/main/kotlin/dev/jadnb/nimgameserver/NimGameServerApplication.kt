package dev.jadnb.nimgameserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NimGameServerApplication

fun main(args: Array<String>) {
	runApplication<NimGameServerApplication>(*args)
}
