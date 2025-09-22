# Sharrow Multi-Service Support Documentation

## Overview

Sharrow now supports sharing content not just to MeTube instances, but also to popular torrent clients and jDownloader instances. This document explains how to configure and use these different service types.

## Supported Service Types

### 1. MeTube
The original service type that Sharrow was built for.

**Features:**
- Share YouTube and other video content to your MeTube instance
- Download videos with configurable quality settings
- Queue management through the MeTube web interface

**Configuration:**
- Service Type: MeTube
- URL: Your MeTube server address (e.g., http://192.168.1.100)
- Port: Your MeTube server port (typically 8081)

### 2. Torrent Clients
Sharrow supports three popular torrent clients with Web UI capabilities.

#### qBittorrent
**Features:**
- Add magnet links and torrent URLs directly to qBittorrent
- Web API integration for seamless sharing
- Supports both HTTP and HTTPS connections

**Configuration:**
- Service Type: Torrent Client
- Torrent Client: qBittorrent
- URL: Your qBittorrent Web UI address (e.g., http://192.168.1.100)
- Port: Your qBittorrent Web UI port (typically 8080)

#### Transmission
**Features:**
- Add magnet links and torrent URLs directly to Transmission
- RPC API integration for reliable communication
- Session management for secure connections

**Configuration:**
- Service Type: Torrent Client
- Torrent Client: Transmission
- URL: Your Transmission Web UI address (e.g., http://192.168.1.100)
- Port: Your Transmission RPC port (typically 9091)

#### uTorrent
**Features:**
- Add magnet links and torrent URLs directly to uTorrent
- Web API integration for quick sharing
- Compatible with both uTorrent and BitTorrent clients

**Configuration:**
- Service Type: Torrent Client
- Torrent Client: uTorrent
- URL: Your uTorrent Web UI address (e.g., http://192.168.1.100)
- Port: Your uTorrent Web UI port (typically 8080)

### 3. jDownloader
**Features:**
- Share download links directly to jDownloader
- Support for hundreds of file hosting services
- Advanced download management and automation

**Configuration:**
- Service Type: jDownloader
- URL: Your jDownloader address (e.g., http://192.168.1.100)
- Port: Your jDownloader port (typically varies by setup)

## Setting Up Service Profiles

### Creating a New Profile
1. Open Sharrow and go to Settings > Server Profiles
2. Tap the "+" button to add a new profile
3. Enter a name for your profile (e.g., "Home qBittorrent")
4. Select the appropriate Service Type from the dropdown
5. For Torrent Clients, select the specific client type
6. Enter the URL and Port for your service
7. Tap "Test Connection" to verify connectivity
8. Tap "Save" to store the profile

### Testing Connections
Each service type has specific testing requirements:

- **MeTube**: Sends a test request to the /add endpoint
- **Torrent Clients**: Verifies Web UI/API availability
- **jDownloader**: Checks connectivity to the service

## Using Different Service Types

### Sharing Process
1. Open any supported media app or website
2. Find content you want to share/download
3. Tap the share button
4. Select "Sharrow" from the sharing options
5. Choose the appropriate service profile from the dropdown
6. Tap "Send to Service"
7. The app will automatically open the service interface in your browser

### Service Type Recognition
Sharrow automatically detects the type of content being shared:
- **YouTube URLs**: Work with MeTube and jDownloader
- **Magnet Links**: Work with Torrent Clients and jDownloader
- **Torrent File URLs**: Work with Torrent Clients and jDownloader
- **Direct Download Links**: Work with jDownloader

## Best Practices

### Security Considerations
1. Use HTTPS when possible for all services
2. Configure strong authentication for your services
3. Limit external access to service ports when not needed
4. Regularly update your services to the latest versions

### Network Configuration
1. Ensure all services are accessible from your device
2. Configure port forwarding if accessing services remotely
3. Use consistent naming for your profiles
4. Set a default profile for your most commonly used service

### Troubleshooting
Common issues and solutions:

1. **Connection Failed**: 
   - Verify URL and port are correct
   - Check that the service is running
   - Ensure network connectivity to the service

2. **Authentication Errors**:
   - Configure proper authentication for your services
   - Check credentials if required

3. **Content Not Recognized**:
   - Verify the URL is supported by the target service
   - Check service logs for detailed error information

## Future Enhancements

Planned improvements for multi-service support:
- Enhanced error reporting with specific service diagnostics
- Batch sharing to multiple services simultaneously
- Service status monitoring and notifications
- Advanced filtering by service capabilities
- Import/export of service configurations

## Support

For issues with service configuration or connectivity:
1. Verify service documentation for API/Web UI setup
2. Check that services are properly configured for external access
3. Consult service-specific forums or documentation
4. Report Sharrow-specific issues through the GitHub repository