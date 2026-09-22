package com.xray.core.rust.client.xcra.dto.core.outbound

import com.google.gson.annotations.SerializedName
import com.xray.core.rust.client.xcra.enums.ConfigType

data class Outbound(
    @SerializedName("tag")
    var tag: String? = null,
    @SerializedName("detour")
    var detour: String? = null,
    @SerializedName("protocol")
    var protocol: String,
    @SerializedName("settings")
    var settings: Setting? = null,
    @SerializedName("stream_settings")
    var streamSettings: StreamSetting? = null,
) {
    abstract class Setting

    class FreedomOutboundSetting : Setting() {

    }

    class BlockOutboundSetting : Setting() {

    }


    data class Socks5Setting(
        @SerializedName("address")
        var address: String?,
        @SerializedName("port")
        var port: Int,
        @SerializedName("username")
        var username: String?,
        @SerializedName("password")
        var password: String?,
    ) : Setting()

    data class VlessSetting(
        @SerializedName("address")
        var address: String?,
        @SerializedName("port")
        var port: Int?,
        @SerializedName("id")
        var id: String?,
        @SerializedName("encryption")
        var encryption: String? = null,
        @SerializedName("flow")
        var flow: String? = null,
    ) : Setting()


    data class VmessSetting(
        @SerializedName("address")
        var address: String?,
        @SerializedName("port")
        var port: Int?,
        @SerializedName("id")
        var id: String?,
        @SerializedName("security")
        var security: String? = null,
    ) : Setting()

    data class TrojanSetting(
        @SerializedName("address")
        var address: String?,
        @SerializedName("port")
        var port: Int?,
        @SerializedName("password")
        var password: String?,
    ) : Setting()

    data class ShadowSocksSetting(
        @SerializedName("address")
        var address: String?,
        @SerializedName("port")
        var port: Int?,
        @SerializedName("password")
        var password: String?,
        @SerializedName("method")
        var method: String?,
        @SerializedName("uot")
        var uot: Boolean? = null,
        @SerializedName("uot_version")
        var uotVersion: Int? = null,
        @SerializedName("uot_is_connect")
        var uotIsConnect: Boolean? = null,
    ) : Setting()

    data class Hysteria2Settings(
        @SerializedName("address")
        var address: String?,
        @SerializedName("port")
        var port: Int?,
        @SerializedName("password")
        var password: String?,
        @SerializedName("obfs_type")
        var obfsType: String? = null,
        @SerializedName("obfs_password")
        var obfsPassword: String? = null,
        @SerializedName("gecko_min_packet_len")
        var geckoMinPacketLen: Int? = null,
        @SerializedName("gecko_max_packet_len")
        var geckoMaxPacketLen: Int? = null,
        @SerializedName("hop_ports")
        var hopPorts: String? = null,
        @SerializedName("hop_intervals")
        var hopIntervals: String? = null,
        @SerializedName("tls_config")
        var tlsSettings: TlsSettings? = null,
    ) : Setting()

    data class TuicSettings(
        @SerializedName("address")
        var address: String?,
        @SerializedName("port")
        var port: Int?,
        @SerializedName("password")
        var password: String?,
        @SerializedName("uuid")
        var uuid: String? = null,
        @SerializedName("heartbeat")
        var heartbeat: String? = null,
        @SerializedName("congestion_control")
        var congestionControl: String? = null,
        @SerializedName("udp_relay_mode")
        var udpRelayMode: String? = null,
        @SerializedName("tls_config")
        var tlsSettings: TlsSettings? = null,
    ) : Setting()

    data class StreamSetting(
        @SerializedName("transport")
        var transport: String = "tcp",
        @SerializedName("security")
        var security: String = "none",
        @SerializedName("tls_settings")
        var tlsSettings: TlsSettings? = null,
        @SerializedName("reality_settings")
        var realitySettings: RealitySettings? = null,
        @SerializedName("tcp_settings")
        var tcpSettings: TcpSettings? = null,
        @SerializedName("ws_settings")
        var wsSettings: WsSettings? = null,
        @SerializedName("http_upgrade_settings")
        var httpUpgradeSettings: HttpUpgradeSettings? = null,
        @SerializedName("x_http_settings")
        var xHttpSettings: XHttpConfig? = null,
        @SerializedName("http_settings")
        var http2Settings: Http2Settings? = null,
        @SerializedName("grpc_settings")
        var grpcSettings: GrpcSettings? = null,
    ) : Setting()

    data class TlsSettings(
        @SerializedName("server_name")
        var serverName: String? = null,
        @SerializedName("verify")
        var verify: Boolean = false,
        @SerializedName("is_early_data")
        var isEarlyData: Boolean = false,
        @SerializedName("early_data_len")
        var earlyDataLen: Int? = null,
        @SerializedName("alpn")
        var alpn: List<String>? = null,
        @SerializedName("pinned_peer_cert_sha256")
        var pinnedPeerCertSha256: List<String>? = null,
        @SerializedName("verify_peer_cert_by_name")
        var verifyPeerCertByName: List<String>? = null,
    )

    data class RealitySettings(
        @SerializedName("server_name")
        var serverName: String? = null,
        @SerializedName("verify")
        var verify: Boolean = false,
        @SerializedName("public_key")
        var publicKey: String? = null,
        @SerializedName("short_id")
        var shortId: String? = null,
        @SerializedName("version_x")
        var versionX: Int? = null,
        @SerializedName("version_y")
        var versionY: Int? = null,
        @SerializedName("version_z")
        var versionZ: Int? = null,
        @SerializedName("is_early_data")
        var isEarlyData: Boolean = false,
        @SerializedName("early_data_len")
        var earlyDataLen: Int? = null,
        @SerializedName("alpn")
        var alpn: List<String>? = null,
    )

    data class TcpSettings(
        @SerializedName("type")
        var type: String? = null,
        @SerializedName("request")
        var request: TcpRequest? = null,
    ) {
        data class TcpRequest(
            @SerializedName("version")
            var version: String? = "1.1",
            @SerializedName("method")
            var method: String? = "GET",
            @SerializedName("path")
            var path: String? = "/",
            @SerializedName("headers")
            var headers: HashMap<String, String> = hashMapOf(),
        )
    }


    data class WsSettings(
        @SerializedName("host")
        var host: String? = null,
        @SerializedName("path")
        var path: String? = null,
    )

    data class HttpUpgradeSettings(
        @SerializedName("host")
        var host: String? = null,
        @SerializedName("path")
        var path: String? = null,
    )

    data class XHttpConfig(
        @SerializedName("host")
        var host: String? = null,
        @SerializedName("path")
        var path: String? = null,
        @SerializedName("mode")
        var mode: String? = null,
        @SerializedName("extra")
        var extra: XHttpConfigExtra? = null,
    ) : Setting() {
        data class XHttpConfigXmux(
            @SerializedName("max_concurrency")
            var maxConcurrency: String? = null,
            @SerializedName("max_connections")
            var maxConnections: String? = null,
            @SerializedName("c_max_reuse_times")
            var cMaxReuseTimes: String? = null,
            @SerializedName("h_max_request_times")
            var hMaxRequestTimes: String? = null,
            @SerializedName("h_max_reusable_secs")
            var hMaxReusableSecs: String? = null,
            @SerializedName("h_keep_alive_period")
            var hKeepAlivePeriod: Long? = null
        )

        data class XHttpConfigDownloadSettings(
            @SerializedName("address")
            var address: String? = null,
            @SerializedName("port")
            var port: Int? = null,
            @SerializedName("transport")
            var transport: String? = null,
            @SerializedName("security")
            var security: String? = null,
            @SerializedName("tls_settings")
            var tlsSettings: TlsSettings? = null,
            @SerializedName("reality_settings")
            var realitySettings: RealitySettings? = null,
            @SerializedName("x_http_settings")
            var xHttpSettings: XHttpConfig? = null,
        )

        data class XHttpConfigExtra(
            @SerializedName("headers")
            var headers: HashMap<String, String>? = null,
            @SerializedName("no_grpc_header")
            var noGrpcHeader: Boolean? = null,
            @SerializedName("x_padding_bytes")
            var xPaddingBytes: String? = null,
            @SerializedName("x_padding_obfs_mode")
            var xPaddingObfsMode: Boolean? = null,
            @SerializedName("x_padding_key")
            var xPaddingKey: String? = null,
            @SerializedName("x_padding_header")
            var xPaddingHeader: String? = null,
            @SerializedName("x_padding_placement")
            var xPaddingPlacement: String? = null,
            @SerializedName("x_padding_method")
            var xPaddingMethod: String? = null,
            @SerializedName("sc_max_each_post_bytes")
            var scMaxEachPostBytes: String? = null,
            @SerializedName("sc_min_posts_interval_ms")
            var scMinPostsIntervalMs: String? = null,
            @SerializedName("uplink_http_method")
            var uplinkHttpMethod: String? = null,
            @SerializedName("uplink_data_placement")
            var uplinkDataPlacement: String? = null,
            @SerializedName("uplink_data_key")
            var uplinkDataKey: String? = null,
            @SerializedName("uplink_chunk_size")
            var uplinkChunkSize: String? = null,
            @SerializedName("session_placement")
            var sessionPlacement: String? = null,
            @SerializedName("session_key")
            var sessionKey: String? = null,
            @SerializedName("seq_placement")
            var seqPlacement: String? = null,
            @SerializedName("seq_key")
            var seqKey: String? = null,
            @SerializedName("xmux")
            var xmux: XHttpConfigXmux? = null,
            @SerializedName("download_settings")
            var downloadSettings: XHttpConfigDownloadSettings? = null,
        )
    }

    data class Http2Settings(
        @SerializedName("host")
        var host: String? = null,
        @SerializedName("path")
        var path: String? = null,
    )

    data class GrpcSettings(
        @SerializedName("service_name")
        var serviceName: String? = null,
    )

    companion object {
        fun createInitOutbound(configType: ConfigType): Outbound {
            val outbound = Outbound(
                protocol = configType.getProtocol()
            )
            return outbound
        }
    }
}
