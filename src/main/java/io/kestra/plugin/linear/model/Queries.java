package io.kestra.plugin.linear.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Queries {
    TEAMS(
        """
            query ($after: String) {
              teams(after: $after) {
                pageInfo {
                  hasNextPage
                  endCursor
                }
                nodes {
                  id
                  name
                }
              }
            }
            """
    ),
    LABELS(
        """
            query ($after: String, $teamId: ID!) {
              issueLabels(
                after: $after
                filter: { or: [{ team: { id: { eq: $teamId } } }, { team: { null: true } }] }
              ) {
                pageInfo {
                  hasNextPage
                  endCursor
                }
                nodes {
                  id
                  name
                }
              }
            }
            """
    );

    private final String value;
}
