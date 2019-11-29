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

* `HIT` selected coordinates had a boat
* `MISS` selected coordinates were empty
* `SUNK` remote boat was fully hit, and then sunk
* `WAIT` wait for opponent
* `READY` your move
* `LOST` you lost the game
* `WON` you won the game

---
## Game matrix
The server saves the status of each player and its matrix. Each tile of the matrix is an object composed like:
```java
Boat boat = new Boat();
Boolean alreadyHit = false;
```

