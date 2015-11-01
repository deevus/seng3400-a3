SRC := $(wildcard **/*.java) $(wildcard *.java)
JC := javac

build:
	$(JC) $(SRC)

all: build

clean:
	$(RM) -r SyncApp *.class

run-deferred:
	java SyncClient -SyncMode deferred

run-async:
	java SyncClient -SyncMode async
