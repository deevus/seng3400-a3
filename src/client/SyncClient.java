import SyncApp.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

public class SyncClient {
  public static void main(String[] args) {
    try {
      ORB orb = ORB.init(args, null);
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
      NameComponent nc = new NameComponent("Sync", "");
      NameComponent[] path = {nc};
      Sync syncRef = SyncHelper.narrow(ncRef.resolve(path));

      int random = syncRef.getRandomNumber();
    }
    catch(Exception e)
    {
      System.out.println("Error: " + e);
    }
  }
}
