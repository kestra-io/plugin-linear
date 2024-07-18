package io.kestra.plugin.linear.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Queries {
    TEAMS(
        """
        {
          "query": "query { teams { nodes { id name } } }"
        }
        """
    ),
    LABELS(
        """
        {
          "query": "query { issueLabels { nodes { id name } } }"
        }
        """
    );

    private final String value;
}
