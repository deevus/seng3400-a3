IDL_OUT := client/Sync server/Sync

all: gen-idl

gen-idl:
	idlj -td client -fall sync.idl
	idlj -td server -fall sync.idl

clean:
	$(RM) -r $(IDL_OUT)
