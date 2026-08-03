#!/usr/bin/env node
/**
 * Minimal harness-state guard stub.
 * Real phase-jump checks belong with harness runtime (not yet in this repo).
 * failClosed hooks require a working script — this allows non-harness edits.
 */
let input = "";
process.stdin.setEncoding("utf8");
process.stdin.on("data", (c) => (input += c));
process.stdin.on("end", () => {
  let payload = {};
  try {
    payload = input ? JSON.parse(input) : {};
  } catch {
    payload = {};
  }
  const filePath = String(
    payload.file_path || payload.path || payload.tool_input?.path || ""
  ).replace(/\\/g, "/");
  const isHarnessState =
    /harness-state\.ya?ml$/i.test(filePath) ||
    /harness\/.*harness-state/i.test(filePath);

  if (isHarnessState) {
    // Without full harness validator, block direct harness-state edits.
    process.stdout.write(
      JSON.stringify({
        permission: "deny",
        user_message:
          "Direct harness-state edits are blocked. Use harness-init / harness-validate when harness runtime is available.",
      })
    );
    process.exit(0);
  }

  process.stdout.write(JSON.stringify({ permission: "allow" }));
});
