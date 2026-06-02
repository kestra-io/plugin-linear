# How to use the Linear plugin

Create Linear issues from Kestra flows.

## Authentication

Set `token` to your Linear personal API key, including any required prefix (e.g. the value you'd pass as the full `Authorization` header). Store it in a [secret](https://kestra.io/docs/concepts/secret).

## Tasks

`issues.Create` creates a new Linear issue — set `team` (the team name, matched case-insensitively), `title`, and optionally `description` and `labels` (a list of label names to attach).
