package DHCPServerTest;

import protocal.directory.server.dhcp.dhcp.DhcpException;
import protocal.directory.server.dhcp.dhcp.io.DhcpRequestContext;
import protocal.directory.server.dhcp.dhcp.messages.DhcpMessage;
import protocal.directory.server.dhcp.dhcp.messages.MessageType;
import protocal.directory.server.dhcp.dhcp.options.vendor.DomainNameServers;
import protocal.directory.server.dhcp.dhcp.options.vendor.Routers;
import protocal.directory.server.dhcp.dhcp.options.vendor.SubnetMask;
import protocal.directory.server.dhcp.dhcp.service.store.FixedStoreLeaseManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomFixedStoreLeaseManager extends FixedStoreLeaseManager {
	@Override
	public DhcpMessage leaseRequest(DhcpRequestContext context, DhcpMessage request, InetAddress clientRequestedAddress, long clientRequestedExpirySecs) throws DhcpException {
		DhcpMessage reply = null;
		try {
			// 调用父类方法生成响应
			reply = super.leaseRequest(context, request, clientRequestedAddress, clientRequestedExpirySecs);
			// 添加路由器选项
			if (reply != null && reply.getMessageType() == MessageType.DHCPACK) {
				InetAddress routerAddress = InetAddress.getByName("192.138.70.1"); // 替换为实际的路由器IP地址
				Routers routersOption = new Routers();
				routersOption.setAddresses((Inet4Address) routerAddress);

				// 设置子网掩码
				SubnetMask subnetMaskOption = new SubnetMask();
				subnetMaskOption.setAddress((Inet4Address) routerAddress);

				// 设置域名服务器
				DomainNameServers dnsOption = new DomainNameServers();
				// 添加一个或多个DNS服务器的IP地址
				dnsOption.setAddresses((Inet4Address) routerAddress);

				reply.getOptions().add(routersOption);
				reply.getOptions().add(dnsOption);
				reply.getOptions().add(subnetMaskOption);
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		return reply;
	}
}
