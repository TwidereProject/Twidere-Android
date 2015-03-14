package twitter4j.http;

import java.io.IOException;
import java.net.InetAddress;

public interface HostAddressResolver {

    public InetAddress[] resolve(String host) throws IOException;

}
