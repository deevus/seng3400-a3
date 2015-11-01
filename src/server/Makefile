SRC := $(wildcard **/*.java) $(wildcard *.java)
JC := javac

build:
	$(JC) $(SRC)

all: build

clean:
	$(RM) -r SyncApp *.class

run:
	java SyncServer
