#!/bin/bash

# List of labels with optional colors
declare -A labels
labels=(
  ["backend"]="#1f77b4"
  ["auth"]="#ff7f0e"
  ["core"]="#2ca02c"
  ["validation"]="#d62728"
  ["feature"]="#9467bd"
  ["optional"]="#8c564b"
  ["bff"]="#e377c2"
  ["graphql"]="#7f7f7f"
  ["security"]="#bcbd22"
  ["setup"]="#17becf"
  ["devops"]="#aec7e8"
)

# Loop through labels and create them
for label in "${!labels[@]}"; do
  gh label create "$label" --color "${labels[$label]}" || echo "Label $label already exists"
done

echo "All labels created."

