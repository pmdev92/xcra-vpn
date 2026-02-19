//package com.xray.core.rust.client.xcra.core;
//
//import static com.irspeedy.vpn.client.Util.Utility.getRandomString;
//import static com.irspeedy.vpn.client.Util.Utility.isHostName;
//
//import com.google.gson.Gson;
//import com.google.gson.annotations.SerializedName;
//import com.irspeedy.vpn.client.Entities.SpeedyRoute;
//import com.irspeedy.vpn.client.Entities.VpnServer;
//import com.irspeedy.vpn.client.Xray.InboundSettings.SocksInboundSetting;
//import com.irspeedy.vpn.client.Xray.Inbounds.Inbound;
//import com.irspeedy.vpn.client.Xray.Log.Log;
//import com.irspeedy.vpn.client.Xray.OutboundSettings.BlackholeOutboundSetting;
//import com.irspeedy.vpn.client.Xray.OutboundSettings.FreedomOutboundSetting;
//import com.irspeedy.vpn.client.Xray.OutboundSettings.SocksOutboundSetting;
//import com.irspeedy.vpn.client.Xray.Outbounds.Outbound;
//import com.irspeedy.vpn.client.Xray.Route.Route;
//import com.irspeedy.vpn.client.Xray.Streams.StreamSettings;
//import com.irspeedy.vpn.client.Xray.dns.Dns;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class XrayConfig {
//    @SerializedName("dns")
//    public Dns dns = new Dns();
//
//    @SerializedName("log")
//    public Log log = new Log();
//    @SerializedName("routing")
//    public Route routing = new Route();
//    @SerializedName("inbounds")
//    public ArrayList<Inbound> inbounds = new ArrayList<>();
//
//    @SerializedName("outbounds")
//    public ArrayList<Outbound> outbounds = new ArrayList<>();
//
//    private XrayConfig() {
//
//    }
//
//
//    public static XrayConfig createForConnect(VpnServer vpnServer,
//                                              String socksAddress,
//                                              int sockPort,
//                                              List<SpeedyRoute> speedyRoutes,
//                                              boolean vodActive,
//                                              int vodPort
//    ) {
//        XrayConfig xrayConfig = new XrayConfig();
//        xrayConfig.inbounds.add(new Inbound(socksAddress, sockPort, "socks", new SocksInboundSetting()));
//        XrayParser xrayParser = new XrayParser(vpnServer.getLink());
//        Outbound outbound = xrayParser.parse();
//        xrayConfig.outbounds.add(outbound);
//        xrayConfig.applyFragment(outbound);
//        xrayConfig.routing = new Route(speedyRoutes);
//
//        xrayConfig.addChain(vpnServer);
//        xrayConfig.addExtraOutBounds();
//
//        if (vodActive) {
//            xrayConfig.addVodOutboundSocks(vodPort);
//        }
//
//        return xrayConfig;
//    }
//
//    public static XrayConfig createForTester(VpnServer vpnServer, int localPort) {
//        XrayConfig xrayConfig = new XrayConfig();
//        xrayConfig.inbounds.add(new Inbound("127.0.0.1", localPort, "socks", new SocksInboundSetting()));
//        XrayParser xrayParser = new XrayParser(vpnServer.getLink());
//        Outbound outbound = xrayParser.parse();
//        xrayConfig.outbounds.add(outbound);
//
//        xrayConfig.addChain(vpnServer);
//
//        xrayConfig.applyFragment(outbound);
//        xrayConfig.addExtraOutBounds();
//        return xrayConfig;
//    }
//
//    public static XrayConfig createForTester(String link, int localPort) {
//        XrayConfig xrayConfig = new XrayConfig();
//        xrayConfig.inbounds.add(new Inbound("127.0.0.1", localPort, "socks", new SocksInboundSetting()));
//        XrayParser xrayParser = new XrayParser(link);
//        Outbound outbound = xrayParser.parse();
//        xrayConfig.outbounds.add(outbound);
//
//        xrayConfig.applyFragment(outbound);
//        xrayConfig.addExtraOutBounds();
//        return xrayConfig;
//    }
//
//    public static XrayConfig createForVod(String link, int localPort) {
//        XrayConfig xrayConfig = new XrayConfig();
//        xrayConfig.inbounds.add(new Inbound("127.0.0.1", localPort, "socks", new SocksInboundSetting()));
//        XrayParser xrayParser = new XrayParser(link);
//        Outbound outbound = xrayParser.parse();
//        xrayConfig.outbounds.add(outbound);
//        xrayConfig.applyFragment(outbound);
//        xrayConfig.addExtraOutBounds();
//        return xrayConfig;
//    }
//
//    private void addExtraOutBounds() {
//        outbounds.add(new Outbound("freedom", "direct", new FreedomOutboundSetting(), null));
//        outbounds.add(new Outbound("blackhole", "block", new BlackholeOutboundSetting(), null));
//        ArrayList<String> domains = new ArrayList<>();
//        for (Outbound outbound : outbounds) {
//            if (outbound.settings.getAddress() != null) {
//                String domain = outbound.settings.getAddress();
//                if (isHostName(domain)) {
//                    domains.add(domain);
//                }
//            }
//        }
//        if (!domains.isEmpty()) {
//            dns.servers.add(0, new Dns.DnsDirectServer(domains));
//        }
//    }
//
//    public String toJson() {
//        Gson gson = new Gson();
//        return gson.toJson(this);
//    }
//
//    private void applyFragment(Outbound outbound) {
//        if (outbound != null && outbound.fragment != null) {
//            if (!"tls".equalsIgnoreCase(outbound.streamSettings.security)
//                    && !"reality".equalsIgnoreCase(outbound.streamSettings.security)
//            ) {
//                return;
//            }
//            String fragment = outbound.fragment;
//            String[] items = fragment.split(",");
//            if (items.length != 3) {
//                return;
//            }
//            String packets = items[2];
//            if ("reality".equalsIgnoreCase(outbound.streamSettings.security)) {
//                packets = "1-3";
//            }
//            StreamSettings fragmentStreamSettings = new StreamSettings();
//            fragmentStreamSettings.sockopt.domainStrategy = null;
//            fragmentStreamSettings.sockopt.mark = 255;
//            fragmentStreamSettings.sockopt.tcpNoDelay = true;
//            String fragmentTag = "fragment-" + getRandomString(5);
//            Outbound fragmentOutbound = new Outbound(
//                    "freedom",
//                    fragmentTag,
//                    new FreedomOutboundSetting(packets, items[1], items[0]),
//                    fragmentStreamSettings
//            );
//            outbound.fragmentOutbound = fragmentOutbound;
//            outbounds.add(fragmentOutbound);
//            outbound.streamSettings.sockopt.dialerProxy = fragmentTag;
//        }
//    }
//
//    private void addChain(VpnServer vpnServer) {
//        if (vpnServer.isChainEnabled()) {
//            addChain(vpnServer.getExtraLink());
//        }
//    }
//
//    private void addChain(String extraLink) {
//        Outbound extraOutbound = new XrayParser(extraLink).parse();
//        if (extraOutbound != null) {
//            applyFragment(extraOutbound);
//            extraOutbound.tag = "proxy-chain";
//            outbounds.add(extraOutbound);
//            Outbound outbound = outbounds.get(0);
//            StreamSettings streamSettings;
//            if (outbound.fragmentOutbound != null) {
//                streamSettings = outbound.fragmentOutbound.streamSettings;
//            } else {
//                streamSettings = outbound.streamSettings;
//                if (streamSettings == null) {
//                    streamSettings = new StreamSettings();
//                    outbound.streamSettings = streamSettings;
//                }
//            }
//            streamSettings.sockopt.dialerProxy = "proxy-chain";
//        }
//    }
//
//    public void addVodOutboundSocks(int vodPort) {
//        String tag = "vod";
//        outbounds.add(new Outbound("socks", tag, new SocksOutboundSetting("127.0.0.1", vodPort), null));
//        routing.addVodRules(tag);
//    }
//}