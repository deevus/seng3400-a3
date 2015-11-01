import SyncApp.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import java.util.concurrent.ThreadLocalRandom;

public class SyncServer {
  public static void main(String[] args) {
    try {
      ORB server = getServer(args);
      server.run();
    }
    catch (Exception e) {
      System.out.println("Error: " + e);
      e.printStackTrace();
    }
  }

  public static ORB getServer(String[] args) {
    ORB orb = ORB.init(args, null);

    try {
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
  private final int randMin = 1;
  private final int randMax = 100;

  public int getRandomNumber() {
    int randSleepTime = ThreadLocalRandom
      .current()
      .nextInt(minSleep, maxSleep + 1);

    try { Thread.sleep(randSleepTime); }
    catch (InterruptedException e) { }

    return ThreadLocalRandom.current().nextInt(randMin, randMax + 1);
  }

  public void getRandomNumberAsync(SyncCallback ref) {
    int r = getRandomNumber();
    ref.callback(r);
  }
}
