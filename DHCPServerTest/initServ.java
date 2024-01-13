package DHCPServerTest;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import protocal.dhcp.common.address.NetworkAddress;
import protocal.directory.server.dhcp.dhcp.io.DhcpInterfaceManager;
import protocal.directory.server.dhcp.dhcp.messages.HardwareAddress;
import server.directory.server.dhcp.netty.netty.DhcpServer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import static jdk.nashorn.internal.objects.Global.print;

public class initServ {
	public static final String INTERFACE_NAME = "eth14";

	public static void main(String[] args) {
		try {
			CustomFixedStoreLeaseManager manager = newLeaseManager(INTERFACE_NAME);
			DhcpServer server = new DhcpServer(manager);
			server.addInterfaces(new DhcpInterfaceManager.NamedPredicate(INTERFACE_NAME));

			server.start();
			Thread.sleep(200000);
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@CheckForNull
	private static NetworkInterface getNetworkInterfaceByName(@Nonnull String name, @Nonnull Enumeration<NetworkInterface> ifaces) throws SocketException {
		for (NetworkInterface iface : Collections.list(ifaces)) {
			if (name.equals(iface.getName()))
				return iface;
			NetworkInterface subiface = getNetworkInterfaceByName(name, iface.getSubInterfaces());
			if (subiface != null)
				return subiface;
		}
		return null;
	}
	@CheckForNull
	public static NetworkInterface getNetworkInterfaceByName(@Nonnull String name) throws SocketException {
		return getNetworkInterfaceByName(name, NetworkInterface.getNetworkInterfaces());
	}
	/**
	 * ifconfig br0 10.27.0.1
	 * sudo dhcping -h 08:00:20:c0:ff:ee -s 10.27.0.1
	 * OR
	 * sudo dhclient -d eth0 -n
	 */
	@Nonnull
	public static CustomFixedStoreLeaseManager newLeaseManager(@Nonnull String interfaceName) throws Exception {
		NetworkInterface iface = getNetworkInterfaceByName(interfaceName);
		if (iface == null) {
			print(NetworkInterface.getNetworkInterfaces(), 0);
//			assertNotNull("No such interface " + interfaceName, iface);
		}
		InterfaceAddress address = Iterables.find(iface.getInterfaceAddresses(), new Predicate<InterfaceAddress>() {
			public boolean apply(InterfaceAddress input) {
				return input.getAddress() instanceof Inet4Address;
			}
		});
		NetworkAddress network = new NetworkAddress(address);
		CustomFixedStoreLeaseManager manager = new CustomFixedStoreLeaseManager();
		manager.addLease(HardwareAddress.fromString("00:0c:29:c2:eb:4c"), network.getMachineAddress(42));
		return manager;
	}
}
