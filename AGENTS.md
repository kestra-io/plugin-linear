# Kestra Linear Plugin

## What

- Provides plugin components under `io.kestra.plugin.linear`.
- Includes classes such as `LinearConnection`, `Create`, `IssueResponse`, `Queries`.

## Why

- What user problem does this solve? Teams need to create, update, and search Linear issues from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Linear steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Linear.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `linear`

### Key Plugin Classes

- `io.kestra.plugin.linear.issues.Create`

### Project Structure

```
plugin-linear/
├── src/main/java/io/kestra/plugin/linear/model/
├── src/test/java/io/kestra/plugin/linear/model/
├── build.gradle
└── README.md
```

## Local rules

- Base the wording on the implemented packages and classes, not on template README text.

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
