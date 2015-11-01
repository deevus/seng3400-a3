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
      SyncMode syncMode = getSyncMode(args);
      if (syncMode == SyncMode.Invalid) {
        System.exit(1);
      }

      ORB orb = ORB.init(args, null);
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

  static SyncClient getClient(ORB orb) {
    try {

      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			NameComponent nc = new NameComponent("Sync", "");
			NameComponent[] path = {nc};
			Sync syncRef = SyncHelper.narrow(ncRef.resolve(path));

			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

      SyncClient client = new SyncClient(syncRef, orb);
			SyncCallbackDecorator syncCbRef = new SyncCallbackDecorator(client); // Create servant
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

  private void runDeferred() throws InterruptedException, WrongTransaction {
    //get request
    Request request = service._request("getRandomNumber");

    //set return type to long
    request.set_return_type(orb.get_primitive_tc(TCKind.tk_long));

    int       count     = 0;
    final int sleepTime = 500,
              query     = 5,
              sync      = 10;

    boolean done = false;
    while (!done) {
      System.out.println("Tick: " + (count++));
      System.out.println("Value: " + this.privateField);

      Thread.sleep(sleepTime);

      if (count >= 50) {
        done = true;
      }

      if (count % query == 0) {
        request.send_deferred();
      }
      else if (count % sync == 0) {
        //spin until we receive a response
        while (!request.poll_response()) { }

        request.get_response();
        this.setPrivateField(request.return_value().extract_long());
      }
    }
  }

  private void runAsync() throws InterruptedException {
    int       count     = 0;
    final int sleepTime = 500,
              query     = 5;
    boolean done = false;

    while (!done) {
      System.out.println("Tick: " + (count++));
      System.out.println("Value: " + this.privateField);

      Thread.sleep(sleepTime);

      if (count >= 50) {
        done = true;
      }

      if (count % query == 0) {
        service.getRandomNumberAsync(callback);
      }
    }
  }
}

class SyncCallbackDecorator extends SyncCallbackPOA {
  private final SyncClient _client;

  public SyncCallbackDecorator(SyncClient client) {
    _client = client;
  }

  public void callback(int rand) {
    System.out.println(rand);
  }
}
