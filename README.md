# SENG3400 - Assignment 3
## Simon Hartcher - C3185790

### Instructions

1. Generate IDL (`src/`)

Note that we use the `-fall` flag as the helper classes are required in the implementation.

```
idlj -td client -fall sync.idl
idlj -td server -fall sync.idl
```

2. Compile server (`src/server/`)

```
javac *.java SyncApp/*.java
```

3. Compile client (`src/client/`)

```
javac *.java SyncApp/*.java
```

4. Start server (assuming orb daemon is running) (`src/server/`)

```
java SyncServer
```

5. Run clients (`src/client/`)

```
java SyncClient -SyncMode deferred
java SyncClient -SyncMode async
```

### Notes

The deferred client will always end after 15 ticks, as the client will block on
the 10th tick until we can assign a new value, before doing the final five ticks.

The async client will end somewhere on or after 10 ticks depending on the server.
When the value changes, the final five ticks counts down before ending.
