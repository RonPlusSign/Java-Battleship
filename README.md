# Java Battleship

### This mainly is the server of the application, for the client's GUI go to:
#### https://github.com/regi18/vue-battleship-gui
###### This project contains a very basic CLI client (Client.java)

---

## How to run Server.java
Inside the 'server' folder, run the following command:
 ```shell script
> javac *java
```
Then:
 ```shell script
> cd ..
> java server.Server
```


(to test)
Create your jar file using the following command:
 ```shell script
jar -cvfe server.jar *
```

When launching the Server using CLI, some arguments can be added.
Possible args are:
*  `-p [portNumber]` specifies the port where the server has to listen from
*  `-l [gridLength]` specifies the grid length
*  `-p [portNumber] -l [gridLength]` specifies both options
*  `-l [gridLength] -p [portNumber]`

## How to run Client.java

###TO BE ADDED

When launching the Client using CLI, some arguments can be added.
Possible args are:
*  `[server address]` (format: "127.0.0.1")
*  `[portNumber]` (from 1 to 65535)
*  `[server address] [portNumber]`
---
<br>

# Communication specifications

## Format

The protocol is based on a simple request-response form. The message format is as follows:

REQUEST
```
CMD <space> MSG
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
 

where `<space>` is a simple space, `CMD` is the command, and `MSG` is the request/response body (the `msg` key is optional)

---

## Requests 

Create ship:
```
SET rrcclo
```
* `rr` is the row coordinate of the ship (2 char length)
* `cc` is the column coordinate (2 char length)
* `l` is the lenght of the ship (1 char length)
* `o` is the orientation of the ship (1 char, H Horizontal, V Vertical)

Fire ship:
```
FIRE rrcc
```
* `rr` row coordinate (2 char length)
* `cc` column coordinate (2 char length)

Delete ship:
```
DELETE rrcc
```
* `rr` row coordinate (2 char length)
* `cc` column coordinate (2 char length)

Delete all ships:
```
RESET
```
[response is `GRID [...]`]

Asks for grid infos:
```
GRID
```
This interrogates the server, which will respond with the corresponding infos (more in the responses)

Tells the server that the client has all the ships set and is ready to play:
```
READY
```
[ possible response are `WAIT` or `PLAY` or `ERROR`]

---
## Responses
the documentation is structured like:
* `CMD`
  * `MSG`

#### Entry point (player are setting ships)

* `WAIT` wait for opponent (to finish positioning ships)
* `GRID` response for the a `GRID` or `RESET` requests.
  * `{ length: l, ships: [...] }`
    * `length`: size of the grid
    * `ships`: is the ships array (of integers) of the ships left to position.

#### Server.Game (both players are in the game)

* `PLAY` game can begin  (needs to be sent to all players)
* `HIT` selected coordinates had a ship
   * `{ row: r, col: c }`
* `MISS` selected coordinates were empty
   * `{ row: r, col: c }`
* `SUNK` remote ship was fully hit, and then sunk. (the message contains the coordinates for the head of the ship + length and orientation)
   * `{ row: r, col: c, length: l, orientation: "..." }`
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

---
### Error status codes

format: `abb` the first digit (a) is the category, while the other digits (bb) represent the error itself

###### POSITIONING SHIPS [category: 1]
 * `00` Invalid ship position
 * `01` Selected ship size not available
 * `02` Selected tile doesn't contain a ship
 * `03` READY command not valid, you still have ships remaining
 * `04` Invalid Orientation (Select H or V)
 
###### GENERAL [category: 9]
 * `00` Invalid message
 * `01` Selected coordinates invalid
  * `02` Connection error
  

