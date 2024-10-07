# XCASTER

XCASTER is a tool developed that is designed to broadcast **any** hostname of a machine over the local network using mDNS. 

## Download

Download the latest release of XCASTER from the link below:

[Download XCASTER v1.0.0](https://github.com/Kobeeeef/XCASTER/releases/download/v1.0.0/XCASTER.jar)

## Usage

To run XCASTER, open a terminal and execute the following command:

```sh
java -Djava.net.preferIPv4Stack=true -jar XCASTER.jar {hostname} {username} {password}
```
## Browsing for XCASTER Services

To browse for machines running the XCASTER service on your local network, use the following command:

```bash
avahi-browse -rt _xcaster._tcp
