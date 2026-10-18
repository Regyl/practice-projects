"""Plot end-to-end latency quantiles, one chart per target EPS."""

import json
from collections import defaultdict
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.patches import Patch

RESULTS_DIR = Path(__file__).resolve().parent.parent / "results"
PLOTS_DIR = RESULTS_DIR / "plots"

QUANTILES = (
    ("p50Micros", "50.00 %"),
    ("p95Micros", "95.00 %"),
    ("p99Micros", "99.00 %"),
    ("p999Micros", "99.90 %"),
)

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


def achieved_target(report: dict) -> bool:
    """True when both publish and consume rates reached the target EPS."""
    target = report["targetEps"]
    return (
        report.get("achievedPublishEps", 0) >= target
        and report.get("achievedConsumeEps", 0) >= target
    )


def load_reports(results_dir: Path) -> dict[int, list[dict]]:
    """Group latency reports that reached their target EPS."""
    by_eps: dict[int, list[dict]] = defaultdict(list)
    for path in sorted(results_dir.glob("*-eps.json")):
        report = json.loads(path.read_text(encoding="utf-8"))
        if not achieved_target(report):
            continue
        by_eps[report["targetEps"]].append(report)
    for reports in by_eps.values():
        reports.sort(key=lambda report: report["broker"])
    return dict(sorted(by_eps.items()))


def plot_eps(target_eps: int, reports: list[dict], output_path: Path) -> None:
    """Draw one quantile chart for a single target EPS."""
    positions = list(range(len(QUANTILES)))
    fig, axis = plt.subplots(figsize=(10, 6))
    fig.patch.set_facecolor("white")
    axis.set_facecolor("white")

    for index in positions:
        axis.axvline(index, color="0.75", linestyle="--", linewidth=1, zorder=0)

    handles = []
    for report in reports:
        broker = report["broker"]
        color = BROKER_COLORS.get(broker, "0.4")
        latency_ms = [report[field] / 1000 for field, _ in QUANTILES]
        axis.plot(positions, latency_ms, color=color, linewidth=2.2, zorder=2)
        label = f"{BROKER_LABELS.get(broker, broker)} - {target_eps:,} EPS"
        handles.append(Patch(facecolor=color, edgecolor="none", label=label))

    axis.set_xlim(-0.35, len(QUANTILES) - 0.65)
    axis.set_ylim(bottom=0)
    axis.set_xticks(positions, [label for _, label in QUANTILES])
    axis.set_title("End-to-End Latency Quantiles", pad=12)
    axis.set_xlabel("Percentile")
    axis.set_ylabel("Latency (ms)")
    axis.tick_params(length=0)
    for spine in axis.spines.values():
        spine.set_color("0.75")

    fig.legend(
        handles=handles,
        loc="lower center",
        bbox_to_anchor=(0.5, 0.02),
        ncol=2,
        frameon=False,
    )
    fig.tight_layout(rect=(0, 0.12, 1, 1))
    output_path.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output_path)


def main() -> None:
    by_eps = load_reports(RESULTS_DIR)
    if not by_eps:
        raise SystemExit(f"No completed reports found in {RESULTS_DIR}")

    stale = PLOTS_DIR / "latency-vs-eps.png"
    if stale.exists():
        stale.unlink()

    for target_eps, reports in by_eps.items():
        plot_eps(target_eps, reports, PLOTS_DIR / f"latency-{target_eps}-eps.png")
    plt.show()


if __name__ == "__main__":
    main()
