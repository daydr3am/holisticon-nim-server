# Nim game server

This server is implemented in Kotlin and uses Spring. The build system is gradle.
It allows creating multiple games of Nim with varying number of matches
and different number of matches that can be taken in each turn. Furthermore,
we game is always played against the computer, but during creation one can specify 
one of two possible strategies (random or winning-oriented strategy). The game is 
played by POSTing a JSON payload to the server, containing the information about
the players turn. 

## Setup

As already mentioned, the application is using gradle. You will need some version of a
JDK. The Server was tested with Java 20. To run the server, 
execute 

```
./gradlew bootRun
```

It will start the server at `localhost:8080`

## Usage
The documentation of the API interface can be found in
api-specification.json.

### Creating a game
To create a game, POST a JSON request to the `/game/new` endpoint. An example
for such a JSON request is
```json
{
    "matches": 20,
    "allowedMoves": [1,2,3,4],
    "computerStrategy": "DP"
}
```

Setting the matches in the game to 20, allowing to even take four matches at a
time and using the DP strategy for the computer player. All these fields are optional
and can be omitted. The standard values for all the fields are:

```json
{
    "matches": 13,
    "allowedMoves": [1,2,3],
    "computerStrategy": "DP"
}
```

For matches and allowedMoves there are few restrictions:
- matches has to be at least zero
- allowedMoves can only contain non-negative moves and cannot be empty

The computerStrategy can only be one of two values either "DP" or "RANDOM".
The endpoint will return a JSON response containing the game entity

```json
{
    "id": "53247e7d-7987-46e8-9cd3-21ebce172b6c",
    "currentState": {
        "numMatches": 20,
        "turn": 0,
        "playersTurn": true
    },
    "allowedMoves": [
        1,
        2,
        3,
        4
    ],
    "computerPlayerStrategy": "DP",
    "winner": "none"
}
```

The `id` is used to identify the game in the next step to make a turn. The 
`currentState` field contains the information about the current game state such 
as the number of matches left in the heap and how many turns have been done already.
The `winner` field is none as long the game is in progress and will be updated.

### Make a move
To make a move, the user has to POST a JSON request to the `/game/makeMove` endpoint.
For example, to make a move in the game above, the following request can be used
```json
{
    "id": "53247e7d-7987-46e8-9cd3-21ebce172b6c",
    "nMatches": 1
}
```

This call might return a failure code if the move is not legal or the game ID 
is not found.

### Get a game
The `game/get?id=` can be used to query for games stored in the game server.
The response if either a game entity or a not found exception if the id is not
found. An example request looks as follows:
```
localhost:8080/game/get?id=53247e7d-7987-46e8-9cd3-21ebce172b6c
```
