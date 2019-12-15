# Java Battleship Server

### This is just the server of the application, for the client go to:
#### https://github.com/regi18/vue-battleship-gui

---

## Basic principles

The protocol is based on a simple request-response form. The message format is as follows:

REQUEST
```
CMD <sp> MSG
```

RESPONSE
```json
{
 "cmd" : "...",
 "msg" : "..."
}
```

the msg key can contain an object, for example on the HIT response:
```json
{
 "cmd" : "HIT",
 "msg" : 
   {
    "row" : "...",
    "col" : "..."
   }
}
```
 

where `<sp>` is a simple space, `CMD` is the command, and `MSG` is the request/response body (the `msg` key is optional)

---

## Requests 

Create boat:
```
SET iijjlo
```
* `ii` is a char representing the row coordinate of the boat (2 char length)
* `jj` is the column coordinate (2 char length)
* `l` is the lenght of the boat (1 char length)
* `o` is the orientation of the boat (1 char, H Horizontal, V Vertical)

Fire boat:
```
FIRE iijj
```
* `ii` row coordinate (2 char length)
* `jj` column coordinate (2 char length)

Delete boat:
```
DELETE iijj
```
* `ii` row coordinate (2 char length)
* `jj` column coordinate (2 char length)

Asks for grid infos:
```
GRID
```
This interrogates the server, which will respond with the corresponding infos (more in the responses)

Tells the server that the client has all the ships set and is ready to play:
```
READY
```
[ possible answers are `WAIT` or `PLAY` or `ERROR`]

---
## Responses
the documentation is structured like:
* `CMD`
  * `MSG`

#### Entry point (player are setting ships)

* `WAIT` wait for opponent (to finish positioning ships)
* `GRID` answer for the a `GRID` request
  * `{ length: l, ships: [...] }`
    * `length`: size of the grid
    * `ships`: is the ships array (of integers)

#### Game (both players are in the game)

* `PLAY` game can begin  (needs to be sent to all players)
* `HIT` selected coordinates had a boat
   * `{ row: i, col: j }`
* `MISS` selected coordinates were empty
   * `{ row: i, col: j }`
* `SUNK` remote boat was fully hit, and then sunk. (the message contains the coordinates for the head of the ship + length and orientation)
   * `{ row: i, col: j, length: l, orientation: "..." }`
* `TURN`
  * msg: `false` opponent's turn
  * msg: `true` your turn

#### Universal (this are always valid)

* `LOST` you lost the game
* `WON` you won the game
* `ERROR`
  * `{ cod: "...", msg: "..." }`
    * `cod`: error status code
    * `msg`: error message
* `OK` successful command
  * `{ cod: "...", msg: "..." }`
    * `cod`: success status code
    * `msg`: success message

---
### Error status codes

format: `abb` the first digit (a) is the category, while the other digits (bb) represent the error itself

###### POSITIONING SHIPS [category: 1]
 * `00` Invalid boat position
 * `01` Selected boat size not available
 * `02` Selected tile doesn't contain a boat
 * `03` READY command not valid, you still have ships remaining
 
###### GAME [category: 2]
 

###### UNIVERSAL [category: 9]
 * `00` Unknown message
 * `01` Selected coordinates invalid

---
### Success status codes

format: `abb` the first digit (a) is the category, while the other digits (bb) represent the error itself

###### POSITIONING SHIPS [category: 1]
 * `00` Ship positioned correctly
 
###### GAME [category: 2]

###### UNIVERSAL [category: 9]
 * `00` OK

