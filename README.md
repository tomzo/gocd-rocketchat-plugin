# GoCD rocketchat notification plugin

This is a notification plugin for GoCD, which sends build failures to RocketChat.

![example](example_message.png)

Features:
 - link to failed stage
 - label of the pipeline with link to VSM view
 - links (only) failed jobs and link to console of each

## Building the code base

To build the jar, run `./gradlew clean test jar`

## Setup

You need to install the jar on server in `plugins/external` directory.
Then you must configure access to rocketchat and room for notifications on plugin settings page.

![settings](settings.png)

## Contributing

Please report bugs.
For new features PRs are most welcome.

## License

Apache License, Version 2.0
