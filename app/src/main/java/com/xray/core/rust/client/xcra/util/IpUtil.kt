package com.xray.core.rust.client.xcra.util

import com.xray.core.rust.client.xcra.App
import org.apache.commons.validator.routines.DomainValidator
import java.net.InetAddress
import java.net.ServerSocket

object IpUtil {
    private val IPV4_REGEX =
        Regex("^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$")
    private val IPV6_REGEX =
        Regex("^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$")

    /**
     * Check if a string is a valid IPv4 address.
     *
     * @param value The string to check.
     * @return True if the string is a valid IPv4 address, false otherwise.
     */
    private fun isIpv4Address(value: String): Boolean {
        return IPV4_REGEX.matches(value)
    }

    /**
     * Check if a string is a valid IPv6 address.
     *
     * @param value The string to check.
     * @return True if the string is a valid IPv6 address, false otherwise.
     */
    fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.startsWith("[") && addr.endsWith("]")) {
            addr = addr.drop(1).dropLast(1)
        }
        return IPV6_REGEX.matches(addr)
    }

    /**
     * Check if a string is a valid IP address.
     *
     * @param value The string to check.
     * @return True if the string is a valid IP address, false otherwise.
     */
    fun isIpAddress(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false

        try {
            var addr = value.trim()
            if (addr.isEmpty()) return false

            //CIDR
            if (addr.contains("/")) {
                val arr = addr.split("/")
                if (arr.size == 2 && arr[1].toIntOrNull() != null && arr[1].toInt() > -1) {
                    addr = arr[0]
                }
            }

            // Handle IPv4-mapped IPv6 addresses
            if (addr.startsWith("::ffff:") && '.' in addr) {
                addr = addr.drop(7)
            } else if (addr.startsWith("[::ffff:") && '.' in addr) {
                addr = addr.drop(8).replace("]", "")
            }

            val octets = addr.split('.')
            if (octets.size == 4) {
                if (octets[3].contains(":")) {
                    addr = addr.substringBefore(":")
                }
                return isIpv4Address(addr)
            }

            return isIpv6Address(addr)
        } catch (e: Exception) {
            App.log("Failed to validate IP address $e")
            return false
        }
    }

    /**
     * Check if a string is a pure IP address (IPv4 or IPv6).
     *
     * @param value The string to check.
     * @return True if the string is a pure IP address, false otherwise.
     */
    fun isPureIpAddress(value: String): Boolean {
        return isIpv4Address(value) || isIpv6Address(value)
    }

    /**
     * Check if a string is a valid domain name.
     *
     * A valid domain name must not be an IP address and must be a valid URL format.
     *
     * @param input The string to check.
     * @return True if the string is a valid domain name, false otherwise.
     */
//    fun isDomainName(input: String?): Boolean {
//        if (input.isNullOrEmpty()) return false
//
//        // Must not be an IP address and must be a valid URL format
//        return !isPureIpAddress(input) && isValidUrl(input)
//    }

    fun isDomainName(input: String?): Boolean {
        if (input.isNullOrEmpty()) return false
        return !isPureIpAddress(input) && DomainValidator.getInstance(true).isValid(input)
    }

    /**
     * Check if an IP address is within a CIDR range
     *
     * @param ip The IP address to check
     * @param cidr The CIDR notation range (e.g., "192.168.1.0/24")
     * @return True if the IP is within the CIDR range, false otherwise
     */
    fun isIpInCidr(ip: String, cidr: String): Boolean {
        try {
            if (!isIpAddress(ip)) return false

            // Parse CIDR (e.g., "192.168.1.0/24")
            val (cidrIp, prefixLen) = cidr.split("/")
            val prefixLength = prefixLen.toInt()

            // Convert IP and CIDR's IP portion to Long
            val ipLong = inetAddressToLong(InetAddress.getByName(ip))
            val cidrIpLong = inetAddressToLong(InetAddress.getByName(cidrIp))

            // Calculate subnet mask (e.g., /24 → 0xFFFFFF00)
            val mask = if (prefixLength == 0) 0L else (-1L shl (32 - prefixLength))

            // Check if they're in the same subnet
            return (ipLong and mask) == (cidrIpLong and mask)
        } catch (e: Exception) {
            App.log("Failed to check if IP is in CIDR $e")
            return false
        }
    }


    /**
     * Converts an InetAddress to its long representation
     *
     * @param ip The InetAddress to convert
     * @return The long representation of the IP address
     */
    private fun inetAddressToLong(ip: InetAddress): Long {
        val bytes = ip.address
        var result: Long = 0
        for (i in bytes.indices) {
            result = result shl 8 or (bytes[i].toInt() and 0xff).toLong()
        }
        return result
    }

    public fun findFreeTcpPort(): Int {
        ServerSocket(0).use { socket ->
            return socket.localPort
        }
    }
}