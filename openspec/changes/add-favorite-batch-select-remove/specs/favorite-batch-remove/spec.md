## ADDED Requirements

### Requirement: Authenticated user can batch-remove own favorites by favorite record ids

The system SHALL accept a request to remove multiple favorite records in one operation. The system SHALL only delete records whose `favorite.id` is in the provided list and whose `favorite.userId` equals the authenticated user id. The system SHALL apply the same product favorite-count and persistence rules as single-record removal.

#### Scenario: Batch remove succeeds

- **WHEN** an authenticated user sends `POST /favorites/batch-remove` with body containing a non-empty list of favorite record ids that all belong to that user
- **THEN** the system removes those favorite records and updates affected products' favorite counts consistently with single removal
- **AND** the response indicates success

#### Scenario: Batch remove rejects empty list

- **WHEN** the user sends a request with an empty list of favorite ids
- **THEN** the system rejects the request with a client error

#### Scenario: Batch remove rejects when any id is invalid or not owned

- **WHEN** the list contains an id that does not exist or belongs to another user
- **THEN** the system rejects the entire operation and does not partially delete favorites (unless a future requirement explicitly defines partial success)

#### Scenario: Batch size limit

- **WHEN** the number of ids in the request exceeds the configured maximum batch size
- **THEN** the system rejects the request with a client error

### Requirement: Favorites page supports multi-select and batch remove

The favorites UI SHALL allow the user to select multiple favorite rows and trigger a batch remove action. The UI SHALL provide a way to select all items currently shown and to clear selection.

#### Scenario: User removes multiple selected items

- **WHEN** the user selects more than one favorite and confirms batch remove
- **THEN** the client calls the batch-remove API with the selected favorite record ids
- **AND** the list refreshes to reflect removed items
