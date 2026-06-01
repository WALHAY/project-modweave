#!/usr/bin/env python3
"""Plot exact (equality_search) and range (range_search) results from index_results.csv

Usage:
  python plot_exact_range.py --csv /path/to/index_results.csv --outdir /path/to/out
"""
from pathlib import Path
import argparse
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.patches import Patch
from matplotlib.ticker import FuncFormatter

PLOT_COLORS = ["#4C78A8", "#F58518", "#54A24B", "#E45756", "#72B7B2"]
BAR_HATCHES = ["", "//", "\\\\", "xx", "..", "++", "--", "**", "oo", "||"]


def configure_plotting():
    plt.style.use("seaborn-v0_8-whitegrid")
    plt.rcParams.update(
        {
            "figure.figsize": (9, 5),
            "axes.titlesize": 14,
            "axes.labelsize": 12,
            "legend.fontsize": 10,
            "xtick.labelsize": 10,
            "ytick.labelsize": 10,
        }
    )


def format_ns(value, _pos=None):
    # Human-friendly formatter for nanoseconds
    try:
        v = float(value)
    except Exception:
        return str(value)
    if v >= 1e9:
        return f"{v/1e9:.2f} s"
    if v >= 1e6:
        return f"{v/1e6:.2f} ms"
    if v >= 1e3:
        return f"{v/1e3:.2f} µs"
    return f"{v:.0f} ns"


def build_query_time_bar(df, query_name: str, output_path: Path):
    qdf = df[df["query"] == query_name]
    if qdf.empty:
        print(f"No rows for query '{query_name}' — skipping {output_path.name}")
        return

    grouped = qdf.groupby("index_type", as_index=False)["time"].mean().sort_values("index_type")
    
    # Russian titles
    query_titles = {
        "equality_search": "Время выполнения поиска по точному совпадению",
        "range_search": "Время выполнения поиска по диапазону",
        "fulltext_search": "Время выполнения полнотекстового поиска"
    }

    fig, ax = plt.subplots()
    positions = list(range(len(grouped)))
    colors = [PLOT_COLORS[i % len(PLOT_COLORS)] for i in positions]
    hatches = [BAR_HATCHES[i % len(BAR_HATCHES)] for i in positions]

    bars = ax.bar(positions, grouped["time"], color=colors, width=0.6)
    for bar, hatch in zip(bars, hatches):
        bar.set_hatch(hatch)
        bar.set_edgecolor("#2f2f2f")
        bar.set_linewidth(0.6)

    ax.set_xticks(positions)
    ax.set_xticklabels(grouped["index_type"], rotation=0)
    ax.set_ylabel("Время (наносекунды)")
    ax.set_xlabel("Тип индекса")
    ax.set_title(query_titles.get(query_name, query_name))
    ax.yaxis.set_major_formatter(FuncFormatter(format_ns))
    ax.grid(axis="y", linestyle="--", alpha=0.4)
    ax.set_axisbelow(True)

    for bar, value in zip(bars, grouped["time"]):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            value,
            format_ns(value),
            ha="center",
            va="bottom",
            fontsize=9,
        )

    fig.tight_layout()
    fig.savefig(output_path, format="svg")
    plt.close(fig)
    print(f"Saved {output_path}")


def build_index_size_bar(df, query_name: str, output_path: Path):
    """Build bar chart of index sizes filtered by a query row (sizes are per index type)."""
    qdf = df[df["query"] == query_name]
    if qdf.empty:
        print(f"No rows for query '{query_name}' — skipping {output_path.name}")
        return

    grouped = qdf.groupby("index_type", as_index=False)["size_bytes"].max().sort_values("index_type")

    # Russian titles
    titles = {
        "equality_search": "Размеры индексов (для точного совпадения)",
        "range_search": "Размеры индексов (для поиска по диапазону)",
        "fulltext_search": "Размеры индексов (для полнотекстового поиска)"
    }

    fig, ax = plt.subplots()
    positions = list(range(len(grouped)))
    colors = [PLOT_COLORS[i % len(PLOT_COLORS)] for i in positions]
    hatches = [BAR_HATCHES[i % len(BAR_HATCHES)] for i in positions]

    bars = ax.bar(positions, grouped["size_bytes"], color=colors, width=0.6)
    for bar, hatch in zip(bars, hatches):
        bar.set_hatch(hatch)
        bar.set_edgecolor("#2f2f2f")
        bar.set_linewidth(0.6)

    ax.set_xticks(positions)
    ax.set_xticklabels(grouped["index_type"], rotation=0)
    ax.set_ylabel("Размер (байты)")
    ax.set_xlabel("Тип индекса")
    ax.set_title(titles.get(query_name, query_name))
    ax.yaxis.set_major_formatter(FuncFormatter(format_bytes))
    ax.grid(axis="y", linestyle="--", alpha=0.4)
    ax.set_axisbelow(True)

    for bar, value in zip(bars, grouped["size_bytes"]):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            value,
            format_bytes(value),
            ha="center",
            va="bottom",
            fontsize=9,
        )

    fig.tight_layout()
    fig.savefig(output_path, format="svg")
    plt.close(fig)
    print(f"Saved {output_path}")


def main():
    configure_plotting()
    parser = argparse.ArgumentParser(description="Plot equality and range query times from index_results.csv")
    parser.add_argument("--csv", type=Path, default=Path.cwd() / "index_results.csv", help="Path to index_results.csv")
    parser.add_argument("--outdir", type=Path, default=Path.cwd(), help="Directory to save SVG files")
    args = parser.parse_args()

    if not args.csv.exists():
        print(f"CSV not found: {args.csv}")
        return

    df = pd.read_csv(args.csv)
    # Ensure 'time' column is numeric
    if "time" in df.columns:
        df["time"] = pd.to_numeric(df["time"], errors="coerce")
    else:
        print("CSV does not contain 'time' column")
        return

    args.outdir.mkdir(parents=True, exist_ok=True)

    build_query_time_bar(df, "equality_search", args.outdir / "equality_search_time.svg")
    build_query_time_bar(df, "range_search", args.outdir / "range_search_time.svg")
    build_query_time_bar(df, "fulltext_search", args.outdir / "fulltext_search_time.svg")

    # Combined index sizes chart
    build_index_size_combined(df, args.outdir / "index_sizes.svg")


def build_index_size_combined(df, output_path: Path):
    """Build combined bar chart of index sizes across index types."""
    grouped = df.groupby("index_type", as_index=False)["size_bytes"].max().sort_values("index_type")
    if grouped.empty:
        print("No size information found — skipping combined index size chart")
        return

    fig, ax = plt.subplots()
    positions = list(range(len(grouped)))
    colors = [PLOT_COLORS[i % len(PLOT_COLORS)] for i in positions]
    hatches = [BAR_HATCHES[i % len(BAR_HATCHES)] for i in positions]

    bars = ax.bar(positions, grouped["size_bytes"], color=colors, width=0.6)
    for bar, hatch in zip(bars, hatches):
        bar.set_hatch(hatch)
        bar.set_edgecolor("#2f2f2f")
        bar.set_linewidth(0.6)

    ax.set_xticks(positions)
    ax.set_xticklabels(grouped["index_type"], rotation=0)
    ax.set_ylabel("Размер (байты)")
    ax.set_xlabel("Тип индекса")
    ax.set_title("Размер индексов по типу")
    ax.yaxis.set_major_formatter(FuncFormatter(format_bytes))
    ax.grid(axis="y", linestyle="--", alpha=0.4)
    ax.set_axisbelow(True)

    for bar, value in zip(bars, grouped["size_bytes"]):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            value,
            format_bytes(value),
            ha="center",
            va="bottom",
            fontsize=9,
        )

    fig.tight_layout()
    fig.savefig(output_path, format="svg")
    plt.close(fig)
    print(f"Saved {output_path}")


if __name__ == "__main__":
    main()
