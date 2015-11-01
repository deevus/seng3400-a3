SRC := $(wildcard **/*.java) $(wildcard *.java)
JC := javac

build:
	$(JC) $(SRC)

all: build

clean:
	$(RM) -r SyncApp *.class

run-deferred:
	java SyncClient -SyncMode deferred -ORBInitialHost localhost -ORBInitialPort 2015

run-async:
	java SyncClient -SyncMode async -ORBInitialHost localhost -ORBInitialPort 2015
