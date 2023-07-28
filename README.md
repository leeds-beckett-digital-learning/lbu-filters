# lbu-filters
Java FilterReader filters and some string functions mainly for use in ant.

## Notes about properties files
If a single entry in a properties file is changed by a filter it is desirable
that a diff comparing pre and post filter only highlight the entry that changed.
This closes down some strategies for filtering properties files because.

1. When properties java class loads and stores a file the entries are randomised in order.
2. Properties files are sometime 'hand' crafted and are non-standard. E.g. wrong
end of line delimiter, extra white space, not escaping characters that should be escaped,
escaping characters that should not be escaped, multiple comments instead of one
at the top etc.

So, the output could be different from source on every line even though no entries
have actually been changed.

Approach?