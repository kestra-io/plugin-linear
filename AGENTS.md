# Kestra Linear Plugin

## What

- Provides plugin components under `io.kestra.plugin.linear`.
- Includes classes such as `LinearConnection`, `Create`, `IssueResponse`, `Queries`.

## Why

- This plugin integrates Kestra with Linear.
- It provides tasks that create, update, and search Linear issues.

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
