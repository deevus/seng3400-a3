/*
  SENG3400
  Assignment 3
  Simon Hartcher
  C3185790
 */

import SyncApp.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import java.util.concurrent.ThreadLocalRandom;

public class SyncServer {
  public static void main(String[] args) {
    try {
      //get the server connection
      ORB server = getServer(args);

      //run it
      server.run();
    }
    catch (Exception e) {
      System.out.println("Error: " + e);
      e.printStackTrace();
    }
  }

  public static ORB getServer(String[] args) {
    //init
    ORB orb = ORB.init(args, null);

    try {
      //bootstrap
      POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      rootpoa.the_POAManager().activate();

      SyncServant syncRef = new SyncServant(); // Create servant

      // Get an object reference for the servant
      org.omg.CORBA.Object ref = rootpoa.servant_to_reference(syncRef);
      Sync sRef = SyncHelper.narrow(ref);

      // Get root Naming Service context ... "NameService" invokes the name service
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

      // Bind the Server reference into the Naming Context
      String name = "Sync";
      NameComponent[] path = ncRef.to_name(name);
      ncRef.rebind(path, sRef);
    }
    catch (Exception e) {
      //do nothing
    }

    return orb;
  }

}

class SyncServant extends SyncPOA {
  private final int minSleep = 1000;
  private final int maxSleep = 5000;
  private final int randMin  = 1;
  private final int randMax  = 100;

  public int getRandomNumber() {
    //get random millis to sleep for
    int randSleepTime = ThreadLocalRandom
      .current()
      .nextInt(minSleep, maxSleep + 1);

    //sleep thread
    try { Thread.sleep(randSleepTime); }
    catch (InterruptedException e) { }

    //return random number
    return ThreadLocalRandom.current().nextInt(randMin, randMax + 1);
  }

  public void getRandomNumberAsync(SyncCallback ref) {
    int r = getRandomNumber();
    ref.callback(r);
  }
}
