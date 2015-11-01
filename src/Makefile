all: gen-idl build-server build-client

gen-idl:
	idlj -td client -fall sync.idl
	idlj -td server -fall sync.idl

build-server:
	$(MAKE) -C server

build-client:
	$(MAKE) -C client

clean:
	$(MAKE) clean -C server
	$(MAKE) clean -C client

run-server:
	$(MAKE) run -C server

run-client-deferred:
	$(MAKE) run-deferred -C client

run-client-async:
	$(MAKE) run-async -C client
