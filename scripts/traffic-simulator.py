import argparse
import json
import random
import time
import urllib.error
import urllib.request


EVENT_TEMPLATES = [
    {
        "eventType": "FAILED_LOGIN",
        "messages": [
            "Repeated failed login attempts",
            "Invalid credentials submitted repeatedly",
            "Multiple authentication failures detected",
        ],
        "severities": ["MEDIUM", "HIGH"],
    },
    {
        "eventType": "PORT_SCAN",
        "messages": [
            "Sequential connection attempts across multiple ports",
            "Possible TCP port scan detected",
            "Unusual port enumeration activity",
        ],
        "severities": ["MEDIUM", "HIGH"],
    },
    {
        "eventType": "MALWARE_DETECTED",
        "messages": [
            "Suspicious executable detected",
            "Malware signature matched on endpoint",
            "Potential malicious payload identified",
        ],
        "severities": ["HIGH", "CRITICAL"],
    },
    {
        "eventType": "SUSPICIOUS_REQUEST",
        "messages": [
            "Unusual request pattern detected",
            "Potential probing request received",
            "Abnormal HTTP request activity",
        ],
        "severities": ["LOW", "MEDIUM", "HIGH"],
    },
]


SOURCES = [
    "edge-firewall",
    "auth-service",
    "web-gateway",
    "endpoint-agent",
    "vpn-gateway",
    "mail-gateway",
]


def random_ip():
    return (
        f"{random.randint(10, 223)}."
        f"{random.randint(0, 255)}."
        f"{random.randint(0, 255)}."
        f"{random.randint(1, 254)}"
    )


def create_event():
    template = random.choice(EVENT_TEMPLATES)

    return {
        "source": random.choice(SOURCES),
        "eventType": template["eventType"],
        "severity": random.choice(template["severities"]),
        "message": random.choice(template["messages"]),
        "ipAddress": random_ip(),
    }


def send_event(api_url, event):
    body = json.dumps(event).encode("utf-8")

    request = urllib.request.Request(
        api_url,
        data=body,
        headers={
            "Content-Type": "application/json",
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(
            request,
            timeout=10,
        ) as response:
            return response.status

    except urllib.error.HTTPError as error:
        print(
            f"HTTP {error.code}: "
            f"{error.read().decode('utf-8')}"
        )
        return error.code

    except urllib.error.URLError as error:
        print(f"Connection error: {error.reason}")
        return None


def main():
    parser = argparse.ArgumentParser(
        description="Generate simulated Sentinel security traffic."
    )

    parser.add_argument(
        "--url",
        default="http://localhost/api/events",
        help="Sentinel event-ingestion endpoint",
    )

    parser.add_argument(
        "--interval",
        type=float,
        default=2.0,
        help="Seconds between generated events",
    )

    parser.add_argument(
        "--count",
        type=int,
        default=0,
        help="Number of events to send. 0 runs continuously.",
    )

    args = parser.parse_args()

    sent = 0

    print("Sentinel traffic simulator")
    print(f"Target:   {args.url}")
    print(f"Interval: {args.interval}s")
    print("Press Ctrl+C to stop.")
    print()

    try:
        while args.count == 0 or sent < args.count:
            event = create_event()

            status = send_event(
                args.url,
                event,
            )

            sent += 1

            print(
                f"[{sent}] "
                f"{event['severity']:<8} "
                f"{event['eventType']:<20} "
                f"{event['ipAddress']:<15} "
                f"HTTP {status}"
            )

            time.sleep(args.interval)

    except KeyboardInterrupt:
        print()
        print(f"Stopped after {sent} events.")


if __name__ == "__main__":
    main()