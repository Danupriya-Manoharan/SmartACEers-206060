# ACE Proofchecker — MVP Demo Guide

# ACE Proofchecker — MVP Demo Guide

A one-click validator for IBM ACE message flows. Right-click a `.msgflow`, choose
**Run Proofcheck**, and it flags common production risks (message loss, unhandled
errors, insecure transport, missing timeouts, naming/best-practice issues) in the
standard Eclipse **Problems** view, a **summary dialog**, and a **log file**.

---

## Prerequisites (one-time)

- **Eclipse IDE for RCP and RAP Developers** (2021-06+), or the **IBM ACE Toolkit**
  with the Eclipse PDE feature installed.
- **JDK 11+** — verify with `java -version`.

---

## Path A — Quickest demo (dev mode, ~5 min, no install)

Use this for a live demo. It launches a sandbox Eclipse with the plugin loaded.

1. **Import the project**
   `File → Import → General → Existing Projects into Workspace` →
   select `SmartACEers-206060/ace-proofchecker-plugin` → **Finish**.

2. **Set the Target Platform** (provides the `org.eclipse.*` libraries)
   `Window → Preferences → Plug-in Development → Target Platform` →
   check **Running Platform** → **Apply and Close**.
   If imports stay red: `Project → Clean… → Clean all projects`.

3. **Launch the plugin**
   Right-click the project → **Run As → Eclipse Application**.
   A second Eclipse instance opens with the plugin active.

4. **Run it on the sample flow**
   In the runtime Eclipse, bring a `.msgflow` into a project — use the fixture
   `ace-proofchecker-plugin/test/resources/SampleFlow.msgflow` (drag it into a
   project, or `File → Import`). Then right-click the file → **Run Proofcheck**.

5. **Show the results**
   - A **summary dialog** appears (e.g. *"6 critical, 2 warnings"*).
   - `Window → Show View → Problems` — each finding is a marker with severity,
     node name, category, and a fix suggestion. Double-click to jump to the node.
   - Full per-node trace: `~/.ace-proofcheck-logs/validation_SampleFlow_*.log`.

---

## Path B — Install into the real ACE Toolkit

Use this for a "real product" demo against actual flows.

1. Right-click the project →
   **Export → Plug-in Development → Deployable plug-ins and fragments**.
2. Check `com.smartaceers.proofchecker`, pick a destination → **Finish**.
   Output: `plugins/com.smartaceers.proofchecker_1.0.0.qualifier.jar`.
3. Copy that JAR into the ACE Toolkit's **`dropins/`** folder.
4. Restart the ACE Toolkit, then right-click any `.msgflow` → **Run Proofcheck**.

---

## Suggested 2-minute narrative

Run it on `SampleFlow.msgflow` (MQInput → Compute → MQOutput + HTTPRequest). It is
built to trigger a clean spread of findings:

| Point at…                                      | Finding              |
| ---------------------------------------------- | -------------------- |
| MQInput with `transactionMode="no"`            | **CRITICAL** — message loss risk |
| MQInput / Compute catch terminals unconnected  | **CRITICAL** — unhandled errors |
| HTTPRequest on `http://`                        | **HIGH** — unencrypted transport |
| HTTPRequest: no timeout / retry / auth          | **MEDIUM/LOW** — reliability gaps |

**Say:** *"Select a flow, one click, and it flags production risks an ACE reviewer
would catch by hand — surfaced in the standard Problems view, with a fix suggestion
on each one."* Then double-click a marker to show it navigates straight to the node.

---

## Notes / current limitations

- On **real** `.msgflow` files, node display names come from a nested
  `<translation>` element the parser doesn't read yet, so names may appear
  auto-generated, and property checks (URL, password, etc.) depend on ACE's real
  attribute names. The sample fixture sidesteps this so the demo looks clean.
- Do the **import + clean** in Eclipse before demoing so there are no red markers.
