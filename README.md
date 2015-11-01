# SENG3400 - Assignment 3
## Simon Hartcher - C3185790

### Instructions

1. Generate IDL (`src/`)

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
