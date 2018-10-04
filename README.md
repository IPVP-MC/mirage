# Mirage - Fake Blocks API #

<p>Mirage is a highly flexible plugin API to send fake blocks to players.</p> 

## Compilation ##

Building Mirage is made simple through usage of [Maven 3](http://maven.apache.org/download.html). Simply clone this repository and run the following command inside the Mirage directory:

```
mvn clean install
```

The resulting mirage.jar file will be in the ```target/``` folder. 

## Developers ##

Developers can easily tap into the Mirage API by simply adding the Mirage dependency to your Maven build path:

```xml
<dependencies>
  <dependency>
    <groupId>org.ipvp</groupId>
    <artifactId>mirage</artifactId>
    <version>3.0.0-SNAPSHOT</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

Make sure to register the packet adapters in your main class:

```java
@Override
public void onEnable() {
    ProtocolLibrary.getProtocolManager().addPacketListener(new BlockDigAdapter(this));
    ProtocolLibrary.getProtocolManager().addPacketListener(new BlockClickAdapter(this));
}
```

### Sending Blocks ###

Sending blocks is made simple through the usage of the ```BlockGenerator``` and ```FakeBlockSender``` interfaces. To begin, we will need to create a `BlockGenerator` and a `FakeBlockSender` instance for our own usage:

```java
private BlockGenerator generator = new SingleBlockGenerator(Material.PURPLE_STAINED_GLASS);
private FakeBlockSender fakeBlockSender = new PlayerBlockSender(player);
```
In this example, we will be sending fake blocks of red wool to players.

There are 2 primary options you can take when sending blocks with the ```FakeBlockSender``` interface:

* Sending multiple blocks
* Sending a single block update

#### Sending multiple blocks ####

Let's say we want to send a player a pillar of red wool, our required step breakdown would be as follows:

1. Create an empty list of ```org.bukkit.util.Vector```
2. Iterate the blocks that we want to send, adding them to our list via ```Location#toVector```
3. Using the ```FakeBlockSender``` (accessible via ```Mirage#getBlockSender``` to send all updates

Our final code would look something like the following:
```java
/**
 * Sends a pillar of red wool to a player
 */
public void sendPillarToPlayer(Player player) {
    Location start = player.getEyeLocation();
    List<Vector> blocks = new ArrayList<>();
    // Collect a pillar starting at the eye location and going to the cieling
    for (int y = start.getBlockY() ; y < 256 ; y++) {
        blocks.add(new Vector(start.getBlockX(), y, start.getBlockZ()));
    }
    // Send the blocks to the player through usage of the block sender instance
    fakeBlockSender.sendBlocks(generator, blocks);
}
```

#### Sending a single block ####

Sending a single block is as simple as passing in a vector location and our generator to the block sender instance:
```java
/**
 * Sends a single wool block to a player
 */
public void sendWool(Vector location) {
    fakeBlockSender.sendBlock(generator, location);
}
```

### Cleaning up memory ###

<p>Once a block has been sent, it remains permanently in a cache until removed. The primary implementation
of FakeBlockSender stores blocks in a key->block cache using the player UUID as a key. It is crucial to
 clean up and remove any fake blocks sent once they are not being used anymore.</p>
 
This task is done simply through the ```clearBlockAt``` or ```clearBlocks``` methods provided by the FakeBlockSender class. Note that the ```clearBlocks``` method has availability to use a ```Predicate``` in order to filter through specific blocks, and not just remove all sent blocks.

## License ##
This software is available under the following licenses:

* MIT