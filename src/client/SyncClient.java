/*
  SENG3400
  Assignment 3
  Simon Hartcher
  C3185790
 */

import SyncApp.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.*;

public class SyncClient {
  private SyncApp.SyncCallback callback;
  private int privateField = 200;
  private final Sync service;
  private final ORB orb;

  enum SyncMode {
    Invalid,
    Async,
    Deferred
  }

  public SyncClient(Sync service, ORB orb) {
    this.service = service;
    this.orb = orb;
  }

  public static void main(String[] args) {
    try {
      //parse syncmode from arguments
      SyncMode syncMode = getSyncMode(args);
      if (syncMode == SyncMode.Invalid) {
        //we cant continue if we have an invalid syncmode
        System.exit(1);
      }

      //init orb
      ORB orb = ORB.init(args, null);

      //init client
      SyncClient client = getClient(orb);

      switch (syncMode) {
        case Async:
          client.runAsync();
          break;
        case Deferred:
          client.runDeferred();
          break;
      }
    }
    catch(Exception e) {
      System.out.println("Error: " + e);
    }
  }

  /**
   * Parses SyncMode from command line arguments
   */
  static SyncMode getSyncMode(String[] args) {
    SyncMode syncMode = SyncMode.Invalid;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-SyncMode")) {
        switch(args[i + 1].toLowerCase()) {
          case "deferred":
            syncMode = SyncMode.Deferred;
            break;
          case "async":
            syncMode = SyncMode.Async;
            break;
          default:
            System.err.println("Error: Invalid SyncMode '" + args[i + 1] + "'");
        }
      }
    }

    return syncMode;
  }

  /**
   * Initiates binding with orb and creates a SyncClient instance
   */
  static SyncClient getClient(ORB orb) {
    try {
      //binding and stuff
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			NameComponent nc = new NameComponent("Sync", "");
			NameComponent[] path = {nc};
			Sync syncRef = SyncHelper.narrow(ncRef.resolve(path));

			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

      //create the client
      SyncClient client = new SyncClient(syncRef, orb);

      //init our callback for when we're using async
			SyncCallbackDecorator syncCbRef = new SyncCallbackDecorator(client);
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(syncCbRef);
			client.setCallback(SyncCallbackHelper.narrow(ref));

			return client;
    }
    catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);

      //redundant statement
      return null;
    }
  }

  public void setCallback(SyncApp.SyncCallback callback) {
    this.callback = callback;
  }

  public void setPrivateField(int value) {
    this.privateField = value;
  }

  /*
    Client runs in deferred mode
   */
  private void runDeferred() throws InterruptedException, WrongTransaction {
    //get request
    Request request = service._request("getRandomNumber");

    //set return type to long
    request.set_return_type(orb.get_primitive_tc(TCKind.tk_long));

    //init vars
    int       count     = 0;
    final int sleepTime = 500,
              query     = 5,
              sync      = 10,
              end       = 15;
    boolean   done      = false;

    while (!done) {
      Thread.sleep(sleepTime);

      //output current tick and value
      System.out.println("Tick: " + (++count));
      System.out.println("Value: " + this.privateField);

      //we've reached the final tick and can exit
      if (count == end) {
        done = true;
      }

      //get server to generate a value
      if (count == query) {
        request.send_deferred();
      }

      //syncronise with server value
      if (count == sync) {
        //spin until we receive a response
        while (!request.poll_response()) { }

        //get response and assign it to our member variable
        request.get_response();
        this.setPrivateField(request.return_value().extract_long());
      }
    }
  }

  /*
    Run client in async mode
   */
  private void runAsync() throws InterruptedException {
    //init vars
    int       count     = 0,
              endCount  = 5;
    final int sleepTime = 500,
              query     = 5;
    boolean   done      = false;

    while (!done) {
      Thread.sleep(sleepTime);

      //print current tick and value
      System.out.println("Tick: " + (++count));
      System.out.println("Value: " + this.privateField);

      //count down once the value has changed
      if (this.privateField != 200) {
        endCount--;
      }

      //we can end now
      if (endCount == 0) {
        done = true;
      }

      //query the server which will be resolved via callback
      if (count == query) {
        service.getRandomNumberAsync(callback);
      }
    }
  }
}

/**
 * SyncCallback decorator class which updates a SyncClient variable when the
 * callback is fired
 */
class SyncCallbackDecorator extends SyncCallbackPOA {
  private final SyncClient _client;

  public SyncCallbackDecorator(SyncClient client) {
    _client = client;
  }

  public void callback(int rand) {
    _client.setPrivateField(rand);
  }
}
