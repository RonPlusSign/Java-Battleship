## Request Protocol 

Create boat:
```
SET xxyylo
```
* `xx` is a char representing the x coordinate of the boat (2 char lenght)
* `yy` is the y coordinate (2 char lenght)
* `l` is the lenght of the boat (1 char lenght)
* `o` is the orientation of the boat (1 char, H Horizontal, V Vertical)

Fire boat:
```
FIRE xxyy
```
* `xx` x coordinate (2 char lenght)
* `yy` y coordinate (2 char lenght)

---
## Response Protcol
The response is a simple string with a status update:

* `HIT` the selected coordinates had a boat
* `MISS` the selected coordinates were empty
* `SUNK` the remote boat was fully hit, and then sunk
* `WAIT` wait for the opponent
* `READY` you're move
* `LOST` you've lost the game
* `WON` you've won the game

---
## Game matrix
The server saves the status of the player with matrix. Each tile of the matrix is an object composed like:
```java
var Boat = new Boat();
Boolean alreadyHit = false;
```

Subsequently the boat object will be:
```

```
