import argparse
import json
import random
import time
import urllib.error
import urllib.parse
import urllib.request
from http.cookiejar import CookieJar


EVENT_TEMPLATES = [
    {
        "eventType": "FAILED_LOGIN",
        "severities": ["MEDIUM", "HIGH"],
        "messages": [
            "Repeated failed login attempts",
            "Multiple invalid credentials submitted",
            "Authentication failure threshold exceeded",
        ],
    },
    {
        "eventType": "PORT_SCAN",
        "severities": ["MEDIUM", "HIGH"],
        "messages": [
            "Sequential connection attempts across multiple ports",
            "Possible TCP port scan detected",
            "Port enumeration activity detected",
        ],
    },
    {
        "eventType": "MALWARE_DETECTED",
        "severities": ["HIGH", "CRITICAL"],
        "messages": [
            "Suspicious executable detected on endpoint",
            "Known malware signature detected",
            "Potential malicious payload identified",
        ],
    },
    {
        "eventType": "SUSPICIOUS_REQUEST",
        "severities": ["LOW", "MEDIUM", "HIGH"],
        "messages": [
            "Unusual HTTP request pattern detected",
            "Potential probing request received",
            "Abnormal request activity detected",
        ],
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


def create_opener():
    cookie_jar = CookieJar()

    opener = urllib.request.build_opener(
        urllib.request.HTTPCookieProcessor(cookie_jar)
    )

    return opener


def read_json(response):
    body = response.read().decode("utf-8")

    if not body:
        return {}

    return json.loads(body)


def get_csrf(opener, base_url):
    request = urllib.request.Request(
        f"{base_url}/api/auth/csrf",
        method="GET",
    )

    with opener.open(request, timeout=10) as response:
        data = read_json(response)

    token = (
        data.get("token")
        or data.get("csrfToken")
        or data.get("_csrf")
    )

    header_name = (
        data.get("headerName")
        or data.get("header")
        or "X-CSRF-TOKEN"
    )

    if not token:
        raise RuntimeError(
            "CSRF endpoint did not return a recognizable token."
        )

    return token, header_name


def login(
    opener,
    base_url,
    username,
    password,
):
    csrf_token, csrf_header = get_csrf(
        opener,
        base_url,
    )

    body = json.dumps(
        {
            "username": username,
            "password": password,
        }
    ).encode("utf-8")

    request = urllib.request.Request(
        f"{base_url}/api/auth/login",
        data=body,
        headers={
            "Content-Type": "application/json",
            csrf_header: csrf_token,
        },
        method="POST",
    )

    with opener.open(request, timeout=10) as response:
        user = read_json(response)

    return user


def random_ip():
    return ".".join(
        [
            str(random.randint(10, 223)),
            str(random.randint(0, 255)),
            str(random.randint(0, 255)),
            str(random.randint(1, 254)),
        ]
    )


def create_event():
    template = random.choice(EVENT_TEMPLATES)

    return {
        "source": random.choice(SOURCES),
        "eventType": template["eventType"],
        "severity": random.choice(
            template["severities"]
        ),
        "message": random.choice(
            template["messages"]
        ),
        "ipAddress": random_ip(),
    }


def send_event(
    opener,
    base_url,
    event,
):
    csrf_token, csrf_header = get_csrf(
        opener,
        base_url,
    )

    body = json.dumps(event).encode("utf-8")

    request = urllib.request.Request(
        f"{base_url}/api/events",
        data=body,
        headers={
            "Content-Type": "application/json",
            csrf_header: csrf_token,
        },
        method="POST",
    )

    try:
        with opener.open(
            request,
            timeout=10,
        ) as response:
            response.read()
            return response.status

    except urllib.error.HTTPError as error:
        body = error.read().decode(
            "utf-8",
            errors="replace",
        )

        if error.code == 429:
            print(
                "Rate limited by Sentinel "
                "(HTTP 429)."
            )
        elif error.code == 401:
            print(
                "Authentication rejected "
                "(HTTP 401)."
            )
        elif error.code == 403:
            print(
                "Request rejected by CSRF/"
                "authorization (HTTP 403)."
            )
        else:
            print(
                f"HTTP {error.code}: {body}"
            )

        return error.code


def main():
    parser = argparse.ArgumentParser(
        description=(
            "Generate authenticated simulated "
            "security traffic for Sentinel."
        )
    )

    parser.add_argument(
        "--url",
        default="http://localhost",
        help=(
            "Sentinel base URL "
            "(default: http://localhost)"
        ),
    )

    parser.add_argument(
        "--username",
        required=True,
        help="Sentinel analyst username",
    )

    parser.add_argument(
        "--password",
        required=True,
        help="Sentinel analyst password",
    )

    parser.add_argument(
        "--interval",
        type=float,
        default=2.0,
        help=(
            "Seconds between events "
            "(default: 2)"
        ),
    )

    parser.add_argument(
        "--count",
        type=int,
        default=0,
        help=(
            "Events to generate. "
            "0 means run continuously."
        ),
    )

    args = parser.parse_args()

    base_url = args.url.rstrip("/")

    opener = create_opener()

    try:
        user = login(
            opener,
            base_url,
            args.username,
            args.password,
        )
    except Exception as error:
        print(
            f"Unable to log in to Sentinel: {error}"
        )
        return

    print("Sentinel traffic simulator")
    print()
    print(
        f"Authenticated as: "
        f"{user.get('username', args.username)}"
    )
    print(f"Target:           {base_url}")
    print(f"Interval:         {args.interval}s")

    if args.count == 0:
        print("Events:           continuous")
    else:
        print(f"Events:           {args.count}")

    print()
    print("Press Ctrl+C to stop.")
    print()

    sent = 0

    try:
        while (
            args.count == 0
            or sent < args.count
        ):
            event = create_event()

            status = send_event(
                opener,
                base_url,
                event,
            )

            sent += 1

            print(
                f"[{sent:04}] "
                f"{event['severity']:<8} "
                f"{event['eventType']:<20} "
                f"{event['source']:<15} "
                f"{event['ipAddress']:<15} "
                f"HTTP {status}"
            )

            time.sleep(args.interval)

    except KeyboardInterrupt:
        print()
        print(
            f"Simulator stopped after "
            f"{sent} generated events."
        )


if __name__ == "__main__":
    main()