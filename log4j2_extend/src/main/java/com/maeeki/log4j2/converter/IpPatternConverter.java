package com.maeeki.log4j2.converter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author zhaotengchao
 * @since 2018-08-09 10:15
 **/
@Plugin(name = "IpPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "ip" })
@PerformanceSensitive("allocation")
public class IpPatternConverter extends LogEventPatternConverter {
    private static final IpPatternConverter INSTANCE =
            new IpPatternConverter();
    private static String IP = "";

    static {
        try {
            IP = getIpAddress().getHostAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private IpPatternConverter()  {
        super("IP", "ip");
    }

    public static IpPatternConverter newInstance(
            final String[] options) {
        return INSTANCE;
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        toAppendTo.append(IP);
    }

    private static InetAddress getIpAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            if (!current.isUp() || current.isLoopback() || current.isVirtual()){
                continue;
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr.isLoopbackAddress()){
                    continue;
                }
                //去掉还回和虚拟地址
                if (addr.isSiteLocalAddress()) {
                    return addr;
                }
            }
        }
        throw new SocketException("Can't get our ip address, interfaces are: " + interfaces);
    }
}
