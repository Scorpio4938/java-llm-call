# Changelog

## [v1.0.0] - General Functionalities

### Added
- Introduced LLMApiClient for making HTTP requests to various language model APIs.
- Implemented dynamic request building with LLMRequest and response parsing via LLMResponse.
- Added a fallback mechanism through ModelChain, allowing for chained model calls when the primary model fails.
- Enhanced provider management by supporting multiple providers with functions to add, update, and retrieve providers.
- Developed utility classes:
  - Validation: Enforces non-null parameters.
  - MapSorter: Provides methods to sort map entries by keys or values.
  - Debugger: Logs relevant debug messages with timestamps.
- Integrated unit tests to validate core functionalities such as request processing, input validation, provider configuration, and the fallback logic.