package hyousa.common.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by yousa on 2018/1/3.
 */
public class IpUtil {
    public static String getLocal() throws IOException {
        return getLocalHostLANAddress().getHostAddress();
    }

    public static InetAddress getLocalHostLANAddress() throws IOException {
        InetAddress candidateAddress = null;
        // 遍历所有的网络接口
        for (Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces(); networks.hasMoreElements(); ) {
            NetworkInterface network = networks.nextElement();
            // 在所有的接口下再遍历IP
            for (Enumeration<InetAddress> addresses = network.getInetAddresses(); addresses.hasMoreElements(); ) {
                InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress()) {// 排除loopback类型地址
                    if (address.isSiteLocalAddress()) {
                        // 如果是site-local地址，就是它了
                        return address;
                    } else if (candidateAddress == null) {
                        // site-local类型的地址未被发现，先记录候选地址
                        candidateAddress = address;
                    }
                }
            }
        }
        if (candidateAddress != null) {
            return candidateAddress;
        }
        // 如果没有发现 non-loopback地址.只能用最次选的方案
        return InetAddress.getLocalHost();
    }
}
