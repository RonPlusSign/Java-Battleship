## Request Protocol 

Create boat:
```
SET xxyylo
```
* `xx` is a char representing the x coordinate of the boat (2 char length)
* `yy` is the y coordinate (2 char length)
* `l` is the lenght of the boat (1 char length)
* `o` is the orientation of the boat (1 char, H Horizontal, V Vertical)

Fire boat:
```
FIRE xxyy
```
* `xx` x coordinate (2 char length)
* `yy` y coordinate (2 char length)

Delete boat:
```
DELETE xxyy
```
* `xx` x coordinate (2 char length)
* `yy` y coordinate (2 char length)

Ask client to create its grid:
```
GRID abcd
```
abcd are the number of ships to position, ordered by length
* a: number of boats of length 2
* b: number of boats of length 3
* c: number of boats of length 4
* d: number of boats of length 5

---
## Response Protcol
The response is a simple string with a status update:

* `PLAY` both players grid are set, game management can start (clients can start listening for commands)
* `HIT` selected coordinates had a boat
* `MISS` selected coordinates were empty
* `SUNK` remote boat was fully hit, and then sunk
* `WAIT` wait for opponent
* `READY` the player is ready to start playing (ship grid is completely set)
* `LOST` you lost the game
* `WON` you won the game
* `ERROR <code> <msg>` unsuccessful command
* `OK <msg>` successful command
* `TURN` Tells to the client that is it's turn
* `MESSAGE <msg>` A simple message

### Error status codes
* `1` Invalid boat position [SET]
* `2` Selected boat size not available [SET]
* `3` Invalid orientation [SET]
* `4` Unknown message
* `5` Connection error
* `6` Selected tile doesn't contain a boat [DELETE]

---
## Game matrix
The server saves the status of each player and its matrix. Each tile of the matrix is an object composed like:
```
Ship ship = new Ship();
Boolean isHit = false;
```

