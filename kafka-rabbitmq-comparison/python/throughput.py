"""Plot peak payload throughput, one bar per broker."""

import json
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.patches import Patch

RESULTS_DIR = Path(__file__).resolve().parent.parent / "results"
PLOTS_DIR = RESULTS_DIR / "plots"

BROKER_ORDER = ("activemq", "kafka", "rabbitmq", "redis")

BROKER_COLORS = {
    "activemq": "#4C78A8",
    "kafka": "#7CB342",
    "rabbitmq": "#E07A8D",
    "redis": "#F2C14E",
}

BROKER_LABELS = {
    "activemq": "ActiveMQ",
    "kafka": "Kafka",
    "rabbitmq": "RabbitMQ",
    "redis": "Redis",
}


def message_bytes() -> int:
    """UTF-8 length of the compact JSON body sent by LoadGenerator.publish.

    sentAtNanos is fixed at 18 digits because System.nanoTime() width varies.
    """
    message_id = "00000000-0000-0000-0000-000000000000"
    body = {
        "id": message_id,
        "email": f"user-{message_id}@example.com",
        "code": "000000",
        "sentAtNanos": 100_000_000_000_000_000,
        "warmup": False,
    }
    return len(json.dumps(body, separators=(",", ":")).encode("utf-8"))


MESSAGE_BYTES = message_bytes()


def peak_consume_eps(results_dir: Path) -> dict[str, float]:
    """Highest achieved consume rate for each broker."""
    peaks: dict[str, float] = {}
    for path in sorted(results_dir.glob("*-eps.json")):
        report = json.loads(path.read_text(encoding="utf-8"))
        broker = report["broker"]
        rate = float(report.get("achievedConsumeEps", 0))
        peaks[broker] = max(peaks.get(broker, 0.0), rate)
    return peaks


def throughput_mbps(events_per_second: float) -> float:
    """Payload megabytes per second for the fixed message size."""
    return events_per_second * MESSAGE_BYTES / 1_000_000


def plot(peaks: dict[str, float], output_path: Path) -> None:
    """Draw one peak-throughput bar for each broker that has a report."""
    brokers = [broker for broker in BROKER_ORDER if broker in peaks]
    positions = list(range(len(brokers)))
    heights = [throughput_mbps(peaks[broker]) for broker in brokers]

    fig, axis = plt.subplots(figsize=(10, 6))
    fig.patch.set_facecolor("white")
    axis.set_facecolor("white")

    bars = axis.bar(
        positions,
        heights,
        color=[BROKER_COLORS.get(broker, "0.4") for broker in brokers],
        width=0.6,
        zorder=2,
    )
    axis.bar_label(bars, fmt="%.2f", padding=4)

    axis.set_xlim(-0.6, len(brokers) - 0.4)
    peak = max(heights, default=0)
    axis.set_ylim(0, peak * 1.12 if peak else 1)
    axis.set_xticks(positions, [BROKER_LABELS.get(broker, broker) for broker in brokers])
    axis.set_title("Peak Throughput", pad=12)
    axis.set_ylabel("Throughput (MB/s)")
    axis.tick_params(length=0)
    for spine in axis.spines.values():
        spine.set_color("0.75")

    handles = [
        Patch(
            facecolor=BROKER_COLORS.get(broker, "0.4"),
            edgecolor="none",
            label=BROKER_LABELS.get(broker, broker),
        )
        for broker in brokers
    ]
    fig.legend(
        handles=handles,
        loc="lower center",
        bbox_to_anchor=(0.5, 0.02),
        ncol=4,
        frameon=False,
    )
    fig.tight_layout(rect=(0, 0.12, 1, 1))
    output_path.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output_path)


def main() -> None:
    peaks = peak_consume_eps(RESULTS_DIR)
    if not peaks:
        raise SystemExit(f"No reports found in {RESULTS_DIR}")

    plot(peaks, PLOTS_DIR / "throughput.png")
    plt.show()


if __name__ == "__main__":
    main()
